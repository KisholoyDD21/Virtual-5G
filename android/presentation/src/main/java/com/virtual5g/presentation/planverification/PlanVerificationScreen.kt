package com.virtual5g.presentation.planverification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.components.MetricRow
import com.virtual5g.presentation.components.SectionLabel
import com.virtual5g.presentation.theme.SignalCyan
import com.virtual5g.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanVerificationScreen(viewModel: PlanVerificationViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var manualCarrier by remember { mutableStateOf("") }
    var manualPlan by remember { mutableStateOf("") }
    var manualFiveG by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Plan Verification") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            GlowCard {
                SectionLabel("Current plan")
                Spacer(Modifier.height(10.dp))
                if (uiState.loading) {
                    CircularProgressIndicator(color = SignalCyan)
                } else {
                    val plan = uiState.plan
                    if (plan == null) {
                        Text("No plan detected", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    } else {
                        Text(plan.planName ?: "Unknown", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(10.dp))
                        MetricRow("Carrier", plan.carrierName ?: "—")
                        MetricRow("5G eligible", if (plan.fiveGEligible) "Yes" else "No")
                        MetricRow("Expiry", plan.expiry ?: "—")
                        MetricRow(
                            "Verification",
                            plan.verificationSource.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::refreshFromCarrier, modifier = Modifier.fillMaxWidth()) {
                    Text("Check with carrier")
                }
            }

            Spacer(Modifier.height(16.dp))

            GlowCard {
                SectionLabel("Enter plan manually")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Manually entered plans are always labeled \"User Provided\" - never shown as carrier-verified.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = manualCarrier,
                    onValueChange = { manualCarrier = it },
                    label = { Text("Carrier name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = manualPlan,
                    onValueChange = { manualPlan = it },
                    label = { Text("Plan name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Includes 5G benefits", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = manualFiveG, onCheckedChange = { manualFiveG = it })
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.submitManualPlan(manualCarrier, manualPlan, manualFiveG, expiry = null) },
                    enabled = manualCarrier.isNotBlank() && manualPlan.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save plan")
                }
            }
        }
    }
}
