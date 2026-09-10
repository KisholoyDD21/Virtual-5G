package com.virtual5g.data.carrier

import com.virtual5g.domain.model.PlanInfo
import com.virtual5g.domain.model.PlanVerificationSource
import com.virtual5g.domain.repository.CarrierInfo
import com.virtual5g.domain.repository.CarrierProvider
import kotlinx.coroutines.delay

/**
 * Development/demo implementation of CarrierProvider. Mirrors the exact
 * example payload from the spec:
 *
 * {
 *   "carrier": "Example Telecom",
 *   "plan": "Unlimited 5G",
 *   "fiveGEligible": true,
 *   "expiry": "2026-10-12"
 * }
 *
 * A real integration (spec section 4) implements this same interface
 * against an actual carrier API and is swapped in via DI - nothing else in
 * the app needs to change. No carrier website is scraped anywhere in this
 * project (see docs/limitations.md).
 */
class MockCarrierProvider(
    private val simulatedLatencyMillis: Long = 400L,
    private val fixture: MockPlanFixture = MockPlanFixture.default()
) : CarrierProvider {

    override suspend fun getCarrierInfo(): CarrierInfo {
        delay(simulatedLatencyMillis)
        return CarrierInfo(
            carrierName = fixture.carrier,
            countryIso = fixture.countryIso,
            mcc = fixture.mcc,
            mnc = fixture.mnc
        )
    }

    override suspend fun getPlanInfo(): PlanInfo {
        delay(simulatedLatencyMillis)
        return PlanInfo(
            carrierName = fixture.carrier,
            planName = fixture.plan,
            fiveGEligible = fixture.fiveGEligible,
            expiry = fixture.expiry,
            verificationSource = PlanVerificationSource.CARRIER_API
        )
    }

    override suspend fun is5GEligible(): Boolean {
        delay(simulatedLatencyMillis)
        return fixture.fiveGEligible
    }

    override suspend fun getPlanExpiry(): String {
        delay(simulatedLatencyMillis)
        return fixture.expiry
    }
}

data class MockPlanFixture(
    val carrier: String,
    val plan: String,
    val fiveGEligible: Boolean,
    val expiry: String,
    val countryIso: String? = null,
    val mcc: String? = null,
    val mnc: String? = null
) {
    companion object {
        fun default() = MockPlanFixture(
            carrier = "Example Telecom",
            plan = "Unlimited 5G",
            fiveGEligible = true,
            expiry = "2026-10-12",
            countryIso = "in",
            mcc = "404",
            mnc = "10"
        )

        /** Used by the "4G device + 5G plan" demo scenario (spec Mode C). */
        fun fiveGPlanOnFourGDevice() = default().copy(plan = "Unlimited 5G", fiveGEligible = true)

        /** Used by the "5G device, no eligible plan" demo scenario. */
        fun noFiveGEligibility() = default().copy(plan = "Standard 4G", fiveGEligible = false)
    }
}
