package com.virtual5g.data.telephony

import com.virtual5g.core.logging.SecureLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class LatencySample(val pingMs: Double?, val jitterMs: Double?)

/**
 * Pings a tiny HEAD request on a low-frequency ticker so the dashboard has
 * a live ping/jitter reading without the user running a manual speed test.
 * Deliberately lightweight: HEAD request, no body, capped interval, and it
 * backs off silently on failure rather than retrying aggressively - this
 * must never become a meaningful drain on the user's data plan.
 */
class LightweightLatencyProbe(
    private val endpoint: String = DEFAULT_ENDPOINT,
    private val intervalMillis: Long = 15_000L
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .callTimeout(4, TimeUnit.SECONDS)
        .build()

    fun observe(): Flow<LatencySample> = flow {
        val recentPings = ArrayDeque<Double>()
        while (true) {
            val ping = measureOnce()
            if (ping != null) {
                if (recentPings.size == HISTORY_SIZE) recentPings.removeFirst()
                recentPings.addLast(ping)
            }
            emit(LatencySample(pingMs = ping, jitterMs = jitterFrom(recentPings)))
            delay(intervalMillis)
        }
    }

    private fun measureOnce(): Double? {
        val request = Request.Builder().url(endpoint).head().build()
        val start = System.nanoTime()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
            }
            (System.nanoTime() - start) / 1_000_000.0
        } catch (error: Exception) {
            SecureLogger.d("LightweightLatencyProbe", "probe failed: ${error.javaClass.simpleName}")
            null
        }
    }

    private fun jitterFrom(samples: ArrayDeque<Double>): Double? {
        if (samples.size < 2) return null
        val diffs = samples.zipWithNext { a, b -> kotlin.math.abs(b - a) }
        return diffs.average()
    }

    private companion object {
        /** Fixed-size rolling window so ping history never grows unbounded. */
        const val HISTORY_SIZE = 6
        const val DEFAULT_ENDPOINT = "https://www.gstatic.com/generate_204"
    }
}
