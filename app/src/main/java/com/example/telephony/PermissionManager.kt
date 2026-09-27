package com.example.telephony

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionManager {
    private const val PREFS_NAME = "salim_permission_prefs"
    private const val KEY_COMPLETED = "has_completed_permission_flow"
    private const val KEY_PERM_PREFIX = "perm_state_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks synchronously if the onboarding permission flow was already completed.
     * Survives process death, app restarts, and recompositions.
     */
    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = getPrefs(context)
        val saved = prefs.getBoolean(KEY_COMPLETED, false)
        if (saved) return true

        // If user already has core SMS permission granted by the system, treat as completed
        val hasSms = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        return hasSms
    }

    /**
     * Marks the onboarding permission process as permanently completed.
     */
    fun setOnboardingCompleted(context: Context, completed: Boolean = true) {
        getPrefs(context).edit().putBoolean(KEY_COMPLETED, completed).commit()
    }

    /**
     * Persists the state of an individual permission.
     */
    fun recordPermissionState(context: Context, permission: String, granted: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PERM_PREFIX + permission, granted).commit()
    }

    /**
     * Retrieves the persisted state of an individual permission.
     */
    fun getRecordedPermissionState(context: Context, permission: String): Boolean {
        return getPrefs(context).getBoolean(KEY_PERM_PREFIX + permission, false)
    }

    /**
     * Opens Android System Application Settings for recovery when a permission is permanently denied.
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
