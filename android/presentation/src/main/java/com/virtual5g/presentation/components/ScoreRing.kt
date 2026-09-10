package com.virtual5g.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.virtual5g.domain.model.NetworkQualityTier
import com.virtual5g.presentation.theme.HairlineStroke
import com.virtual5g.presentation.theme.StatusCritical
import com.virtual5g.presentation.theme.StatusExcellent
import com.virtual5g.presentation.theme.StatusGood
import com.virtual5g.presentation.theme.StatusModerate
import com.virtual5g.presentation.theme.StatusPoor

fun NetworkQualityTier.toBadgeTone(): BadgeTone = when (this) {
    NetworkQualityTier.EXCELLENT -> BadgeTone.EXCELLENT
    NetworkQualityTier.GOOD -> BadgeTone.GOOD
    NetworkQualityTier.MODERATE -> BadgeTone.MODERATE
    NetworkQualityTier.POOR -> BadgeTone.POOR
    NetworkQualityTier.CRITICAL -> BadgeTone.CRITICAL
}

private fun colorForTier(tier: NetworkQualityTier) = when (tier) {
    NetworkQualityTier.EXCELLENT -> StatusExcellent
    NetworkQualityTier.GOOD -> StatusGood
    NetworkQualityTier.MODERATE -> StatusModerate
    NetworkQualityTier.POOR -> StatusPoor
    NetworkQualityTier.CRITICAL -> StatusCritical
}

@Composable
fun ScoreRing(
    score: Int,
    tier: NetworkQualityTier,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 120.dp
) {
    val color = colorForTier(tier)
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val strokeWidth = size.minDimension * 0.09f
            drawArc(
                color = HairlineStroke,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (score.coerceIn(0, 100) / 100f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            )
        }
        Text(text = "$score", style = MaterialTheme.typography.displayLarge, color = color)
    }
}
