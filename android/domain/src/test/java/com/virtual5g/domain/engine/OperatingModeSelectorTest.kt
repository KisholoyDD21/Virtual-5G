package com.virtual5g.domain.engine

import com.google.common.truth.Truth.assertThat
import com.virtual5g.domain.model.CapabilityConfidence
import com.virtual5g.domain.model.ConnectionType
import com.virtual5g.domain.model.DeviceCapability
import com.virtual5g.domain.model.NetworkMetrics
import com.virtual5g.domain.model.OperatingMode
import com.virtual5g.domain.model.PlanInfo
import com.virtual5g.domain.model.PlanVerificationSource
import org.junit.Test

class OperatingModeSelectorTest {

    private fun device(supports5G: Boolean) = DeviceCapability(
        androidSdkInt = 34,
        androidRelease = "15",
        manufacturer = "Example",
        model = "Phone",
        supports5GHardware = supports5G,
        capabilityConfidence = if (supports5G) CapabilityConfidence.CONFIRMED_BY_OBSERVATION else CapabilityConfidence.UNKNOWN_NO_COVERAGE_OBSERVED,
        supportedNetworkTypeNames = emptyList()
    )

    private fun network(type: ConnectionType) = NetworkMetrics(connectionType = type, timestampMillis = 0L)

    private fun plan(eligible: Boolean) = PlanInfo(
        carrierName = "Example Telecom",
        planName = "Unlimited",
        fiveGEligible = eligible,
        expiry = "2027-01-01",
        verificationSource = PlanVerificationSource.USER_PROVIDED
    )

    @Test
    fun `4G-only device with no plan info runs Virtual 5G - spec Mode A`() {
        val mode = OperatingModeSelector.select(device(false), network(ConnectionType.CELLULAR_4G), plan = null)
        assertThat(mode).isEqualTo(OperatingMode.VIRTUAL_5G)
    }

    @Test
    fun `4G-only device with a 5G-eligible plan still runs Virtual 5G, never claims real 5G - spec Mode C`() {
        val mode = OperatingModeSelector.select(device(false), network(ConnectionType.CELLULAR_4G), plan(eligible = true))
        assertThat(mode).isEqualTo(OperatingMode.VIRTUAL_5G)

        val explanation = OperatingModeSelector.explanationFor(mode, device(false), plan(eligible = true))
        assertThat(explanation).contains("Device Limited to 4G")
        assertThat(explanation).doesNotContain("Real 5G is now active")
    }

    @Test
    fun `5G device actually attached via NR with an eligible plan is Real 5G - spec Mode B`() {
        val mode = OperatingModeSelector.select(device(true), network(ConnectionType.CELLULAR_5G), plan(eligible = true))
        assertThat(mode).isEqualTo(OperatingMode.REAL_5G)
    }

    @Test
    fun `5G-capable device out of 5G coverage but eligible plan falls back to Virtual 5G`() {
        val mode = OperatingModeSelector.select(device(true), network(ConnectionType.CELLULAR_4G), plan(eligible = true))
        assertThat(mode).isEqualTo(OperatingMode.VIRTUAL_5G)
    }

    @Test
    fun `5G-capable device without an eligible plan runs Optimized 4G - spec point 3`() {
        val mode = OperatingModeSelector.select(device(true), network(ConnectionType.CELLULAR_4G), plan(eligible = false))
        assertThat(mode).isEqualTo(OperatingMode.OPTIMIZED_4G)
    }

    @Test
    fun `wifi always wins regardless of device or plan`() {
        val mode = OperatingModeSelector.select(device(true), network(ConnectionType.WIFI), plan(eligible = true))
        assertThat(mode).isEqualTo(OperatingMode.WIFI)
    }

    @Test
    fun `no connection is offline regardless of device or plan`() {
        val mode = OperatingModeSelector.select(device(true), network(ConnectionType.NONE), plan(eligible = true))
        assertThat(mode).isEqualTo(OperatingMode.OFFLINE)
    }

    @Test
    fun `real 5G is never selected on a device without confirmed 5G hardware, even if attached type looks like 5G`() {
        // Defensive case: a misreported connection type must not be enough on its own.
        val mode = OperatingModeSelector.select(device(false), network(ConnectionType.CELLULAR_5G), plan(eligible = true))
        assertThat(mode).isNotEqualTo(OperatingMode.REAL_5G)
    }
}
