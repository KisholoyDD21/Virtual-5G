package com.virtual5g.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.virtual5g.presentation.onboarding.OnboardingScreen
import com.virtual5g.presentation.theme.Virtual5GTheme
import org.junit.Rule
import org.junit.Test

/**
 * Representative instrumented UI test. The spec asks for UI test coverage
 * across onboarding, dashboard, speed test, diagnostics, settings, and plan
 * verification (section 19) - this establishes the pattern for onboarding;
 * the rest follow the same shape once their ViewModels have fake use-case
 * implementations (see docs/limitations.md for the full test-suite scope
 * that's still open).
 */
class OnboardingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingContinue_advancesFromWelcomeToPermissionStep() {
        var permissionRequested = false
        var finished = false

        composeRule.setContent {
            Virtual5GTheme {
                OnboardingScreen(
                    onRequestTelephonyPermission = { permissionRequested = true },
                    onFinished = { finished = true }
                )
            }
        }

        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Grant permission").assertExists()

        composeRule.onNodeWithText("Grant permission").performClick()
        assert(permissionRequested)
        assert(finished)
    }

    @Test
    fun skippingPermissionStep_stillFinishesOnboarding() {
        var finished = false

        composeRule.setContent {
            Virtual5GTheme {
                OnboardingScreen(
                    onRequestTelephonyPermission = { },
                    onFinished = { finished = true }
                )
            }
        }

        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Skip for now (limited features)").performClick()
        assert(finished)
    }
}
