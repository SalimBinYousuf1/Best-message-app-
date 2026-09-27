package com.example.ui.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.QuickReplyTemplateEntity
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassButtonStyle
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass

data class QuickTemplateItem(
    val title: String,
    val content: String,
    val isCustom: Boolean = false,
    val id: Long = 0L
)

val PREDEFINED_TEMPLATES = listOf(
    QuickTemplateItem("On my way", "I'm on my way!"),
    QuickTemplateItem("Can't talk", "Can't talk right now. What's up?"),
    QuickTemplateItem("Call right back", "I'll call you right back."),
    QuickTemplateItem("Sounds good", "Sounds good, thanks!"),
    QuickTemplateItem("Running late", "Running a few minutes late, see you soon.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickReplyTemplatesSheet(
    templates: List<QuickReplyTemplateEntity>,
    onSelectTemplate: (String) -> Unit,
    onSaveCustomTemplate: ((String, String) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isWritingCustom by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var customContent by remember { mutableStateOf("") }
    var saveForFuture by remember { mutableStateOf(true) }

    // Combine custom saved templates from database with predefined templates
    val combinedList = remember(templates) {
        val userItems = templates.map {
            QuickTemplateItem(it.title, it.content, isCustom = true, id = it.id)
        }
        val existingContents = userItems.map { it.content.trim().lowercase() }.toSet()
        val uniquePredefined = PREDEFINED_TEMPLATES.filter {
            !existingContents.contains(it.content.trim().lowercase())
        }
        userItems + uniquePredefined
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = SalimBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Templates",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${combinedList.size} available",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PERSISTENT Custom Message Button: Always visible and persistent at top!
            LiquidGlassButton(
                onClick = { isWritingCustom = !isWritingCustom },
                style = if (isWritingCustom) LiquidGlassButtonStyle.SECONDARY else LiquidGlassButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isWritingCustom) Icons.Default.TextSnippet else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isWritingCustom) SalimBlue else Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isWritingCustom) "Hide Custom Editor" else "+ Write Custom Quick Message",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWritingCustom) MaterialTheme.colorScheme.onSurface else Color.White,
                    fontSize = 14.sp
                )
            }

            // Inline Custom Composer Card
            AnimatedVisibility(
                visible = isWritingCustom,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .liquidGlass(shape = RoundedCornerShape(16.dp), elevation = 3.dp)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Create Quick Message",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Title / Shortcut (optional)") },
                        placeholder = { Text("e.g. On Vacation") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customContent,
                        onValueChange = { customContent = it },
                        label = { Text("Message text *") },
                        placeholder = { Text("Type custom quick reply text...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = saveForFuture,
                            onCheckedChange = { saveForFuture = it },
                            colors = CheckboxDefaults.colors(checkedColor = SalimBlue)
                        )
                        Text(
                            text = "Save as permanent template for future use",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isWritingCustom = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trimmed = customContent.trim()
                                if (trimmed.isNotEmpty()) {
                                    if (saveForFuture) {
                                        val title = customTitle.trim().ifBlank {
                                            if (trimmed.length > 20) trimmed.take(20) + "…" else trimmed
                                        }
                                        onSaveCustomTemplate?.invoke(title, trimmed)
                                    }
                                    onSelectTemplate(trimmed)
                                    onDismiss()
                                }
                            },
                            enabled = customContent.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use & Insert")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Predefined and Custom Templates List
            Text(
                text = "TAP TO INSERT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                items(combinedList, key = { "${it.title}_${it.content}" }) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(shape = RoundedCornerShape(14.dp), elevation = 1.dp)
                            .clickable {
                                onSelectTemplate(item.content)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.isCustom) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .liquidGlass(shape = RoundedCornerShape(6.dp), elevation = 0.dp)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Custom",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = SalimBlue
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Insert",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
