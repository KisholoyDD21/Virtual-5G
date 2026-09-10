package com.virtual5g.domain.usecase

import com.virtual5g.domain.model.PlanInfo
import com.virtual5g.domain.model.PlanVerificationSource
import com.virtual5g.domain.model.SpeedTestProgress
import com.virtual5g.domain.model.SpeedTestTier
import com.virtual5g.domain.repository.CarrierProvider
import com.virtual5g.domain.repository.SpeedTestRepository
import kotlinx.coroutines.flow.Flow

class RunSpeedTestUseCase(private val speedTestRepository: SpeedTestRepository) {
    operator fun invoke(tier: SpeedTestTier): Flow<SpeedTestProgress> = speedTestRepository.runTest(tier)
}

class GetPlanInfoUseCase(private val carrierProvider: CarrierProvider) {
    suspend operator fun invoke(): PlanInfo? = carrierProvider.getPlanInfo()
}

/**
 * Applies a user-entered plan selection. This never claims carrier-verified
 * status - it is always tagged USER_PROVIDED (or RECEIPT_UPLOAD) so the UI
 * can render "Verification: User Provided" per spec section 4.
 */
class SubmitUserProvidedPlanUseCase {
    operator fun invoke(carrierName: String, planName: String, fiveGEligible: Boolean, expiry: String?): PlanInfo =
        PlanInfo(
            carrierName = carrierName,
            planName = planName,
            fiveGEligible = fiveGEligible,
            expiry = expiry,
            verificationSource = PlanVerificationSource.USER_PROVIDED
        )
}
