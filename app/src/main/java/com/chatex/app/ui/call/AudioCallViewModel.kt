package com.chatex.app.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatex.app.data.model.CallUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AudioCallViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private var callTimerJob: Job? = null
    private var callStartTime: Long = 0L

    init {
        startCallTimer()
    }

    /**
     * بدء عداد مدة المكالمة
     */
    private fun startCallTimer() {
        callStartTime = System.currentTimeMillis()
        
        callTimerJob = viewModelScope.launch {
            while (true) {
                val elapsedTime = System.currentTimeMillis() - callStartTime
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
                
                _uiState.update { currentState ->
                    currentState.copy(
                        callDuration = String.format("%02d:%02d", minutes, seconds)
                    )
                }
                
                delay(1000) // تحديث كل ثانية
            }
        }
    }

    /**
     * تبديل حالة كتم الصوت
     */
    fun toggleMute() {
        _uiState.update { currentState ->
            currentState.copy(isMuted = !currentState.isMuted)
        }
    }

    /**
     * تبديل حالة مكبر الصوت
     */
    fun toggleSpeaker() {
        _uiState.update { currentState ->
            currentState.copy(isSpeakerOn = !currentState.isSpeakerOn)
        }
    }

    /**
     * إضافة مكالمة أخرى
     */
    fun addCall() {
        // TODO: تنفيذ إضافة مكالمة أخرى
    }

    /**
     * إنهاء المكالمة
     */
    fun endCall() {
        callTimerJob?.cancel()
        // TODO: تنفيذ إنهاء المكالمة
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
    }
}
