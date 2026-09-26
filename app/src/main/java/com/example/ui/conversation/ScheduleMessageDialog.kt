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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass
import java.util.Calendar

@Composable
fun ScheduleMessageDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSchedule: (String, Long) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var selectedTimestamp by remember {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 30) // Default 30 mins
        }
        mutableStateOf(cal.timeInMillis)
    }

    val presets = remember {
        listOf(
            "In 15 minutes" to 15 * 60 * 1000L,
            "In 1 hour" to 60 * 60 * 1000L,
            "In 3 hours" to 3 * 60 * 60 * 1000L,
            "Tomorrow 9:00 AM" to getTomorrowMorningMillis()
        )
    }

    var selectedPresetIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = SalimBlue
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text("Schedule Message", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Message will be sent automatically via native alarm scheduler.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Dispatch time",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEachIndexed { index, (label, offset) ->
                        val isSelected = selectedPresetIndex == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlass(
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = if (isSelected) 3.dp else 1.dp,
                                    customAlpha = if (isSelected) 0.9f else 0.5f
                                )
                                .clickable {
                                    selectedPresetIndex = index
                                    selectedTimestamp = if (index == 3) {
                                        offset
                                    } else {
                                        System.currentTimeMillis() + offset
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) SalimBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSchedule(text.trim(), selectedTimestamp)
                        onDismiss()
                    }
                },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(22.dp)
    )
}

private fun getTomorrowMorningMillis(): Long {
    val cal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}
