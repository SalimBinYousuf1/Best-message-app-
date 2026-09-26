package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.preferences.NotificationPrivacy
import com.example.data.local.preferences.SalimPreferences
import com.example.data.local.preferences.SalimUserPreferences
import com.example.data.local.preferences.ThemeMode
import com.example.telephony.DefaultSmsRoleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: SalimUserPreferences = SalimUserPreferences(),
    val isDefaultSmsApp: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences: SalimPreferences = (application as SalimApplication).preferences
    private val _isDefaultSms = MutableStateFlow(DefaultSmsRoleManager.isDefaultSmsApp(application))

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.preferencesFlow,
        _isDefaultSms
    ) { prefs, isDefault ->
        SettingsUiState(preferences = prefs, isDefaultSmsApp = isDefault)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun refreshDefaultSms() {
        _isDefaultSms.value = DefaultSmsRoleManager.isDefaultSmsApp(getApplication())
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { preferences.setDynamicColor(enabled) }
    }

    fun setSendOnEnter(enabled: Boolean) {
        viewModelScope.launch { preferences.setSendOnEnter(enabled) }
    }

    fun setAutoDetectOtp(enabled: Boolean) {
        viewModelScope.launch { preferences.setAutoDetectOtp(enabled) }
    }

    fun setShowDeliveryReports(enabled: Boolean) {
        viewModelScope.launch { preferences.setShowDeliveryReports(enabled) }
    }

    fun setShowCharCounter(enabled: Boolean) {
        viewModelScope.launch { preferences.setShowCharCounter(enabled) }
    }

    fun setNotificationPrivacy(privacy: NotificationPrivacy) {
        viewModelScope.launch { preferences.setNotificationPrivacy(privacy) }
    }

    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch { preferences.setAppLockEnabled(enabled) }
    }

    fun setBubbleTextScale(scale: Float) {
        viewModelScope.launch { preferences.setBubbleTextScale(scale) }
    }

    fun setGlassIntensity(intensity: Float) {
        viewModelScope.launch { preferences.setGlassIntensity(intensity) }
    }
}
