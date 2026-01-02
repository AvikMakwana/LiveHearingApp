package com.avikmakwana.livehearingapp.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope // Import this
import com.avikmakwana.livehearingapp.domain.AudioEngine
import com.avikmakwana.livehearingapp.service.AudioForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HearingViewModel @Inject constructor(
    private val app: Application,
    private val audioEngine: AudioEngine
) : AndroidViewModel(app) {

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    val amplitude = _amplitude.asStateFlow()

    // New: Balance State (-1.0 to 1.0)
    private val _balance = MutableStateFlow(0f)
    val balance = _balance.asStateFlow()

    init {
        _isListening.value = audioEngine.isRunning()
        _balance.value = audioEngine.currentBalance // Sync if service running

        audioEngine.currentAmplitude = { amp -> _amplitude.value = amp }

        // Listen to error callbacks if needed (omitted for brevity)
    }

    fun toggleListening() {
        val intent = Intent(app, AudioForegroundService::class.java)
        if (_isListening.value) {
            intent.action = AudioForegroundService.ACTION_STOP
            _isListening.value = false
        } else {
            intent.action = AudioForegroundService.ACTION_START
            _isListening.value = true
        }
        app.startService(intent)
    }

    fun updateBalance(newBalance: Float) {
        _balance.value = newBalance
        audioEngine.currentBalance = newBalance
    }
}