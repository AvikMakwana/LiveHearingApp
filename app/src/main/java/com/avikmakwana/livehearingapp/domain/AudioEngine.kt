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

@Singleton
class AudioEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ... (Previous variables remain the same) ...
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var job: Job? = null

    // Config
    private val sampleRate = 44100
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)

    var currentAmplitude: ((Int) -> Unit)? = null

    // NEW: Error callback for UI to show "Connect Headphones" toast
    var onError: ((String) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startAudioLoop() {
        if (job?.isActive == true) return

        // SAFETY CHECK: Headphones Only!
        if (!isHeadsetConnected()) {
            onError?.invoke("Please connect WeHear device or headphones to avoid feedback noise.")
            return
        }

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                // ... (Setup AudioRecord & AudioTrack same as before) ...
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfigIn, audioFormat, minBufSize * 2)

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
                            .setChannelMask(channelConfigOut)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioRecord?.startRecording()
                audioTrack?.play()

                val buffer = ShortArray(minBufSize)

                while (isActive) {
                    val readSize = audioRecord?.read(buffer, 0, minBufSize) ?: 0
                    if (readSize > 0) {
                        audioTrack?.write(buffer, 0, readSize)

                        // Amplitude calculation
                        val maxAmplitude = buffer.take(readSize).maxOfOrNull { kotlin.math.abs(it.toInt()) } ?: 0
                        // Smoothed normalization for better UI ripple
                        val normalized = (maxAmplitude / 100).coerceIn(0, 100)

                        withContext(Dispatchers.Main) {
                            currentAmplitude?.invoke(normalized)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError?.invoke("Audio Error: ${e.message}") }
            } finally {
                stopAudioLoop()
            }
        }
    }

    fun stopAudioLoop() {
        // ... (Cleanup code same as before) ...
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

    // HELPER: Detects wired or bluetooth headsets
    private fun isHeadsetConnected(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            val type = device.type
            if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                type == AudioDeviceInfo.TYPE_USB_HEADSET
            ) {
                return true
            }
        }
        return false
    }
}