package com.virtual5g.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.domain.engine.OperatingModeSelector
import com.virtual5g.domain.engine.OptimizationEngine
import com.virtual5g.domain.engine.OptimizationRecommendation
import com.virtual5g.domain.model.OperatingMode
import com.virtual5g.domain.model.Virtual5GState
import com.virtual5g.presentation.components.BadgeTone
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.components.MetricRow
import com.virtual5g.presentation.components.ScoreRing
import com.virtual5g.presentation.components.SectionLabel
import com.virtual5g.presentation.components.StatusBadge
import com.virtual5g.presentation.components.toBadgeTone
import com.virtual5g.presentation.theme.SignalCyan
import com.virtual5g.presentation.theme.TextSecondary
import com.virtual5g.presentation.theme.VirtualViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenSpeedTest: () -> Unit,
    onOpenDeviceCompatibility: () -> Unit,
    onOpenPlanVerification: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Virtual 5G") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> LoadingBody(padding)
            is DashboardUiState.Error -> ErrorBody(padding, state.message, viewModel::refresh)
            is DashboardUiState.Ready -> DashboardBody(
                padding = padding,
                state = state.state,
                recommendations = state.recommendations,
                onOpenSpeedTest = onOpenSpeedTest,
                onOpenDeviceCompatibility = onOpenDeviceCompatibility,
                onOpenPlanVerification = onOpenPlanVerification,
                onOpenDiagnostics = onOpenDiagnostics
            )
        }
    }
}

@Composable
private fun LoadingBody(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = SignalCyan)
    }
}

@Composable
private fun ErrorBody(padding: PaddingValues, message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = TextSecondary)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        IconButton(onClick = onRetry) { Icon(Icons.Filled.Refresh, contentDescription = "Retry") }
    }
}

@Composable
private fun DashboardBody(
    padding: PaddingValues,
    state: Virtual5GState,
    recommendations: List<OptimizationRecommendation>,
    onOpenSpeedTest: () -> Unit,
    onOpenDeviceCompatibility: () -> Unit,
    onOpenPlanVerification: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ExperienceScoreCard(state) }
        item { ConnectionCard(state, onOpenSpeedTest) }
        item { DeviceCard(state, onOpenDeviceCompatibility) }
        item { PlanCard(state, onOpenPlanVerification) }
        item { OptimizationCard(recommendations, onOpenDiagnostics) }
        if (state.isOffline) {
            item { OfflineNotice(state) }
        }
    }
}

@Composable
private fun ExperienceScoreCard(state: Virtual5GState) {
    val accent = if (state.mode == OperatingMode.REAL_5G) SignalCyan else VirtualViolet
    GlowCard(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ScoreRing(score = state.score.total, tier = state.score.tier)
            Column {
                Text("Virtual 5G Experience Score", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Reflects your measured software/network experience - not a claim of radio-level 5G.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(10.dp))
                StatusBadge(OperatingModeSelector.displayLabelFor(state.mode), toneForMode(state.mode))
            }
        }
    }
}

@Composable
private fun ConnectionCard(state: Virtual5GState, onOpenSpeedTest: () -> Unit) {
    GlowCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SectionLabel("Network")
            StatusBadge(state.score.tier.name.lowercase().replaceFirstChar { it.uppercase() }, state.score.tier.toBadgeTone())
        }
        Spacer(Modifier.height(10.dp))
        Text(OperatingModeSelector.displayLabelFor(state.mode), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(state.explanation, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(14.dp))
        MetricRow("Download", state.network.downloadMbps?.let { "%.0f Mbps".format(it) } ?: "Run a speed test")
        MetricRow("Upload", state.network.uploadMbps?.let { "%.0f Mbps".format(it) } ?: "Run a speed test")
        MetricRow("Ping", state.network.pingMs?.let { "%.0f ms".format(it) } ?: "—")
        MetricRow("Jitter", state.network.jitterMs?.let { "%.0f ms".format(it) } ?: "—")
        MetricRow("Packet loss", state.network.packetLossPercent?.let { "%.1f%%".format(it) } ?: "—")
        Spacer(Modifier.height(8.dp))
        SpeedTestLink(onOpenSpeedTest)
    }
}

@Composable
private fun SpeedTestLink(onOpenSpeedTest: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onOpenSpeedTest) {
        Text("Run full speed test →")
    }
}

@Composable
private fun DeviceCard(state: Virtual5GState, onOpenDeviceCompatibility: () -> Unit) {
    GlowCard {
        SectionLabel("Device")
        Spacer(Modifier.height(10.dp))
        Text("${state.device.manufacturer} ${state.device.model}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        MetricRow("5G hardware support", if (state.device.supports5GHardware) "Yes" else "No")
        MetricRow("Android version", "API ${state.device.androidSdkInt} (${state.device.androidRelease})")
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.TextButton(onClick = onOpenDeviceCompatibility) {
            Text("View full compatibility →")
        }
    }
}

@Composable
private fun PlanCard(state: Virtual5GState, onOpenPlanVerification: () -> Unit) {
    GlowCard {
        SectionLabel("Plan")
        Spacer(Modifier.height(10.dp))
        val plan = state.plan
        if (plan == null) {
            Text("No plan information yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        } else {
            Text(plan.planName ?: "Unknown plan", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            MetricRow("5G eligible", if (plan.fiveGEligible) "Yes" else "No")
            MetricRow("Carrier", plan.carrierName ?: "—")
            MetricRow("Verification", plan.verificationSource.name.lowercase().replace('_', ' ')
                .replaceFirstChar { it.uppercase() })
        }
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.TextButton(onClick = onOpenPlanVerification) {
            Text("Verify or change plan →")
        }
    }
}

@Composable
private fun OptimizationCard(recommendations: List<OptimizationRecommendation>, onOpenDiagnostics: () -> Unit) {
    GlowCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SectionLabel("Virtual 5G Engine")
            StatusBadge("Active", BadgeTone.GOOD)
        }
        Spacer(Modifier.height(10.dp))
        OptimizationEngine.activeOptimizations().forEach { label ->
            MetricRow(label, "✓")
        }
        if (recommendations.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            SectionLabel("Recommendations")
            Spacer(Modifier.height(6.dp))
            recommendations.take(2).forEach { rec ->
                Text("• ${rec.title}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.TextButton(onClick = onOpenDiagnostics) {
            Text("Full diagnostics →")
        }
    }
}

@Composable
private fun OfflineNotice(state: Virtual5GState) {
    GlowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CloudOff, contentDescription = null, tint = TextSecondary)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Offline", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Last network measurement: ${relativeTime(state.lastUpdatedMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

private fun relativeTime(timestampMillis: Long): String {
    val diffMinutes = ((System.currentTimeMillis() - timestampMillis) / 60_000).coerceAtLeast(0)
    return when {
        diffMinutes < 1 -> "just now"
        diffMinutes == 1L -> "1 min ago"
        else -> "$diffMinutes min ago"
    }
}

private fun toneForMode(mode: OperatingMode): BadgeTone = when (mode) {
    OperatingMode.REAL_5G -> BadgeTone.EXCELLENT
    OperatingMode.VIRTUAL_5G -> BadgeTone.GOOD
    OperatingMode.OPTIMIZED_4G -> BadgeTone.MODERATE
    OperatingMode.WIFI -> BadgeTone.GOOD
    OperatingMode.LIMITED_MODE -> BadgeTone.POOR
    OperatingMode.OFFLINE -> BadgeTone.NEUTRAL
}
