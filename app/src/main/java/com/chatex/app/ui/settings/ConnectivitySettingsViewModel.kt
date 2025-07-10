package com.chatex.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatex.app.data.model.ConnectivitySettingsState
import com.chatex.app.data.model.NetworkStatus
import com.chatex.app.data.model.PerformanceMode
import com.chatex.app.data.repository.ConnectivitySettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectivitySettingsViewModel @Inject constructor(
    private val repository: ConnectivitySettingsRepository
) : ViewModel() {

    // حالة واجهة المستخدم
    private val _uiState = MutableStateFlow(ConnectivitySettingsState())
    val uiState: StateFlow<ConnectivitySettingsState> = _uiState

    init {
        // جمع التحديثات من المستودع
        viewModelScope.launch {
            repository.connectivitySettingsFlow.collect { settings ->
                _uiState.update { settings }
                // تحديث حالة الشبكة وعدد الأقران
                updateNetworkStatus()
            }
        }
    }

    // تبديل حالة المشاركة في شبكة النقل
    fun toggleRelayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateRelayEnabled(enabled)
            // تحديث حالة الشبكة بناءً على التغيير
            updateNetworkStatus()
        }
    }

    // تحديث وضع الأداء
    fun updatePerformanceMode(mode: PerformanceMode) {
        viewModelScope.launch {
            repository.updatePerformanceMode(mode)
        }
    }

    // تحديث حالة الشبكة وعدد الأقران
    private fun updateNetworkStatus() {
        val currentState = _uiState.value
        
        // محاكاة تحديث حالة الشبكة بناءً على الإعدادات الحالية
        val newStatus = when (currentState.networkStatus) {
            NetworkStatus.OFFLINE -> 
                if (currentState.isRelayEnabled) NetworkStatus.SEARCHING 
                else NetworkStatus.OFFLINE
            NetworkStatus.SEARCHING -> 
                if (currentState.peerCount > 0) NetworkStatus.RELAY 
                else NetworkStatus.ONLINE
            NetworkStatus.RELAY -> 
                if (!currentState.isRelayEnabled) NetworkStatus.ONLINE 
                else NetworkStatus.RELAY
            NetworkStatus.ONLINE -> 
                if (!currentState.isRelayEnabled) NetworkStatus.ONLINE 
                else NetworkStatus.SEARCHING
        }

        // محاكاة تحديث عدد الأقران المتصلين
        val newPeerCount = when (newStatus) {
            NetworkStatus.RELAY -> (1..10).random() // عدد عشوائي من الأقران
            else -> 0
        }

        _uiState.update { current ->
            current.copy(
                networkStatus = newStatus,
                peerCount = newPeerCount
            )
        }
    }

    // مزامنة الإعدادات مع Supabase
    fun syncWithSupabase(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(networkStatus = NetworkStatus.SEARCHING) }
                repository.syncWithSupabase(userId)
                _uiState.update { it.copy(networkStatus = NetworkStatus.ONLINE) }
            } catch (e: Exception) {
                _uiState.update { it.copy(networkStatus = NetworkStatus.OFFLINE) }
                // يمكن إضافة معالجة الخطأ المناسبة هنا
            }
        }
    }
}
