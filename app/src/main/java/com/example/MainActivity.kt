package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.data.local.preferences.SalimPreferences
import com.example.data.local.preferences.SalimUserPreferences
import com.example.security.AppLockManager
import com.example.telephony.ContactResolver
import com.example.telephony.PermissionManager
import com.example.ui.navigation.SalimNavGraph
import com.example.ui.navigation.Screen
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimTheme
import com.example.ui.theme.liquidGlass
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private lateinit var preferences: SalimPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = (application as SalimApplication).preferences
        val initialOnboardingDone = PermissionManager.isOnboardingCompleted(this)

        setContent {
            val userPrefs by preferences.preferencesFlow.collectAsStateWithLifecycle(
                initialValue = SalimUserPreferences()
            )

            var isUnlocked by remember { mutableStateOf(!userPrefs.appLockEnabled) }
            var showNoLockDialog by remember { mutableStateOf(false) }

            val lockManager = remember { AppLockManager(this@MainActivity) }

            // Trigger authentication when app lock is enabled and locked
            LaunchedEffect(userPrefs.appLockEnabled) {
                if (userPrefs.appLockEnabled && !isUnlocked) {
                    lockManager.authenticateUser(
                        onSuccess = { isUnlocked = true },
                        onError = { /* Keep locked */ },
                        onNeedsCustomPin = { showNoLockDialog = true }
                    )
                } else if (!userPrefs.appLockEnabled) {
                    isUnlocked = true
                }
            }

            SalimTheme(
                themeMode = userPrefs.themeMode,
                dynamicColor = userPrefs.dynamicColor
            ) {
                if (userPrefs.appLockEnabled && !isUnlocked) {
                    // Lock Screen UI
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .liquidGlass(shape = CircleShape, elevation = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = SalimBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Salim is Locked",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Use your device screen lock (fingerprint, face, pattern, or PIN) to access messages.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(28.dp))
                            Button(
                                onClick = {
                                    lockManager.authenticateUser(
                                        onSuccess = { isUnlocked = true },
                                        onError = { /* Retry on next click */ },
                                        onNeedsCustomPin = { showNoLockDialog = true }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Unlock App",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (showNoLockDialog) {
                        AlertDialog(
                            onDismissRequest = { showNoLockDialog = false },
                            title = { Text("Device Screen Lock Required") },
                            text = {
                                Text("No lock screen security (PIN, Pattern, Fingerprint, or Password) is configured on this device. Please set up a screen lock in Android Settings to secure your messages.")
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showNoLockDialog = false
                                        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SalimBlue)
                                ) {
                                    Text("Open Settings", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showNoLockDialog = false
                                    isUnlocked = true
                                }) {
                                    Text("Continue anyway")
                                }
                            }
                        )
                    }
                } else {
                    val navController = rememberNavController()
                    val startDestination = if (initialOnboardingDone || userPrefs.hasCompletedOnboarding) {
                        Screen.Inbox.route
                    } else {
                        Screen.Onboarding.route
                    }

                    // Handle incoming intents (notification tap, SENDTO sms intent)
                    LaunchedEffect(intent) {
                        handleIntent(intent) { convId, address ->
                            navController.navigate(Screen.Conversation.createRoute(convId, address))
                        }
                    }

                    SalimNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        onCompleteOnboarding = {
                            PermissionManager.setOnboardingCompleted(this@MainActivity, true)
                            lifecycleScope.launch {
                                preferences.setHasCompletedOnboarding(true)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIntent(intent: Intent?, onNavigateToConv: (Long, String) -> Unit) {
        if (intent == null) return

        // Direct notification extra
        val convId = intent.getLongExtra("conversation_id", -1L)
        val address = intent.getStringExtra("recipient_address")
        if (convId != -1L && !address.isNullOrBlank()) {
            onNavigateToConv(convId, address)
            return
        }

        // SENDTO scheme: sms: or smsto:
        if (intent.action == Intent.ACTION_SENDTO || intent.action == Intent.ACTION_SEND) {
            val data: Uri? = intent.data
            val recipient = data?.schemeSpecificPart
            if (!recipient.isNullOrBlank()) {
                val cleanAddress = ContactResolver.normalizePhoneNumber(recipient)
                lifecycleScope.launch {
                    val app = application as SalimApplication
                    val id = app.conversationRepository.getOrCreateConversation(cleanAddress, null)
                    onNavigateToConv(id, cleanAddress)
                }
            }
        }
    }
}
