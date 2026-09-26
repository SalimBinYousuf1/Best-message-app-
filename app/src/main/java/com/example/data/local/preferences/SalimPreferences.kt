package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    OLED
}

enum class NotificationPrivacy {
    SHOW_ALL,
    SENDER_ONLY,
    HIDE_ALL
}

data class SalimUserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val sendOnEnter: Boolean = false,
    val autoDetectOtp: Boolean = true,
    val showDeliveryReports: Boolean = true,
    val showCharCounter: Boolean = true,
    val notificationPrivacy: NotificationPrivacy = NotificationPrivacy.SHOW_ALL,
    val hapticFeedback: Boolean = true,
    val appLockEnabled: Boolean = false,
    val hasCompletedOnboarding: Boolean = false
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salim_settings")

class SalimPreferences(private val context: Context) {

    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    private val KEY_SEND_ON_ENTER = booleanPreferencesKey("send_on_enter")
    private val KEY_AUTO_DETECT_OTP = booleanPreferencesKey("auto_detect_otp")
    private val KEY_SHOW_DELIVERY_REPORTS = booleanPreferencesKey("show_delivery_reports")
    private val KEY_SHOW_CHAR_COUNTER = booleanPreferencesKey("show_char_counter")
    private val KEY_NOTIFICATION_PRIVACY = stringPreferencesKey("notification_privacy")
    private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    private val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

    val preferencesFlow: Flow<SalimUserPreferences> = context.dataStore.data.map { prefs ->
        SalimUserPreferences(
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
            sendOnEnter = prefs[KEY_SEND_ON_ENTER] ?: false,
            autoDetectOtp = prefs[KEY_AUTO_DETECT_OTP] ?: true,
            showDeliveryReports = prefs[KEY_SHOW_DELIVERY_REPORTS] ?: true,
            showCharCounter = prefs[KEY_SHOW_CHAR_COUNTER] ?: true,
            notificationPrivacy = runCatching {
                NotificationPrivacy.valueOf(prefs[KEY_NOTIFICATION_PRIVACY] ?: NotificationPrivacy.SHOW_ALL.name)
            }.getOrDefault(NotificationPrivacy.SHOW_ALL),
            hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
            appLockEnabled = prefs[KEY_APP_LOCK_ENABLED] ?: false,
            hasCompletedOnboarding = prefs[KEY_HAS_COMPLETED_ONBOARDING] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setSendOnEnter(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SEND_ON_ENTER] = enabled }
    }

    suspend fun setAutoDetectOtp(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_DETECT_OTP] = enabled }
    }

    suspend fun setShowDeliveryReports(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_DELIVERY_REPORTS] = enabled }
    }

    suspend fun setShowCharCounter(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_CHAR_COUNTER] = enabled }
    }

    suspend fun setNotificationPrivacy(privacy: NotificationPrivacy) {
        context.dataStore.edit { it[KEY_NOTIFICATION_PRIVACY] = privacy.name }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { it[KEY_HAS_COMPLETED_ONBOARDING] = completed }
    }
}
