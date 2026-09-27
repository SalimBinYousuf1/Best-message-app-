package com.example.ui.inbox

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ConversationEntity
import com.example.telephony.ContactResolver
import com.example.telephony.DefaultSmsRoleManager
import com.example.ui.components.ContactAvatar
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassButtonStyle
import com.example.ui.components.LiquidGlassSegmentedControl
import com.example.ui.components.LiquidGlassTopBar
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass

@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    onNavigateToConversation: (Long, String) -> Unit,
    onNavigateToCompose: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToScheduled: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Activity launcher for default SMS role request
    val defaultSmsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.refreshDefaultSmsStatus()
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.syncSms()
        }
    }

    // Permission launcher for SMS and Contacts
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.syncSms()
        viewModel.refreshDefaultSmsStatus()
    }

    LaunchedEffect(Unit) {
        viewModel.syncSms()
        viewModel.refreshDefaultSmsStatus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LiquidGlassTopBar(
                title = "Salim",
                subtitle = if (uiState.unreadCountTotal > 0) "${uiState.unreadCountTotal} unread" else "Messages",
                actions = {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("inbox_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNavigateToScheduled,
                        modifier = Modifier.testTag("inbox_scheduled_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Scheduled Messages",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("inbox_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCompose,
                containerColor = SalimBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
                    .testTag("compose_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Compose",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Default SMS App Banner (if not default)
            AnimatedVisibility(
                visible = !uiState.isDefaultSmsApp,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DefaultSmsNoticeCard(
                    onSetDefaultClick = {
                        val intent = DefaultSmsRoleManager.createDefaultSmsIntent(context)
                        if (intent != null) {
                            defaultSmsLauncher.launch(intent)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Filter Tabs (All, Unread, Starred, Archived) with sliding glass thumb
            LiquidGlassSegmentedControl(
                items = listOf(
                    InboxFilter.ALL to "All",
                    InboxFilter.UNREAD to "Unread",
                    InboxFilter.STARRED to "Pinned",
                    InboxFilter.ARCHIVED to "Archived"
                ),
                selectedItem = uiState.filter,
                onItemSelected = { viewModel.setFilter(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Pinned conversations row (if on ALL filter and pinned exists)
            if (uiState.filter == InboxFilter.ALL && uiState.pinnedConversations.isNotEmpty()) {
                PinnedConversationsRow(
                    pinnedList = uiState.pinnedConversations,
                    onConversationClick = { onNavigateToConversation(it.id, it.recipientAddress) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            // Conversations List or Empty State
            if (uiState.conversations.isEmpty()) {
                EmptyInboxView(
                    filter = uiState.filter,
                    onComposeClick = onNavigateToCompose,
                    onSyncClick = { viewModel.syncSms() },
                    isSyncing = uiState.isSyncing,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.conversations,
                        key = { it.id }
                    ) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = {
                                onNavigateToConversation(conversation.id, conversation.recipientAddress)
                            },
                            onTogglePin = { viewModel.togglePin(conversation) },
                            onToggleArchive = { viewModel.toggleArchive(conversation) },
                            onToggleMute = { viewModel.toggleMute(conversation) },
                            onDelete = { viewModel.deleteConversation(conversation.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultSmsNoticeCard(
    onSetDefaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(18.dp), elevation = 3.dp)
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SalimBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Make Salim your default messaging app",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "To receive, manage, and reply to SMS instantly without missing messages, set Salim as default.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            LiquidGlassButton(
                onClick = onSetDefaultClick,
                style = LiquidGlassButtonStyle.PRIMARY,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("set_default_sms_button")
            ) {
                Text("Set as Default App", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun PinnedConversationsRow(
    pinnedList: List<ConversationEntity>,
    onConversationClick: (ConversationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "PINNED",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pinnedList, key = { it.id }) { conversation ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onConversationClick(conversation) }
                        .width(64.dp)
                ) {
                    ContactAvatar(
                        name = conversation.recipientName,
                        address = conversation.recipientAddress,
                        size = 52.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = conversation.recipientName ?: conversation.recipientAddress,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyInboxView(
    filter: InboxFilter,
    onComposeClick: () -> Unit,
    onSyncClick: () -> Unit,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .liquidGlass(shape = CircleShape, elevation = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = SalimBlue,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (filter) {
                    InboxFilter.ALL -> "No messages yet"
                    InboxFilter.UNREAD -> "All caught up"
                    InboxFilter.STARRED -> "No pinned conversations"
                    InboxFilter.ARCHIVED -> "No archived messages"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (filter) {
                    InboxFilter.ALL -> "Start a clean, fast conversation or import SMS messages from your device."
                    InboxFilter.UNREAD -> "You have no unread SMS conversations."
                    InboxFilter.STARRED -> "Long-press any conversation to pin it here."
                    InboxFilter.ARCHIVED -> "Conversations you archive will be kept quietly here."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (filter == InboxFilter.ALL) {
                    LiquidGlassButton(
                        onClick = onComposeClick,
                        style = LiquidGlassButtonStyle.PRIMARY
                    ) {
                        Text("Start conversation", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    LiquidGlassButton(
                        onClick = onSyncClick,
                        enabled = !isSyncing,
                        style = LiquidGlassButtonStyle.SECONDARY
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = SalimBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSyncing) "Syncing..." else "Import SMS", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
