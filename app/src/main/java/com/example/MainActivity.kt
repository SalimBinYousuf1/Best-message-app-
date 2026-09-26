package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.data.local.preferences.SalimPreferences
import com.example.data.local.preferences.SalimUserPreferences
import com.example.telephony.ContactResolver
import com.example.ui.navigation.SalimNavGraph
import com.example.ui.navigation.Screen
import com.example.ui.theme.SalimTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferences: SalimPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = (application as SalimApplication).preferences

        setContent {
            val userPrefs by preferences.preferencesFlow.collectAsStateWithLifecycle(
                initialValue = SalimUserPreferences()
            )

            SalimTheme(
                themeMode = userPrefs.themeMode,
                dynamicColor = userPrefs.dynamicColor
            ) {
                val navController = rememberNavController()
                val startDestination = if (userPrefs.hasCompletedOnboarding) {
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
                        lifecycleScope.launch {
                            preferences.setHasCompletedOnboarding(true)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
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
