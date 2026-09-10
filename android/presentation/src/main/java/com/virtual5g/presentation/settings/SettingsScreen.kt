package com.virtual5g.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.components.SectionLabel
import com.virtual5g.presentation.theme.TextSecondary

private data class OptimizationToggle(val label: String, val description: String)

private val toggles = listOf(
    OptimizationToggle("Latency-aware endpoint selection", "Prefer the fastest-responding server for supported requests."),
    OptimizationToggle("DNS optimization", "Use a low-latency DNS resolver where the platform allows overriding it."),
    OptimizationToggle("Adaptive networking", "Adjust behavior automatically as measured conditions change."),
    OptimizationToggle("Response caching", "Cache frequently accessed content to reduce repeat downloads."),
    OptimizationToggle("Background sync minimization", "Defer non-essential background sync on poor connections.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var virtual5GEnabled by remember { mutableStateOf(true) }
    var autoWifiSuggest by remember { mutableStateOf(true) }
    var latencyThreshold by remember { mutableFloatStateOf(150f) }

    Scaffold(topBar = { TopAppBar(title = { Text("Virtual 5G Settings") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            GlowCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Virtual 5G engine", style = MaterialTheme.typography.titleMedium)
                        Text("Master switch for all software-level optimization.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Switch(checked = virtual5GEnabled, onCheckedChange = { virtual5GEnabled = it })
                }
            }
            Spacer(Modifier.height(16.dp))
            GlowCard {
                SectionLabel("Optimization controls")
                Spacer(Modifier.height(12.dp))
                toggles.forEach { toggle -> ToggleRow(toggle, enabled = virtual5GEnabled) }
            }
            Spacer(Modifier.height(16.dp))
            GlowCard {
                Text("Recommend Wi-Fi below this latency threshold", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("${latencyThreshold.toInt()} ms", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Slider(value = latencyThreshold, onValueChange = { latencyThreshold = it }, valueRange = 50f..400f)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-suggest Wi-Fi handoff", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = autoWifiSuggest, onCheckedChange = { autoWifiSuggest = it })
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(toggle: OptimizationToggle, enabled: Boolean) {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(toggle.label, style = MaterialTheme.typography.bodyLarge)
            Text(toggle.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Switch(checked = checked && enabled, onCheckedChange = { checked = it }, enabled = enabled)
    }
}
