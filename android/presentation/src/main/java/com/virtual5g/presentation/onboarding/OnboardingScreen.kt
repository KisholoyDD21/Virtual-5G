package com.virtual5g.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtual5g.presentation.components.GlowCard
import com.virtual5g.presentation.theme.SignalCyan
import com.virtual5g.presentation.theme.TextSecondary
import com.virtual5g.presentation.theme.VirtualViolet

private enum class OnboardingStep { WELCOME, PERMISSIONS }

/**
 * onRequestTelephonyPermission should trigger the real Android runtime
 * permission dialog (ActivityResultContracts.RequestPermission) from the
 * hosting Activity - wired in NavGraph/MainActivity, not here, since
 * permission requests need an Activity context.
 */
@Composable
fun OnboardingScreen(
    onRequestTelephonyPermission: () -> Unit,
    onFinished: () -> Unit
) {
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            OnboardingStep.WELCOME -> WelcomeStep(onNext = { step = OnboardingStep.PERMISSIONS })
            OnboardingStep.PERMISSIONS -> PermissionStep(
                onGrant = {
                    onRequestTelephonyPermission()
                    onFinished()
                },
                onSkip = onFinished
            )
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Text("Virtual 5G", style = MaterialTheme.typography.displayLarge, color = SignalCyan)
    Spacer(Modifier.height(12.dp))
    Text(
        "A software-optimized 5G-like experience on 4G, and real 5G unlocked automatically when your " +
            "device, network, and plan actually support it.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
    Spacer(Modifier.height(24.dp))
    GlowCard(accent = VirtualViolet) {
        Text(
            "Virtual 5G never converts your 4G modem into a real 5G radio. It optimizes your existing " +
                "connection in software, and switches to real 5G automatically the moment your phone is " +
                "actually attached to a 5G network.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Spacer(Modifier.height(24.dp))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
}

@Composable
private fun PermissionStep(onGrant: () -> Unit, onSkip: () -> Unit) {
    Text("One permission needed", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(12.dp))
    Text(
        "Phone state access lets Virtual 5G read your connection type and signal strength so it can " +
            "detect real 5G and score your connection quality. We never read call logs, contacts, or " +
            "message content.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary
    )
    Spacer(Modifier.height(24.dp))
    Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) { Text("Grant permission") }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
        Text("Skip for now (limited features)")
    }
}
