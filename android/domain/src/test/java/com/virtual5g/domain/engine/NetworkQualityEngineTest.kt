package com.virtual5g.domain.engine

import com.google.common.truth.Truth.assertThat
import com.virtual5g.domain.model.ConnectionType
import com.virtual5g.domain.model.NetworkMetrics
import com.virtual5g.domain.model.NetworkQualityTier
import org.junit.Test

class NetworkQualityEngineTest {

    private val engine = NetworkQualityEngine()

    private fun metrics(
        connectionType: ConnectionType = ConnectionType.CELLULAR_5G,
        ping: Double? = 15.0,
        jitter: Double? = 3.0,
        loss: Double? = 0.0,
        download: Double? = 250.0,
        signalLevel: Int? = 4
    ) = NetworkMetrics(
        connectionType = connectionType,
        pingMs = ping,
        jitterMs = jitter,
        packetLossPercent = loss,
        downloadMbps = download,
        signalLevel = signalLevel,
        timestampMillis = 0L
    )

    @Test
    fun `excellent conditions score in the excellent tier`() {
        val result = engine.score(metrics())
        assertThat(result.total).isAtLeast(90)
        assertThat(result.tier).isEqualTo(NetworkQualityTier.EXCELLENT)
    }

    @Test
    fun `high latency and packet loss pull the score down to poor or critical`() {
        val result = engine.score(
            metrics(ping = 240.0, jitter = 95.0, loss = 4.5, download = 2.0, signalLevel = 1)
        )
        assertThat(result.total).isLessThan(50)
        assertThat(result.tier).isAnyOf(NetworkQualityTier.POOR, NetworkQualityTier.CRITICAL)
    }

    @Test
    fun `total score is always clamped between 0 and 100`() {
        val worst = engine.score(metrics(ping = 999.0, jitter = 999.0, loss = 100.0, download = 0.0, signalLevel = 0))
        val best = engine.score(metrics(ping = 0.0, jitter = 0.0, loss = 0.0, download = 10_000.0, signalLevel = 4))
        assertThat(worst.total).isAtLeast(0)
        assertThat(best.total).isAtMost(100)
    }

    @Test
    fun `missing metrics fall back to a neutral score instead of crashing or zeroing`() {
        val result = engine.score(
            NetworkMetrics(
                connectionType = ConnectionType.CELLULAR_4G,
                pingMs = null,
                jitterMs = null,
                packetLossPercent = null,
                downloadMbps = null,
                signalLevel = null,
                timestampMillis = 0L
            )
        )
        // All-unknown metrics should land near the neutral midpoint, not at 0 or 100.
        assertThat(result.total).isIn(30..70)
    }

    @Test
    fun `throughput ceiling is connection-type aware`() {
        val fiveG = engine.score(metrics(connectionType = ConnectionType.CELLULAR_5G, download = 100.0))
        val fourG = engine.score(metrics(connectionType = ConnectionType.CELLULAR_4G, download = 100.0))
        // The same 100 Mbps download is "good" on 5G but saturates (and caps at 100) on 4G.
        assertThat(fourG.throughputScore).isEqualTo(100)
        assertThat(fiveG.throughputScore).isLessThan(fourG.throughputScore)
    }

    @Test
    fun `stable recent samples score higher on stability than wildly varying ones`() {
        val stableHistory = List(5) { metrics(ping = 20.0) }
        val volatileHistory = listOf(
            metrics(ping = 10.0), metrics(ping = 200.0), metrics(ping = 15.0), metrics(ping = 180.0)
        )
        val stable = engine.score(metrics(ping = 20.0), stableHistory)
        val volatile = engine.score(metrics(ping = 20.0), volatileHistory)
        assertThat(stable.stabilityScore).isGreaterThan(volatile.stabilityScore)
    }
}
