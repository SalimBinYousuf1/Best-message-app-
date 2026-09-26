package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val AvatarColors = listOf(
    Color(0xFF5B8DEF),
    Color(0xFF8E5BEF),
    Color(0xFFEF5BA8),
    Color(0xFFEF8E5B),
    Color(0xFF5BEFA8),
    Color(0xFF28B78D),
    Color(0xFF3B82F6),
    Color(0xFF6366F1),
    Color(0xFFEC4899)
)

@Composable
fun ContactAvatar(
    name: String?,
    address: String,
    photoUri: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val initials = remember(name, address) {
        val cleanName = name?.trim()
        if (!cleanName.isNullOrBlank()) {
            val parts = cleanName.split("\\s+".toRegex())
            if (parts.size >= 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
                "${parts[0].first()}${parts[1].first()}".uppercase()
            } else {
                "${cleanName.first()}".uppercase()
            }
        } else {
            val digits = address.filter { it.isDigit() }
            if (digits.isNotEmpty()) "#" else "?"
        }
    }

    val backgroundColor = remember(name, address) {
        val key = name ?: address
        val hash = kotlin.math.abs(key.hashCode())
        AvatarColors[hash % AvatarColors.size]
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = name ?: address,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            val fontSize = (size.value * 0.40f).sp
            Text(
                text = initials,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
