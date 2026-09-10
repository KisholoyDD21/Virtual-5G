package com.virtual5g.presentation.navigation

import androidx.lifecycle.ViewModelProvider

/**
 * Everything the nav graph needs but shouldn't construct itself. Implemented
 * by AppContainer in :app, so :presentation never depends on :data directly
 * - only on these factories, matching Clean Architecture's dependency rule.
 */
interface NavGraphDependencies {
    val dashboardViewModelFactory: ViewModelProvider.Factory
    val speedTestViewModelFactory: ViewModelProvider.Factory
    val planVerificationViewModelFactory: ViewModelProvider.Factory
    val hasTelephonyPermission: () -> Boolean
    val onRequestTelephonyPermission: () -> Unit
}
