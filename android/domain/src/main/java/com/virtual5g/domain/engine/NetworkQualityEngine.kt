package com.virtual5g.domain.engine

import com.virtual5g.domain.model.ConnectionType
import com.virtual5g.domain.model.NetworkMetrics
import com.virtual5g.domain.model.NetworkQualityTier
import com.virtual5g.domain.model.NetworkScoreBreakdown
import com.virtual5g.domain.model.ScoreWeights
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Converts raw, measured network metrics into a 0-100 "network experience"
 * score and a five-tier label (Excellent..Critical).
 *
 * Pure and side-effect-free by design: every threshold lives in this one
 * file, so the scoring formula can be unit tested exhaustively without any
 * Android dependency. This score describes the *measured software/network
 * experience on the current connection* - it is not a claim about
 * radio-level 5G capability (see ExperienceScore in the domain model and
 * docs/limitations.md).
 */
class NetworkQualityEngine(
    private val weights: ScoreWeights = ScoreWeights.DEFAULT
) {

    fun score(current: NetworkMetrics, recentSamples: List<NetworkMetrics> = emptyList()): NetworkScoreBreakdown {
        val latency = scoreLatency(current.pingMs)
        val jitter = scoreJitter(current.jitterMs)
        val loss = scorePacketLoss(current.packetLossPercent)
        val throughput = scoreThroughput(current.downloadMbps, current.connectionType)
        val signal = scoreSignal(current.signalLevel)
        val stability = scoreStability(recentSamples + current)

        val total = (
            latency * weights.latency +
                jitter * weights.jitter +
                loss * weights.packetLoss +
                throughput * weights.throughput +
                signal * weights.signal +
                stability * weights.stability
            ).roundToInt().coerceIn(0, 100)

        return NetworkScoreBreakdown(
            latencyScore = latency,
            jitterScore = jitter,
            packetLossScore = loss,
            throughputScore = throughput,
            signalScore = signal,
            stabilityScore = stability,
            total = total,
            tier = tierFor(total)
        )
    }

    private fun scoreLatency(pingMs: Double?): Int {
        val p = pingMs ?: return NEUTRAL_SCORE
        return linearFalloff(p, goodAt = 20.0, zeroAt = 250.0)
    }

    private fun scoreJitter(jitterMs: Double?): Int {
        val j = jitterMs ?: return NEUTRAL_SCORE
        return linearFalloff(j, goodAt = 5.0, zeroAt = 100.0)
    }

    private fun scorePacketLoss(lossPercent: Double?): Int {
        val l = lossPercent ?: return NEUTRAL_SCORE
        return linearFalloff(l, goodAt = 0.0, zeroAt = 5.0)
    }

    private fun scoreThroughput(downloadMbps: Double?, type: ConnectionType): Int {
        val d = downloadMbps ?: return NEUTRAL_SCORE
        val ceiling = when (type) {
            ConnectionType.CELLULAR_5G -> 300.0
            ConnectionType.WIFI -> 150.0
            ConnectionType.CELLULAR_4G_PLUS -> 80.0
            ConnectionType.CELLULAR_4G -> 40.0
            ConnectionType.CELLULAR_3G_OR_LOWER -> 8.0
            ConnectionType.UNKNOWN, ConnectionType.NONE -> 20.0
        }
        return ((d / ceiling) * 100).roundToInt().coerceIn(0, 100)
    }

    private fun scoreSignal(level: Int?): Int {
        val l = level ?: return NEUTRAL_SCORE
        return (l.coerceIn(0, 4) * 25)
    }

    /** Coefficient-of-variation of recent ping samples: steadier ping = higher stability score. */
    private fun scoreStability(samples: List<NetworkMetrics>): Int {
        val pings = samples.mapNotNull { it.pingMs }
        if (pings.size < 2) return 75 // not enough history yet - assume reasonable stability
        val mean = pings.average()
        if (mean <= 0.0) return 75
        val variance = pings.sumOf { (it - mean) * (it - mean) } / pings.size
        val coefficientOfVariation = sqrt(variance) / mean
        return (100 - (coefficientOfVariation * 200)).roundToInt().coerceIn(0, 100)
    }

    /** 100 at/below [goodAt], 0 at/above [zeroAt], straight line between. */
    private fun linearFalloff(value: Double, goodAt: Double, zeroAt: Double): Int = when {
        value <= goodAt -> 100
        value >= zeroAt -> 0
        else -> (100 - ((value - goodAt) / (zeroAt - goodAt) * 100)).roundToInt()
    }.coerceIn(0, 100)

    private fun tierFor(total: Int): NetworkQualityTier = when {
        total >= 90 -> NetworkQualityTier.EXCELLENT
        total >= 75 -> NetworkQualityTier.GOOD
        total >= 50 -> NetworkQualityTier.MODERATE
        total >= 25 -> NetworkQualityTier.POOR
        else -> NetworkQualityTier.CRITICAL
    }

    private companion object {
        /** Used when a metric is unavailable - keeps a missing reading from tanking the score. */
        const val NEUTRAL_SCORE = 50
    }
}
