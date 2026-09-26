package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.preferences.NotificationPrivacy
import com.example.data.local.preferences.ThemeMode
import com.example.telephony.DefaultSmsRoleManager
import com.example.ui.components.LiquidGlassTopBar
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.liquidGlass

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val defaultSmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshDefaultSms()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LiquidGlassTopBar(
                title = "Settings",
                subtitle = "Preferences & Privacy",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Telephony & Default App
            SettingsSectionHeader(title = "SYSTEM INTEGRATION")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(shape = RoundedCornerShape(18.dp), elevation = 2.dp)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = if (uiState.isDefaultSmsApp) StatusSuccess else StatusWarning
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Default SMS App",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (uiState.isDefaultSmsApp) "Salim handles device SMS/MMS" else "Tap to configure default handler",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!uiState.isDefaultSmsApp) {
                            Button(
                                onClick = {
                                    val intent = DefaultSmsRoleManager.createDefaultSmsIntent(context)
                                    if (intent != null) defaultSmsLauncher.launch(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Change", fontSize = 12.sp)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = StatusSuccess
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = SalimBlue
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App Permissions in System",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Manage SMS, Contacts, Notifications & Alarms in Android settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section: Appearance
            SettingsSectionHeader(title = "APPEARANCE")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(shape = RoundedCornerShape(18.dp), elevation = 2.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            ThemeMode.SYSTEM to "System",
                            ThemeMode.LIGHT to "Light",
                            ThemeMode.DARK to "Dark",
                            ThemeMode.OLED to "OLED"
                        ).forEach { (mode, label) ->
                            val isSelected = uiState.preferences.themeMode == mode
                            Button(
                                onClick = { viewModel.setThemeMode(mode) },
                                colors = if (isSelected) {
                                    ButtonDefaults.buttonColors(containerColor = SalimBlue)
                                } else {
                                    ButtonDefaults.outlinedButtonColors()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }

                    HorizontalDivider()

                    SettingsToggleRow(
                        title = "Dynamic Material Color",
                        subtitle = "Harmonize with system wallpaper on Android 12+",
                        checked = uiState.preferences.dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) }
                    )
                }
            }

            // Section: Messaging
            SettingsSectionHeader(title = "MESSAGING CONTROLS")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(shape = RoundedCornerShape(18.dp), elevation = 2.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsToggleRow(
                        title = "Delivery Reports",
                        subtitle = "Request SMS delivery confirmation from carrier",
                        checked = uiState.preferences.showDeliveryReports,
                        onCheckedChange = { viewModel.setShowDeliveryReports(it) }
                    )

                    HorizontalDivider()

                    SettingsToggleRow(
                        title = "Send on Enter",
                        subtitle = "Pressing Enter sends the message immediately",
                        checked = uiState.preferences.sendOnEnter,
                        onCheckedChange = { viewModel.setSendOnEnter(it) }
                    )

                    HorizontalDivider()

                    SettingsToggleRow(
                        title = "SMS Character & Part Counter",
                        subtitle = "Show segment limit and remaining characters while typing",
                        checked = uiState.preferences.showCharCounter,
                        onCheckedChange = { viewModel.setShowCharCounter(it) }
                    )
                }
            }

            // Section: Privacy & Security
            SettingsSectionHeader(title = "PRIVACY & SECURITY")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(shape = RoundedCornerShape(18.dp), elevation = 2.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsToggleRow(
                        title = "Local OTP Detection",
                        subtitle = "Private local regex detector with 1-tap copy button",
                        checked = uiState.preferences.autoDetectOtp,
                        onCheckedChange = { viewModel.setAutoDetectOtp(it) }
                    )

                    HorizontalDivider()

                    Text(
                        text = "Notification Preview on Lock Screen",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            NotificationPrivacy.SHOW_ALL to "Full",
                            NotificationPrivacy.SENDER_ONLY to "Sender",
                            NotificationPrivacy.HIDE_ALL to "Hidden"
                        ).forEach { (privacy, label) ->
                            val isSelected = uiState.preferences.notificationPrivacy == privacy
                            Button(
                                onClick = { viewModel.setNotificationPrivacy(privacy) },
                                colors = if (isSelected) {
                                    ButtonDefaults.buttonColors(containerColor = SalimBlue)
                                } else {
                                    ButtonDefaults.outlinedButtonColors()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }

                    HorizontalDivider()

                    SettingsToggleRow(
                        title = "App Lock",
                        subtitle = "Require biometric or passcode before viewing messages",
                        checked = uiState.preferences.appLockEnabled,
                        onCheckedChange = { viewModel.setAppLock(it) }
                    )
                }
            }

            // Section: About
            SettingsSectionHeader(title = "ABOUT SALIM")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(shape = RoundedCornerShape(18.dp), elevation = 2.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Salim Messaging",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version 1.0 (Build 1) • Production Native Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Private by default: All SMS and conversation records are stored in local device SQLite/Room database. No tracking or telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                checkedTrackColor = SalimBlue
            )
        )
    }
}
