package com.virtual5g.presentation.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.domain.engine.OptimizationRecommendation
import com.virtual5g.domain.model.ExperienceScore
import com.virtual5g.domain.model.Virtual5GState
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.components.MetricRow
import com.virtual5g.presentation.components.SectionLabel
import com.virtual5g.presentation.theme.SignalCyan
import com.virtual5g.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    state: Virtual5GState,
    recommendations: List<OptimizationRecommendation>,
    onBack: () -> Unit
) {
    val score = ExperienceScore.from(state.score)

    Scaffold(topBar = { TopAppBar(title = { Text("Diagnostics & Analytics") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlowCard {
                    SectionLabel("Score breakdown")
                    Spacer(Modifier.height(12.dp))
                    ScoreBar("Speed", score.speed)
                    ScoreBar("Latency", score.latency)
                    ScoreBar("Stability", score.stability)
                    ScoreBar("Signal", score.signal)
                    ScoreBar("Jitter", score.jitter)
                }
            }
            item {
                GlowCard {
                    SectionLabel("Raw metrics")
                    Spacer(Modifier.height(10.dp))
                    MetricRow("Connection type", state.network.connectionType.name)
                    MetricRow("NR attachment", state.network.nrAttachment.name)
                    MetricRow("Signal level (0-4)", state.network.signalLevel?.toString() ?: "—")
                    MetricRow("Signal (dBm)", state.network.signalStrengthDbm?.toString() ?: "—")
                    MetricRow("Carrier", state.network.carrierName ?: "—")
                }
            }
            item {
                GlowCard {
                    SectionLabel("Recommendations")
                    Spacer(Modifier.height(10.dp))
                }
            }
            items(recommendations) { rec ->
                GlowCard {
                    Text(rec.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(rec.detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ScoreBar(label: String, value: Int) {
    Text(label, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(4.dp))
    LinearProgressIndicator(
        progress = { value / 100f },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color = SignalCyan
    )
    Spacer(Modifier.height(2.dp))
    Text("$value / 100", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    Spacer(Modifier.height(10.dp))
}
