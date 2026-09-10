package com.virtual5g.presentation.devicecompat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.domain.model.CapabilityConfidence
import com.virtual5g.domain.model.DeviceCapability
import com.virtual5g.domain.model.defaultCapabilityComparison
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.components.MetricRow
import com.virtual5g.presentation.components.SectionLabel
import com.virtual5g.presentation.theme.StatusCritical
import com.virtual5g.presentation.theme.StatusExcellent
import com.virtual5g.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCompatibilityScreen(device: DeviceCapability, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Device Compatibility") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlowCard {
                    SectionLabel("Device")
                    Spacer(Modifier.height(10.dp))
                    Text("${device.manufacturer} ${device.model}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    MetricRow("Android version", "API ${device.androidSdkInt} (${device.androidRelease})")
                    MetricRow("Supported radios", device.supportedNetworkTypeNames.joinToString(", "))
                    MetricRow("5G hardware support", if (device.supports5GHardware) "Yes" else "No")
                    MetricRow("Confidence", confidenceLabel(device.capabilityConfidence))
                }
            }
            item {
                GlowCard {
                    Text("How we determine this", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Android has no direct API to query 5G hardware support independent of current " +
                            "coverage. We confirm 5G support the first time your device actually attaches to " +
                            "a 5G network, and remember that going forward. Until then, this is shown as " +
                            "\"not yet confirmed\" rather than a hard \"no\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            item {
                GlowCard {
                    Text("What works without 5G hardware", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    defaultCapabilityComparison(device.supports5GHardware).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(row.capability, style = MaterialTheme.typography.bodyLarge)
                                row.note?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }
                            Icon(
                                imageVector = if (row.available) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = if (row.available) "Available" else "Not available",
                                tint = if (row.available) StatusExcellent else StatusCritical
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun confidenceLabel(confidence: CapabilityConfidence): String = when (confidence) {
    CapabilityConfidence.CONFIRMED_BY_OBSERVATION -> "Confirmed by observed 5G attach"
    CapabilityConfidence.UNKNOWN_NO_COVERAGE_OBSERVED -> "Not yet confirmed (no 5G coverage observed)"
    CapabilityConfidence.UNKNOWN_PERMISSION_DENIED -> "Unknown (phone permission not granted)"
}
