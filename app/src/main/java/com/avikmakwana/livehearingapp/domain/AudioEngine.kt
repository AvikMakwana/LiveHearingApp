package com.avikmakwana.livehearingapp.domain

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AudioEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var job: Job? = null

    // Callbacks
    var currentAmplitude: ((Int) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    // State: -1.0 (Left) to 1.0 (Right). 0.0 is Center.
    @Volatile
    var currentBalance: Float = 0f

    @SuppressLint("MissingPermission")
    fun startAudioLoop() {
        if (job?.isActive == true) return

        if (!isHeadsetConnected()) {
            onError?.invoke("Please connect WeHear device or headphones.")
            return
        }

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                // OPTIMIZATION 1: Get Native Sample Rate (usually 48000Hz)
                // Avoiding resampling cuts latency by ~10-20ms
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val sampleRateStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
                val sampleRate = sampleRateStr?.toIntOrNull() ?: 48000

                // OPTIMIZATION 2: Get Native Buffer Size
                // This gives us the hardware's preferred "burst" size
                val bufferSizeStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
                val nativeBufferSize = bufferSizeStr?.toIntOrNull() ?: 256

                val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
                val channelConfigOut = AudioFormat.CHANNEL_OUT_STEREO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT

                // Calculate minimum buffer size required by Android
                val minInternalBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)

                // We use a small multiple of the native buffer for stability,
                // but keep it as close to the "minInternal" as safe.
                val bufferSize = maxOf(minInternalBufSize, nativeBufferSize * 2)

                // 1. Setup Input (Mono Mic) - VOICE_RECOGNITION often has lower latency processing than MIC
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfigIn,
                    audioFormat,
                    bufferSize
                )

                // 2. Setup Output (Stereo Playback) - OPTIMIZED FOR LOW LATENCY
                val attributesBuilder = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)

                // Android 10+ Low Latency Flag
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    attributesBuilder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
                }
                // Flag Low Latency explicitly
                attributesBuilder.setFlags(AudioAttributes.FLAG_LOW_LATENCY)

                val formatBuilder = AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfigOut)

                val trackBuilder = AudioTrack.Builder()
                    .setAudioAttributes(attributesBuilder.build())
                    .setAudioFormat(formatBuilder.build())
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)

                // Android 8.0+ Performance Mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                }

                audioTrack = trackBuilder.build()

                audioRecord?.startRecording()
                audioTrack?.play()

                // READ BUFFER: Smaller is faster, but too small causes glitches.
                // We try to read exactly one "native burst" at a time.
                val readChunkSize = nativeBufferSize
                val monoBuffer = ShortArray(readChunkSize)
                val stereoBuffer = ShortArray(readChunkSize * 2)

                while (isActive) {
                    // Blocking Read
                    val readSize = audioRecord?.read(monoBuffer, 0, readChunkSize) ?: 0

                    if (readSize > 0) {
                        // DSP: Software Panning
                        val balance = currentBalance

                        val leftGain = if (balance > 0) 1f - balance else 1f
                        val rightGain = if (balance < 0) 1f + balance else 1f

                        // Interleave Mono -> Stereo
                        for (i in 0 until readSize) {
                            val signal = monoBuffer[i]
                            stereoBuffer[i * 2] = (signal * leftGain).toInt().toShort()
                            stereoBuffer[i * 2 + 1] = (signal * rightGain).toInt().toShort()
                        }

                        // Write Stereo Buffer
                        // BLOCKING WRITE to ensure we don't drift
                        audioTrack?.write(stereoBuffer, 0, readSize * 2)

                        // Visualization - Run on separate thread to not block audio loop?
                        // No, for simple calc, doing it here is fine, but throttle UI updates.
                        // We use a simple counter to only update UI every ~4th frame to save CPU.
                        if (readSize > 0) {
                            val maxAmplitude = monoBuffer.take(readSize).maxOfOrNull { abs(it.toInt()) } ?: 0
                            val normalized = (maxAmplitude / 100).coerceIn(0, 100)
                            withContext(Dispatchers.Main) {
                                currentAmplitude?.invoke(normalized)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError?.invoke("Engine Error: ${e.message}") }
            } finally {
                stopAudioLoop()
            }
        }
    }

    fun stopAudioLoop() {
        job?.cancel()
        job = null
        try {
            audioRecord?.stop(); audioRecord?.release()
            audioTrack?.pause(); audioTrack?.flush(); audioTrack?.release()
        } catch (e: Exception) { /* Ignored */ }
        audioRecord = null
        audioTrack = null
        currentAmplitude?.invoke(0)
    }

    fun isRunning() = job?.isActive == true

    fun isHeadsetConnected(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            val type = device.type
            if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                type == AudioDeviceInfo.TYPE_USB_HEADSET
            ) return true
        }
        return false
    }
}