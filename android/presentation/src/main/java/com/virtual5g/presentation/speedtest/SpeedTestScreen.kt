package com.virtual5g.presentation.speedtest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.domain.model.SpeedTestStage
import com.virtual5g.domain.model.SpeedTestTier
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.components.MetricRow
import com.virtual5g.presentation.theme.SignalCyan
import com.virtual5g.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(viewModel: SpeedTestViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTier by remember { mutableStateOf(SpeedTestTier.STANDARD) }

    Scaffold(topBar = { TopAppBar(title = { Text("Speed Test") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            GlowCard {
                Text("Test depth", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    tierDescription(selectedTier),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpeedTestTier.entries.forEach { tier ->
                        FilterChip(
                            selected = tier == selectedTier,
                            onClick = { selectedTier = tier },
                            label = { Text(tier.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.start(selectedTier) },
                    enabled = uiState !is SpeedTestUiState.Running,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedTier == SpeedTestTier.DEEP) "Start Deep Test (uses more data)" else "Start Test")
                }
            }

            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                SpeedTestUiState.Idle -> Unit
                is SpeedTestUiState.Running -> RunningCard(state.stage)
                is SpeedTestUiState.Done -> ResultsCard(state)
            }
        }
    }
}

private fun tierDescription(tier: SpeedTestTier): String = when (tier) {
    SpeedTestTier.QUICK -> "~2 MB down / 1 MB up. Fastest, lowest data use."
    SpeedTestTier.STANDARD -> "~10 MB down / 5 MB up. Balanced accuracy and data use."
    SpeedTestTier.DEEP -> "~25 MB down / 10 MB up. Most accurate on fast connections; uses noticeably more data."
}

@Composable
private fun RunningCard(stage: SpeedTestStage) {
    GlowCard {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.height(20.dp).padding(end = 12.dp), color = SignalCyan, strokeWidth = 2.dp)
            Text(stageLabel(stage), style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun stageLabel(stage: SpeedTestStage): String = when (stage) {
    SpeedTestStage.PING -> "Measuring ping..."
    SpeedTestStage.JITTER -> "Measuring jitter..."
    SpeedTestStage.DOWNLOAD -> "Testing download..."
    SpeedTestStage.UPLOAD -> "Testing upload..."
    SpeedTestStage.DNS -> "Resolving DNS..."
    SpeedTestStage.HTTPS_HANDSHAKE -> "Establishing secure connection..."
    SpeedTestStage.COMPLETE -> "Done"
}

@Composable
private fun ResultsCard(state: SpeedTestUiState.Done) {
    val r = state.result
    GlowCard {
        Text("Results", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        MetricRow("Download", r.downloadMbps?.let { "%.1f Mbps".format(it) } ?: "Failed")
        MetricRow("Upload", r.uploadMbps?.let { "%.1f Mbps".format(it) } ?: "Failed")
        MetricRow("Ping", r.pingMs?.let { "%.0f ms".format(it) } ?: "—")
        MetricRow("Jitter", r.jitterMs?.let { "%.0f ms".format(it) } ?: "—")
        MetricRow("Packet loss", r.packetLossPercent?.let { "%.1f%%".format(it) } ?: "—")
        MetricRow("DNS lookup", r.dnsLookupMs?.let { "%.0f ms".format(it) } ?: "—")
        MetricRow("TCP connect", r.tcpConnectMs?.let { "%.0f ms".format(it) } ?: "—")
        MetricRow("TLS handshake", r.httpsHandshakeMs?.let { "%.0f ms".format(it) } ?: "—")
        if (!r.succeeded) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Test endpoint was unreachable. Check your connection or configure a different endpoint in Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
