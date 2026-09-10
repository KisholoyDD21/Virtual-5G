package com.virtual5g.domain.repository

import com.virtual5g.domain.model.SpeedTestProgress
import com.virtual5g.domain.model.SpeedTestTier
import kotlinx.coroutines.flow.Flow

interface SpeedTestRepository {
    /** Emits progress as each stage completes, finishing with a SpeedTestProgress(COMPLETE, result). */
    fun runTest(tier: SpeedTestTier): Flow<SpeedTestProgress>
}
