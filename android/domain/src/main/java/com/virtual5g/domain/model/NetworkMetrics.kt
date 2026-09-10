package com.virtual5g.domain.model

data class NetworkMetrics(
    val connectionType: ConnectionType,
    val nrAttachment: NrAttachmentState = NrAttachmentState.NONE,
    val downloadMbps: Double? = null,
    val uploadMbps: Double? = null,
    val pingMs: Double? = null,
    val jitterMs: Double? = null,
    val packetLossPercent: Double? = null,
    /** Raw dBm from SignalStrength, where the platform exposes it. */
    val signalStrengthDbm: Int? = null,
    /** 0 (none) .. 4 (great), Android's normalized signal level. */
    val signalLevel: Int? = null,
    val carrierName: String? = null,
    val timestampMillis: Long
)

/**
 * Weights must sum to 1.0. Kept configurable so the scoring formula can be
 * tuned without touching NetworkQualityEngine's logic.
 */
data class ScoreWeights(
    val latency: Double,
    val jitter: Double,
    val packetLoss: Double,
    val throughput: Double,
    val signal: Double,
    val stability: Double
) {
    init {
        val sum = latency + jitter + packetLoss + throughput + signal + stability
        require(sum in 0.99..1.01) { "ScoreWeights must sum to ~1.0, got $sum" }
    }

    companion object {
        val DEFAULT = ScoreWeights(
            latency = 0.20,
            jitter = 0.15,
            packetLoss = 0.20,
            throughput = 0.20,
            signal = 0.15,
            stability = 0.10
        )
    }
}

data class NetworkScoreBreakdown(
    val latencyScore: Int,
    val jitterScore: Int,
    val packetLossScore: Int,
    val throughputScore: Int,
    val signalScore: Int,
    val stabilityScore: Int,
    val total: Int,
    val tier: NetworkQualityTier
)
