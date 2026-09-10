package com.virtual5g.presentation.navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.virtual5g.domain.engine.OptimizationEngine
import com.virtual5g.presentation.about.AboutScreen
import com.virtual5g.presentation.dashboard.DashboardScreen
import com.virtual5g.presentation.dashboard.DashboardUiState
import com.virtual5g.presentation.dashboard.DashboardViewModel
import com.virtual5g.presentation.devicecompat.DeviceCompatibilityScreen
import com.virtual5g.presentation.diagnostics.DiagnosticsScreen
import com.virtual5g.presentation.onboarding.OnboardingScreen
import com.virtual5g.presentation.planverification.PlanVerificationScreen
import com.virtual5g.presentation.planverification.PlanVerificationViewModel
import com.virtual5g.presentation.privacy.PrivacyDashboardScreen
import com.virtual5g.presentation.settings.SettingsScreen
import com.virtual5g.presentation.speedtest.SpeedTestScreen
import com.virtual5g.presentation.speedtest.SpeedTestViewModel

@Composable
fun Virtual5GNavGraph(
    navController: NavHostController,
    dependencies: NavGraphDependencies,
    startDestination: String = Destination.Onboarding.route
) {
    // Hoisted once at the graph level so Dashboard, Device Compatibility, and
    // Diagnostics all observe the same live Virtual5GState rather than each
    // spinning up their own collector.
    val dashboardViewModel: DashboardViewModel = viewModel(factory = dependencies.dashboardViewModelFactory)

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destination.Onboarding.route) {
            OnboardingScreen(
                onRequestTelephonyPermission = dependencies.onRequestTelephonyPermission,
                onFinished = {
                    navController.navigate(Destination.Dashboard.route) {
                        popUpTo(Destination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Destination.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onOpenSpeedTest = { navController.navigate(Destination.SpeedTest.route) },
                onOpenDeviceCompatibility = { navController.navigate(Destination.DeviceCompatibility.route) },
                onOpenPlanVerification = { navController.navigate(Destination.PlanVerification.route) },
                onOpenDiagnostics = { navController.navigate(Destination.Diagnostics.route) }
            )
        }

        composable(Destination.SpeedTest.route) {
            val viewModel: SpeedTestViewModel = viewModel(factory = dependencies.speedTestViewModelFactory)
            SpeedTestScreen(viewModel = viewModel, onBack = navController::popBackStack)
        }

        composable(Destination.DeviceCompatibility.route) {
            val uiState by dashboardViewModel.uiState.collectAsState()
            when (val state = uiState) {
                is DashboardUiState.Ready -> DeviceCompatibilityScreen(state.state.device, navController::popBackStack)
                else -> CircularProgressIndicator()
            }
        }

        composable(Destination.PlanVerification.route) {
            val viewModel: PlanVerificationViewModel = viewModel(factory = dependencies.planVerificationViewModelFactory)
            PlanVerificationScreen(viewModel = viewModel, onBack = navController::popBackStack)
        }

        composable(Destination.Diagnostics.route) {
            val uiState by dashboardViewModel.uiState.collectAsState()
            when (val state = uiState) {
                is DashboardUiState.Ready -> DiagnosticsScreen(
                    state = state.state,
                    recommendations = OptimizationEngine.recommendationsFor(state.state.network, state.state.mode),
                    onBack = navController::popBackStack
                )
                else -> CircularProgressIndicator()
            }
        }

        composable(Destination.Settings.route) {
            SettingsScreen(onBack = navController::popBackStack)
        }

        composable(Destination.Privacy.route) {
            PrivacyDashboardScreen(onBack = navController::popBackStack)
        }

        composable(Destination.About.route) {
            AboutScreen(onBack = navController::popBackStack)
        }
    }
}
