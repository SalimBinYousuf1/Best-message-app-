package com.example.ui.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telephony.DefaultSmsRoleManager
import com.example.telephony.PermissionManager
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassButtonStyle
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.liquidGlass

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDefaultSms by remember { mutableStateOf(DefaultSmsRoleManager.isDefaultSmsApp(context)) }
    var permissionsRequested by remember { mutableStateOf(false) }

    val defaultSmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultSms = DefaultSmsRoleManager.isDefaultSmsApp(context)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsRequested = true
        results.forEach { (perm, granted) ->
            PermissionManager.recordPermissionState(context, perm, granted)
        }
    }

    fun completeAndProceed() {
        PermissionManager.setOnboardingCompleted(context, true)
        onCompleteOnboarding()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Hero Brand & Icon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .liquidGlass(shape = CircleShape, elevation = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Salim icon",
                        tint = SalimBlue,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome to Salim",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A calm, fast, liquid-glass messaging experience for Android.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Benefits cards
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OnboardingFeatureRow(
                    icon = Icons.Default.Speed,
                    title = "Instant & Fluid",
                    description = "Zero lag, smooth native Compose animations, and persistent draft recovery."
                )

                OnboardingFeatureRow(
                    icon = Icons.Default.Lock,
                    title = "Private by Default",
                    description = "All messages and conversations are stored strictly on your device in local SQLite."
                )

                OnboardingFeatureRow(
                    icon = Icons.Default.Message,
                    title = "Reliable SMS & MMS",
                    description = "Native telephony integration with smart OTP detection and scheduled dispatches."
                )
            }

            // Action section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isDefaultSms) {
                    LiquidGlassButton(
                        onClick = {
                            val intent = DefaultSmsRoleManager.createDefaultSmsIntent(context)
                            if (intent != null) {
                                defaultSmsLauncher.launch(intent)
                            }
                            permissionsLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_SMS,
                                    android.Manifest.permission.RECEIVE_SMS,
                                    android.Manifest.permission.SEND_SMS,
                                    android.Manifest.permission.READ_CONTACTS
                                )
                            )
                        },
                        style = LiquidGlassButtonStyle.PRIMARY,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Set as Default SMS App",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LiquidGlassButton(
                        onClick = { completeAndProceed() },
                        style = LiquidGlassButtonStyle.SECONDARY,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Continue without default role", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    LiquidGlassButton(
                        onClick = { completeAndProceed() },
                        style = LiquidGlassButtonStyle.PRIMARY,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Open Inbox",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(18.dp), elevation = 2.dp)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .liquidGlass(shape = CircleShape, elevation = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SalimBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
