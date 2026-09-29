package com.example.security

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class AppLockManager(private val activity: FragmentActivity) {

    fun authenticateUser(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onNeedsCustomPin: () -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)

        // Combine biometrics (Fingerprint/Face) + System Lock Credentials (PIN/Pattern/Password)
        val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Device lock screen / biometrics are available — launch system prompt
                showBiometricPrompt(authenticators, onSuccess, onError)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                // Phone has NO lock screen set up — divert to setup / custom lock screen
                onNeedsCustomPin()
            }
            else -> {
                onError("Authentication unavailable on this device.")
            }
        }
    }

    private fun showBiometricPrompt(
        authenticators: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        // Do NOT set setNegativeButtonText() when using DEVICE_CREDENTIAL
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Salim")
            .setSubtitle("Use your phone lock screen or biometrics to proceed")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
