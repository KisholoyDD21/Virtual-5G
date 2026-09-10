package com.virtual5g.domain.engine

import com.virtual5g.domain.model.NetworkMetrics
import com.virtual5g.domain.model.OperatingMode

enum class RecommendationSeverity { INFO, SUGGESTED, IMPORTANT }

data class OptimizationRecommendation(
    val title: String,
    val detail: String,
    val severity: RecommendationSeverity
)

/**
 * Produces human-readable, application-level recommendations from measured
 * conditions. Deliberately limited to things a normal app is allowed to do:
 * no traffic interception, no packet manipulation, no carrier bypass, no
 * VPN-based MITM. See docs/limitations.md for what was intentionally left
 * out and why.
 */
object OptimizationEngine {

    fun recommendationsFor(metrics: NetworkMetrics, mode: OperatingMode): List<OptimizationRecommendation> {
        val recs = mutableListOf<OptimizationRecommendation>()

        metrics.pingMs?.let { ping ->
            if (ping > 150) {
                recs += OptimizationRecommendation(
                    title = "High latency detected",
                    detail = "Prefer lower-latency endpoints and enable QUIC/HTTP-3 where the server supports it.",
                    severity = RecommendationSeverity.IMPORTANT
                )
            }
        }

        metrics.jitterMs?.let { jitter ->
            if (jitter > 30) {
                recs += OptimizationRecommendation(
                    title = "Unstable connection (high jitter)",
                    detail = "Real-time features (calls, live data) may stutter. Consider Wi-Fi if available.",
                    severity = RecommendationSeverity.SUGGESTED
                )
            }
        }

        metrics.packetLossPercent?.let { loss ->
            if (loss > 1.0) {
                recs += OptimizationRecommendation(
                    title = "Packet loss detected",
                    detail = "Reduce concurrent background transfers and retry failed requests with backoff.",
                    severity = RecommendationSeverity.IMPORTANT
                )
            }
        }

        metrics.downloadMbps?.let { down ->
            if (down < 5.0) {
                recs += OptimizationRecommendation(
                    title = "Low throughput",
                    detail = "Lower streaming/image quality automatically and defer non-essential downloads.",
                    severity = RecommendationSeverity.SUGGESTED
                )
            }
        }

        if (mode == OperatingMode.VIRTUAL_5G || mode == OperatingMode.OPTIMIZED_4G) {
            recs += OptimizationRecommendation(
                title = "Background sync minimized",
                detail = "Non-urgent background sync is deferred to preserve foreground bandwidth.",
                severity = RecommendationSeverity.INFO
            )
        }

        if (recs.isEmpty()) {
            recs += OptimizationRecommendation(
                title = "Connection healthy",
                detail = "No optimization action needed right now.",
                severity = RecommendationSeverity.INFO
            )
        }

        return recs
    }

    /** Fixed list of always-on optimizations shown on the Optimization Card. */
    fun activeOptimizations(): List<String> = listOf(
        "Latency-aware endpoint selection",
        "DNS optimization",
        "Adaptive networking",
        "Response caching"
    )
}
