package com.example.ui.conversation

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.text.format.DateUtils
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.MessageEntity
import com.example.data.local.preferences.ThemeMode
import com.example.ui.components.ContactAvatar
import com.example.ui.components.DateSeparator
import com.example.ui.components.OtpBanner
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimCanvasDark
import com.example.ui.theme.SalimCanvasLight
import com.example.ui.theme.liquidGlass
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val draftText by viewModel.draftText.collectAsStateWithLifecycle()
    val isInitialLoadDone by viewModel.isInitialLoadDone.collectAsStateWithLifecycle()

    val messages = uiState.messages

    // Direct, non-animated initial positioning strictly at the latest message
    val listState = remember(isInitialLoadDone && messages.isNotEmpty()) {
        LazyListState(
            firstVisibleItemIndex = if (messages.isNotEmpty()) messages.size - 1 else 0
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var previousMessageCount by rememberSaveable { mutableIntStateOf(0) }

    // Detect if user is at the latest message
    val isAtLatestMessage by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            if (total == 0) return@derivedStateOf true
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= total - 1
        }
    }

    // Scroll-to-latest button visible only when scrolled away from bottom
    val showScrollToLatest by remember {
        derivedStateOf {
            !isAtLatestMessage && messages.isNotEmpty()
        }
    }

    // New message arrival: auto-scroll only if user was already at the latest message
    LaunchedEffect(messages.size) {
        if (previousMessageCount > 0 && messages.size > previousMessageCount) {
            if (isAtLatestMessage) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
        previousMessageCount = messages.size
    }

    // Show error snackbar if error occurs
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    val displayName = uiState.contactInfo?.name ?: uiState.conversation?.recipientName ?: uiState.conversation?.recipientAddress ?: ""
    val themeMode = LocalThemeMode.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (themeMode == ThemeMode.SALIM) Color.Transparent else MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ConversationTopBar(
                title = displayName,
                subtitle = uiState.conversation?.recipientAddress ?: "",
                photoUri = uiState.contactInfo?.photoUri,
                onBackClick = onBackClick,
                onContactClick = {
                    val address = uiState.conversation?.recipientAddress ?: ""
                    if (address.isNotBlank()) {
                        try {
                            val contactIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.withAppendedPath(
                                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                                    Uri.encode(address)
                                )
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(contactIntent)
                        } catch (_: Exception) {
                            try {
                                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:$address")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(fallbackIntent)
                            } catch (_: Exception) {}
                        }
                    }
                },
                onCallClick = {
                    val address = uiState.conversation?.recipientAddress
                    if (!address.isNullOrBlank()) {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address"))
                        context.startActivity(dialIntent)
                    }
                }
            )
        },
        bottomBar = {
            ComposerDock(
                text = draftText,
                onTextChanged = { viewModel.onDraftChanged(it) },
                onSend = {
                    viewModel.sendMessage(draftText)
                },
                onOpenAttachments = { showAttachmentSheet = true },
                onOpenTemplates = { showTemplatesSheet = true },
                onOpenSchedule = { showScheduleDialog = true },
                showCharCounter = uiState.preferences.showCharCounter,
                sendOnEnter = uiState.preferences.sendOnEnter
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // OTP Banner
                if (uiState.preferences.autoDetectOtp && uiState.detectedOtp != null) {
                    OtpBanner(
                        otpResult = uiState.detectedOtp,
                        onDismiss = { viewModel.dismissOtpBanner() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Messages List
                if (uiState.messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Start of conversation",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Messages to $displayName are dispatched as SMS or MMS through your carrier.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        var previousDate: String? = null

                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            val messageDate = getMessageDateString(message.timestamp)
                            if (messageDate != previousDate) {
                                DateSeparator(
                                    dateText = messageDate,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                previousDate = messageDate
                            }

                            MessageBubble(
                                message = message,
                                textScale = uiState.preferences.bubbleTextScale,
                                onRetry = { viewModel.retryMessage(message.id) },
                                onToggleStar = { viewModel.toggleStar(message) },
                                onSelectReaction = { emoji -> viewModel.toggleReaction(message, emoji) },
                                onDelete = { viewModel.deleteMessage(message.id) }
                            )
                        }
                    }
                }
            }

            // Apple-style Scroll to Latest floating glass button
            AnimatedVisibility(
                visible = showScrollToLatest,
                enter = fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.82f),
                exit = fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.82f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .liquidGlass(shape = CircleShape, elevation = 4.dp)
                        .clickable {
                            coroutineScope.launch {
                                if (uiState.messages.isNotEmpty()) {
                                    listState.animateScrollToItem(uiState.messages.size - 1)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to latest message",
                        tint = SalimBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // Attachment bottom sheet
    if (showAttachmentSheet) {
        AttachmentBottomSheet(
            onDismiss = { showAttachmentSheet = false },
            onAttachmentSelected = { uriString, type, name ->
                viewModel.sendMessage(
                    body = draftText,
                    attachmentUri = uriString,
                    attachmentType = type,
                    attachmentName = name
                )
            }
        )
    }

    // Quick templates bottom sheet
    if (showTemplatesSheet) {
        QuickReplyTemplatesSheet(
            templates = uiState.quickTemplates,
            onSelectTemplate = { templateText ->
                viewModel.onDraftChanged(templateText)
            },
            onSaveCustomTemplate = { title, content ->
                viewModel.saveTemplate(title, content)
            },
            onDismiss = { showTemplatesSheet = false }
        )
    }

    // Schedule message dialog
    if (showScheduleDialog) {
        ScheduleMessageDialog(
            initialText = draftText,
            onDismiss = { showScheduleDialog = false },
            onSchedule = { text, timestamp ->
                viewModel.scheduleMessage(text, timestamp)
            }
        )
    }
}

@Composable
private fun ConversationTopBar(
    title: String,
    subtitle: String,
    photoUri: String?,
    onBackClick: () -> Unit,
    onContactClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val isDark = LocalThemeIsDark.current
    val themeMode = LocalThemeMode.current
    val headerBaseColor = when (themeMode) {
        ThemeMode.SALIM -> Color.Transparent
        ThemeMode.LIGHT -> SalimCanvasLight
        ThemeMode.DARK -> SalimCanvasDark
        ThemeMode.SYSTEM -> if (isDark) SalimCanvasDark else SalimCanvasLight
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBaseColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .liquidGlass(shape = RoundedCornerShape(22.dp), elevation = 4.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("conversation_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(modifier = Modifier.clickable { onContactClick() }) {
                ContactAvatar(
                    name = title,
                    address = subtitle,
                    photoUri = photoUri,
                    size = 38.dp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onContactClick() }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank() && subtitle != title) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onCallClick,
                modifier = Modifier.testTag("conversation_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call $title",
                    tint = SalimBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun getMessageDateString(timestamp: Long): String {
    val now = System.currentTimeMillis()
    return when {
        DateUtils.isToday(timestamp) -> "Today"
        DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
