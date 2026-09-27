package com.example.ui.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleMessageDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSchedule: (String, Long) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var isCustomPickerOpen by remember { mutableStateOf(false) }

    // Quick presets
    val presets = remember {
        listOf(
            "In 15 minutes" to 15 * 60 * 1000L,
            "In 1 hour" to 60 * 60 * 1000L,
            "In 3 hours" to 3 * 60 * 60 * 1000L,
            "Tomorrow 9:00 AM" to getTomorrowMorningMillis()
        )
    }

    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    // Custom calendar and flipper states initialized to now + 30 mins
    val initialCal = remember {
        Calendar.getInstance().apply { add(Calendar.MINUTE, 30) }
    }

    var calendarMonthOffset by remember { mutableIntStateOf(0) } // month navigation from current
    var selectedYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(initialCal.get(Calendar.DAY_OF_MONTH)) }

    // Time Flipper states: HH (1..12), mm (0..59), ss (0..59), am/pm ("AM", "PM")
    var selectedHour12 by remember {
        val raw = initialCal.get(Calendar.HOUR)
        mutableIntStateOf(if (raw == 0) 12 else raw)
    }
    var selectedMinute by remember { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }
    var selectedSecond by remember { mutableIntStateOf(0) }
    var selectedAmPm by remember {
        mutableStateOf(if (initialCal.get(Calendar.AM_PM) == Calendar.PM) "PM" else "AM")
    }

    // Calculated target timestamp
    val customTimestamp by remember {
        derivedStateOf {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)

                var hour24 = selectedHour12 % 12
                if (selectedAmPm == "PM") {
                    hour24 += 12
                }
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, selectedSecond)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }
    }

    val finalTimestamp by remember {
        derivedStateOf {
            if (isCustomPickerOpen) {
                customTimestamp
            } else {
                if (selectedPresetIndex == 3) {
                    presets[3].second
                } else {
                    System.currentTimeMillis() + presets[selectedPresetIndex].second
                }
            }
        }
    }

    val isTimestampInFuture by remember {
        derivedStateOf {
            finalTimestamp > System.currentTimeMillis() + 10_000L
        }
    }

    val formattedScheduledPreview = remember(finalTimestamp) {
        val sdf = SimpleDateFormat("EEE, MMM d, yyyy 'at' hh:mm:ss a", Locale.getDefault())
        sdf.format(Date(finalTimestamp))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = SalimBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule Message", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Message will be queued and sent via Android native alarm manager.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Message text") },
                    placeholder = { Text("Type message to schedule...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mode switch: Quick presets vs Custom Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISPATCH TIME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Custom Date & Time button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCustomPickerOpen) SalimBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { isCustomPickerOpen = !isCustomPickerOpen }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (isCustomPickerOpen) Color.White else SalimBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCustomPickerOpen) "Using Custom" else "Custom Date & Time",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isCustomPickerOpen) Color.White else SalimBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isCustomPickerOpen) {
                    // Quick Presets
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        presets.forEachIndexed { index, (label, _) ->
                            val isSelected = selectedPresetIndex == index
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlass(
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = if (isSelected) 2.dp else 1.dp,
                                        customAlpha = if (isSelected) 0.92f else 0.55f
                                    )
                                    .clickable { selectedPresetIndex = index }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) SalimBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "Selected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SalimBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // MINIMALIST PREMIUM APPLE-DESIGNED CALENDAR
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                            .padding(12.dp)
                    ) {
                        AppleMinimalistCalendar(
                            monthOffset = calendarMonthOffset,
                            onMonthOffsetChange = { calendarMonthOffset = it },
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            selectedDay = selectedDay,
                            onDateSelected = { y, m, d ->
                                selectedYear = y
                                selectedMonth = m
                                selectedDay = d
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // TIME FLIPPER: HH mm ss (am/pm) 100% WORKING
                    Text(
                        text = "TIME FLIPPER (HH : mm : ss)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        TimeFlipper(
                            hour = selectedHour12,
                            minute = selectedMinute,
                            second = selectedSecond,
                            amPm = selectedAmPm,
                            onHourChange = { selectedHour12 = it },
                            onMinuteChange = { selectedMinute = it },
                            onSecondChange = { selectedSecond = it },
                            onAmPmChange = { selectedAmPm = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Schedule preview badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isTimestampInFuture) SalimBlue.copy(alpha = 0.12f)
                            else StatusError.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = if (isTimestampInFuture) "Ready for dispatch:" else "Selected time is in the past!",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isTimestampInFuture) SalimBlue else StatusError
                            )
                        )
                        Text(
                            text = formattedScheduledPreview,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank() && isTimestampInFuture) {
                        onSchedule(text.trim(), finalTimestamp)
                        onDismiss()
                    }
                },
                enabled = text.isNotBlank() && isTimestampInFuture,
                colors = ButtonDefaults.buttonColors(containerColor = SalimBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Schedule", fontWeight = FontWeight.SemiBold)
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

/**
 * Minimalist premium Apple-designed calendar.
 * Renders sleek month navigation, weekday labels, and crisp day circular selectors.
 */
@Composable
private fun AppleMinimalistCalendar(
    monthOffset: Int,
    onMonthOffsetChange: (Int) -> Unit,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDateSelected: (Int, Int, Int) -> Unit
) {
    val displayCal = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val displayYear = displayCal.get(Calendar.YEAR)
    val displayMonth = displayCal.get(Calendar.MONTH)
    val monthTitle = remember(displayCal) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(displayCal.time)
    }

    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    // Calculate days grid
    val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = displayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based Sunday=0

    Column(modifier = Modifier.fillMaxWidth()) {
        // Month title + Navigation Chevrons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row {
                IconButton(
                    onClick = { if (monthOffset > 0) onMonthOffsetChange(monthOffset - 1) },
                    enabled = monthOffset > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = if (monthOffset > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { if (monthOffset < 12) onMonthOffsetChange(monthOffset + 1) },
                    enabled = monthOffset < 12,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = if (monthOffset < 12) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Weekday header
        val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Days Grid (6 rows max)
        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
        val rows = totalCells / 7

        for (r in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val dayNumber = cellIndex - firstDayOfWeek + 1

                    if (dayNumber in 1..daysInMonth) {
                        val isSelected = (displayYear == selectedYear && displayMonth == selectedMonth && dayNumber == selectedDay)
                        val isToday = (displayYear == todayYear && displayMonth == todayMonth && dayNumber == todayDay)
                        val isPast = (displayYear < todayYear) ||
                                (displayYear == todayYear && displayMonth < todayMonth) ||
                                (displayYear == todayYear && displayMonth == todayMonth && dayNumber < todayDay)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> SalimBlue
                                        isToday -> SalimBlue.copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(enabled = !isPast) {
                                    onDateSelected(displayYear, displayMonth, dayNumber)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNumber",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = when {
                                    isSelected -> Color.White
                                    isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                    isToday -> SalimBlue
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    } else {
                        // Empty slot outside month
                        Spacer(modifier = Modifier.weight(1f).size(32.dp))
                    }
                }
            }
        }
    }
}

/**
 * Apple-style Time Flipper Wheel for HH, mm, ss, and AM/PM.
 * 100% interactive, supports step clicking and continuous adjustments.
 */
@Composable
private fun TimeFlipper(
    hour: Int,
    minute: Int,
    second: Int,
    amPm: String,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onSecondChange: (Int) -> Unit,
    onAmPmChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // HH Column
        FlipperWheelColumn(
            label = "HOUR",
            value = String.format(Locale.US, "%02d", hour),
            onIncrement = {
                val next = if (hour >= 12) 1 else hour + 1
                onHourChange(next)
            },
            onDecrement = {
                val prev = if (hour <= 1) 12 else hour - 1
                onHourChange(prev)
            }
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 14.dp)
        )

        // mm Column
        FlipperWheelColumn(
            label = "MIN",
            value = String.format(Locale.US, "%02d", minute),
            onIncrement = {
                val next = (minute + 1) % 60
                onMinuteChange(next)
            },
            onDecrement = {
                val prev = if (minute <= 0) 59 else minute - 1
                onMinuteChange(prev)
            }
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 14.dp)
        )

        // ss Column
        FlipperWheelColumn(
            label = "SEC",
            value = String.format(Locale.US, "%02d", second),
            onIncrement = {
                val next = (second + 1) % 60
                onSecondChange(next)
            },
            onDecrement = {
                val prev = if (second <= 0) 59 else second - 1
                onSecondChange(prev)
            }
        )

        // AM/PM Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 6.dp)
        ) {
            Text(
                text = "PERIOD",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (amPm == "AM") SalimBlue else Color.Transparent)
                            .clickable { onAmPmChange("AM") }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (amPm == "AM") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (amPm == "PM") SalimBlue else Color.Transparent)
                            .clickable { onAmPmChange("PM") }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PM",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (amPm == "PM") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipperWheelColumn(
    label: String,
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increment $label",
                tint = SalimBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 36.dp)
                .liquidGlass(shape = RoundedCornerShape(10.dp), elevation = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrement $label",
                tint = SalimBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
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
