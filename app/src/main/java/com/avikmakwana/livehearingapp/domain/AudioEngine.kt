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

    // Config
    private val sampleRate = 44100
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    // CHANGED: Output must be STEREO to control Left/Right independently
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    // Buffers
    private val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)

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
                // 1. Setup Input (Mono Mic)
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfigIn, audioFormat, minBufSize * 2)

                // 2. Setup Output (Stereo Playback)
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfigOut) // STEREO
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize * 4) // Larger buffer for stereo
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioRecord?.startRecording()
                audioTrack?.play()

                val monoBuffer = ShortArray(minBufSize)
                // Stereo buffer is 2x size (L + R for each sample)
                val stereoBuffer = ShortArray(minBufSize * 2)

                while (isActive) {
                    val readSize = audioRecord?.read(monoBuffer, 0, minBufSize) ?: 0

                    if (readSize > 0) {
                        // DSP: Software Panning
                        val balance = currentBalance // Capture volatile read

                        // Calculate Gains (Linear Panning)
                        // If balance is -1 (Left), R=0. If balance is 1 (Right), L=0.
                        val leftGain = if (balance > 0) 1f - balance else 1f
                        val rightGain = if (balance < 0) 1f + balance else 1f

                        // Interleave Mono -> Stereo
                        for (i in 0 until readSize) {
                            val signal = monoBuffer[i]
                            stereoBuffer[i * 2] = (signal * leftGain).toInt().toShort()     // LEFT
                            stereoBuffer[i * 2 + 1] = (signal * rightGain).toInt().toShort() // RIGHT
                        }

                        // Write Stereo Buffer
                        audioTrack?.write(stereoBuffer, 0, readSize * 2)

                        // Visualization (use max amplitude of raw input)
                        val maxAmplitude = monoBuffer.take(readSize).maxOfOrNull { abs(it.toInt()) } ?: 0
                        val normalized = (maxAmplitude / 100).coerceIn(0, 100)

                        withContext(Dispatchers.Main) {
                            currentAmplitude?.invoke(normalized)
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
            audioTrack?.stop(); audioTrack?.release()
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