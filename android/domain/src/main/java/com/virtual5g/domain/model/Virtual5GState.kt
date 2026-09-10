package com.virtual5g.domain.model

/** The single object the Dashboard (and every other screen) renders from. */
data class Virtual5GState(
    val mode: OperatingMode,
    val device: DeviceCapability,
    val network: NetworkMetrics,
    val plan: PlanInfo?,
    val score: NetworkScoreBreakdown,
    val explanation: String,
    val isOffline: Boolean = false,
    val lastUpdatedMillis: Long
)

/** The 5-number "Virtual 5G Experience Score" breakdown shown on the dashboard. */
data class ExperienceScore(
    val overall: Int,
    val speed: Int,
    val latency: Int,
    val stability: Int,
    val signal: Int,
    val jitter: Int
) {
    companion object {
        fun from(breakdown: NetworkScoreBreakdown): ExperienceScore = ExperienceScore(
            overall = breakdown.total,
            speed = breakdown.throughputScore,
            latency = breakdown.latencyScore,
            stability = breakdown.stabilityScore,
            signal = breakdown.signalScore,
            jitter = breakdown.jitterScore
        )
    }
}
