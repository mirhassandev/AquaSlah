package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.liquidGlassCard(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.85f
): Modifier {
    val isDark = MaterialTheme.colorScheme.background == GlassDarkBg

    val bgBrush = remember(isDark, alpha) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF102438).copy(alpha = alpha),
                    Color(0xFF0C1B2B).copy(alpha = alpha)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha + 0.10f),
                    Color(0xFFF4F8FC).copy(alpha = alpha)
                )
            )
        }
    }

    val borderBrush = remember(isDark) {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF02C39A).copy(alpha = 0.45f),
                    Color(0xFFE5A93B).copy(alpha = 0.25f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF00A896).copy(alpha = 0.35f),
                    Color(0xFF000000).copy(alpha = 0.10f)
                )
            )
        }
    }

    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    return this
        .clip(shape)
        .background(bgBrush, shape)
        .border(borderWidth, borderBrush, shape)
}

@Composable
fun getLiquidBackgroundBrush(isDark: Boolean): Brush {
    return remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF06111C),
                    Color(0xFF0B1E30),
                    Color(0xFF081420)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE8F2FC),
                    Color(0xFFF4F8FC),
                    Color(0xFFE2EFFB)
                )
            )
        }
    }
}
