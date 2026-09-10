package com.virtual5g.presentation.privacy

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.theme.TextSecondary

private data class PrivacyItem(val title: String, val explanation: String, val toggleable: Boolean, val defaultOn: Boolean)

private val privacyItems = listOf(
    PrivacyItem(
        "Network data",
        "Connection type, signal strength, and measured speed/latency - used only to compute your score and mode on-device.",
        toggleable = false,
        defaultOn = true
    ),
    PrivacyItem(
        "Device data",
        "Android version, manufacturer, and model - used only for compatibility decisions on-device.",
        toggleable = false,
        defaultOn = true
    ),
    PrivacyItem(
        "Diagnostics data",
        "Speed test results you explicitly run, stored locally so you can see your history.",
        toggleable = false,
        defaultOn = true
    ),
    PrivacyItem(
        "Cloud synchronization",
        "Off by default. When enabled, your plan verification status syncs to your account so it's " +
            "available if you reinstall.",
        toggleable = true,
        defaultOn = false
    ),
    PrivacyItem(
        "Analytics telemetry",
        "Off by default. When enabled, aggregate, anonymized usage metrics help us improve the app. " +
            "Never includes browsing content, messages, or precise location.",
        toggleable = true,
        defaultOn = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDashboardScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Privacy & Security") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "We collect the minimum needed to run the app. Nothing here ever includes message " +
                        "content, passwords, browsing history, or decrypted HTTPS traffic.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            items(privacyItems) { item -> PrivacyRow(item) }
        }
    }
}

@Composable
private fun PrivacyRow(item: PrivacyItem) {
    var enabled by remember { mutableStateOf(item.defaultOn) }
    GlowCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(item.explanation, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Spacer(Modifier.width(12.dp))
            if (item.toggleable) {
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            } else {
                Text("Required", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
    }
}
