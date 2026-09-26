package com.example.ui.compose

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.telephony.ContactInfo
import com.example.telephony.ContactResolver
import com.example.ui.components.ContactAvatar
import com.example.ui.components.LiquidGlassTopBar
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass
import kotlinx.coroutines.launch

@Composable
fun ComposeScreen(
    viewModel: ComposeViewModel,
    onNavigateToConversation: (Long, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LiquidGlassTopBar(
                title = "New Message",
                subtitle = "Choose recipient",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Recipient search field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .liquidGlass(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    placeholder = { Text("Type name or phone number") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SalimBlue
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("compose_search_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // If user typed a raw phone number, offer direct send option
            val normalizedInput = ContactResolver.normalizePhoneNumber(uiState.query)
            if (normalizedInput.length >= 3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .liquidGlass(shape = RoundedCornerShape(14.dp), elevation = 1.dp)
                        .clickable {
                            coroutineScope.launch {
                                val convId = viewModel.getOrCreateConversationId(normalizedInput, null)
                                onNavigateToConversation(convId, normalizedInput)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = null,
                            tint = SalimBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Send to $normalizedInput",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Direct SMS to this number",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Contacts List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(uiState.contacts) { contact ->
                    ContactRowItem(
                        contact = contact,
                        onClick = {
                            coroutineScope.launch {
                                val convId = viewModel.getOrCreateConversationId(
                                    contact.normalizedNumber,
                                    contact.name
                                )
                                onNavigateToConversation(convId, contact.normalizedNumber)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ContactRowItem(
    contact: ContactInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(16.dp), elevation = 1.dp)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ContactAvatar(
                name = contact.name,
                address = contact.normalizedNumber,
                photoUri = contact.photoUri,
                size = 44.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = contact.name ?: contact.normalizedNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (contact.name != null) {
                    Text(
                        text = contact.normalizedNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
