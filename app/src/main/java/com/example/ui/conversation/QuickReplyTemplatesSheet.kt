package com.example.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.QuickReplyTemplateEntity
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickReplyTemplatesSheet(
    templates: List<QuickReplyTemplateEntity>,
    onSelectTemplate: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    var isAddingNew by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TextSnippet,
                        contentDescription = null,
                        tint = SalimBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "Quick Templates",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isAddingNew) {
                    Button(
                        onClick = { isAddingNew = true },
                        colors = ButtonDefaults.outlinedButtonColors(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New template",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isAddingNew) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                        .padding(14.dp)
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title (e.g. In a meeting)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Message text") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { isAddingNew = false },
                            colors = ButtonDefaults.textButtonColors()
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (newContent.isNotBlank()) {
                                    onSelectTemplate(newContent.trim())
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Use")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    items(templates, key = { it.id }) { template ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlass(shape = RoundedCornerShape(14.dp), elevation = 1.dp)
                                .clickable {
                                    onSelectTemplate(template.content)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column {
                                Text(
                                    text = template.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = template.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
