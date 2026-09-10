package com.virtual5g.presentation.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.theme.TextSecondary
import com.virtual5g.presentation.theme.VirtualViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("About Virtual 5G") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            GlowCard(accent = VirtualViolet) {
                Text("What Virtual 5G is", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Virtual 5G does not convert 4G hardware into a real 5G modem. It provides a " +
                        "software-level optimized connectivity experience on 4G devices - compression, " +
                        "caching, DNS and route selection, and adaptive behavior - while genuine 5G " +
                        "functionality is enabled only when your hardware and network actually support it.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.height(16.dp))
            GlowCard {
                Text("Version", style = MaterialTheme.typography.titleMedium)
                Text("1.0.0 (MVP - Phases 1-3)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))
            GlowCard {
                Text("Open source", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Virtual 5G is built as an open, documented portfolio project. See the README and " +
                        "docs/limitations.md in the repository for the full technical writeup, including " +
                        "exactly which Android/carrier limitations shaped each design decision.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
