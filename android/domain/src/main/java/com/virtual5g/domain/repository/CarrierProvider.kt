package com.virtual5g.domain.repository

import com.virtual5g.domain.model.PlanInfo

/**
 * Abstraction for "does this SIM/account have an eligible 5G plan".
 *
 * No real carrier is scraped or integrated in this MVP - see
 * docs/limitations.md. [MockCarrierProvider] in the :data module implements
 * this with the exact JSON shape from the spec, and a real integration can
 * be dropped in later behind the same interface.
 */
interface CarrierProvider {
    suspend fun getCarrierInfo(): CarrierInfo?
    suspend fun getPlanInfo(): PlanInfo?
    suspend fun is5GEligible(): Boolean
    suspend fun getPlanExpiry(): String?
}

data class CarrierInfo(
    val carrierName: String,
    val countryIso: String?,
    val mcc: String?,
    val mnc: String?
)
