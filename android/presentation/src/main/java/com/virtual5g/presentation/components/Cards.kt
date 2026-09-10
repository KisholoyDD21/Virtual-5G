package com.virtual5g.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.virtual5g.presentation.theme.CardSurface
import com.virtual5g.presentation.theme.CardSurfaceElevated
import com.virtual5g.presentation.theme.HairlineStroke
import com.virtual5g.presentation.theme.StatusCritical
import com.virtual5g.presentation.theme.StatusExcellent
import com.virtual5g.presentation.theme.StatusGood
import com.virtual5g.presentation.theme.StatusModerate
import com.virtual5g.presentation.theme.StatusPoor
import com.virtual5g.presentation.theme.TextMuted
import com.virtual5g.presentation.theme.TextSecondary

/** The base "glass panel" every card on every screen is built from. */
@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    accent: Color = HairlineStroke,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(listOf(CardSurfaceElevated, CardSurface))
            )
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = TextMuted,
        modifier = modifier
    )
}

@Composable
fun MetricRow(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
    }
}

enum class BadgeTone { EXCELLENT, GOOD, MODERATE, POOR, CRITICAL, NEUTRAL }

@Composable
fun StatusBadge(text: String, tone: BadgeTone, modifier: Modifier = Modifier) {
    val color = when (tone) {
        BadgeTone.EXCELLENT -> StatusExcellent
        BadgeTone.GOOD -> StatusGood
        BadgeTone.MODERATE -> StatusModerate
        BadgeTone.POOR -> StatusPoor
        BadgeTone.CRITICAL -> StatusCritical
        BadgeTone.NEUTRAL -> TextSecondary
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}
