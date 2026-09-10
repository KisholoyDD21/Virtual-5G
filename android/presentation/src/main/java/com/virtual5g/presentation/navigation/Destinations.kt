package com.virtual5g.presentation.navigation

sealed class Destination(val route: String) {
    data object Onboarding : Destination("onboarding")
    data object Dashboard : Destination("dashboard")
    data object SpeedTest : Destination("speed_test")
    data object DeviceCompatibility : Destination("device_compatibility")
    data object PlanVerification : Destination("plan_verification")
    data object Diagnostics : Destination("diagnostics")
    data object Settings : Destination("settings")
    data object Privacy : Destination("privacy")
    data object About : Destination("about")
}
