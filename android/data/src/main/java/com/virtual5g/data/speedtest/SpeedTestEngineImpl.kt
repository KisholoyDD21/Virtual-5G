package com.virtual5g.data.speedtest

import com.virtual5g.core.logging.SecureLogger
import com.virtual5g.domain.model.SpeedTestProgress
import com.virtual5g.domain.model.SpeedTestResult
import com.virtual5g.domain.model.SpeedTestStage
import com.virtual5g.domain.model.SpeedTestTier
import com.virtual5g.domain.repository.SpeedTestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Real network measurement using OkHttp. Timing phases (DNS/connect/TLS)
 * come from an EventListener rather than manual socket code, which is the
 * accurate, supported way to get this granularity in Android's HTTP stack.
 *
 * Every test respects the requested tier's byte budget (spec section 9:
 * "avoid wasting the user's data plan") and never retries aggressively on
 * failure - a failed stage just reports null for that field rather than
 * looping.
 */
class SpeedTestEngineImpl(
    private val endpoints: SpeedTestEndpoints = SpeedTestEndpoints()
) : SpeedTestRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun runTest(tier: SpeedTestTier): Flow<SpeedTestProgress> = flow {
        val timings = PhaseTimings()
        val instrumentedClient = client.newBuilder()
            .eventListenerFactory(timings.listener())
            .build()

        emit(SpeedTestProgress(SpeedTestStage.DNS))
        emit(SpeedTestProgress(SpeedTestStage.HTTPS_HANDSHAKE))

        emit(SpeedTestProgress(SpeedTestStage.PING))
        val pingSamples = measurePings(instrumentedClient, sampleCount = if (tier == SpeedTestTier.QUICK) 3 else 5)
        val ping = pingSamples.filterNotNull().minOrNull()
        val jitter = jitterFrom(pingSamples.filterNotNull())

        emit(SpeedTestProgress(SpeedTestStage.DOWNLOAD))
        val downloadMbps = measureDownload(instrumentedClient, tier)

        emit(SpeedTestProgress(SpeedTestStage.UPLOAD))
        val uploadMbps = measureUpload(instrumentedClient, tier)

        val packetLoss = estimatePacketLoss(pingSamples)

        val result = SpeedTestResult(
            tier = tier,
            pingMs = ping,
            jitterMs = jitter,
            downloadMbps = downloadMbps,
            uploadMbps = uploadMbps,
            packetLossPercent = packetLoss,
            dnsLookupMs = timings.dnsMs,
            tcpConnectMs = timings.connectMs,
            httpsHandshakeMs = timings.tlsMs,
            serverEndpoint = endpoints.downloadUrl,
            timestampMillis = System.currentTimeMillis(),
            succeeded = downloadMbps != null || ping != null
        )
        emit(SpeedTestProgress(SpeedTestStage.COMPLETE, result))
    }.flowOn(Dispatchers.IO)

    private fun measurePings(client: OkHttpClient, sampleCount: Int): List<Double?> =
        (1..sampleCount).map { timeRequest(client, Request.Builder().url(endpoints.pingUrl).head().build()) }

    private fun measureDownload(client: OkHttpClient, tier: SpeedTestTier): Double? {
        val request = Request.Builder().url(endpoints.downloadUrlFor(tier)).get().build()
        val start = System.nanoTime()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val bytes = response.body?.bytes()?.size ?: return null
                val seconds = (System.nanoTime() - start) / 1_000_000_000.0
                if (seconds <= 0.0) return null
                (bytes * 8.0 / 1_000_000.0) / seconds
            }
        } catch (error: IOException) {
            SecureLogger.w("SpeedTestEngine", "download measurement failed", error)
            null
        }
    }

    private fun measureUpload(client: OkHttpClient, tier: SpeedTestTier): Double? {
        val payload = ByteArray(endpoints.uploadBytesFor(tier).toInt())
        val body = payload.toRequestBody("application/octet-stream".toMediaType())
        val request = Request.Builder().url(endpoints.uploadUrl).post(body).build()
        val start = System.nanoTime()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val seconds = (System.nanoTime() - start) / 1_000_000_000.0
                if (seconds <= 0.0) return null
                (payload.size * 8.0 / 1_000_000.0) / seconds
            }
        } catch (error: IOException) {
            SecureLogger.w("SpeedTestEngine", "upload measurement failed", error)
            null
        }
    }

    private fun timeRequest(client: OkHttpClient, request: Request): Double? {
        val start = System.nanoTime()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
            }
            (System.nanoTime() - start) / 1_000_000.0
        } catch (error: IOException) {
            null
        }
    }

    private fun jitterFrom(samples: List<Double>): Double? {
        if (samples.size < 2) return null
        return samples.zipWithNext { a, b -> abs(b - a) }.average()
    }

    /** Failed/timed-out pings among the sample set, as a rough packet-loss proxy. */
    private fun estimatePacketLoss(samples: List<Double?>): Double? {
        if (samples.isEmpty()) return null
        val failures = samples.count { it == null }
        return (failures.toDouble() / samples.size) * 100.0
    }
}

/** Captures DNS/connect/TLS phase durations for the most recent call via OkHttp's EventListener. */
private class PhaseTimings {
    var dnsMs: Double? = null
    var connectMs: Double? = null
    var tlsMs: Double? = null

    private var dnsStart = 0L
    private var connectStart = 0L
    private var secureStart = 0L

    fun listener(): EventListener.Factory = EventListener.Factory { call: Call ->
        object : EventListener() {
            override fun dnsStart(call: Call, domainName: String) {
                dnsStart = System.nanoTime()
            }

            override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<java.net.InetAddress>) {
                dnsMs = (System.nanoTime() - dnsStart) / 1_000_000.0
            }

            override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
                connectStart = System.nanoTime()
            }

            override fun connectEnd(
                call: Call,
                inetSocketAddress: InetSocketAddress,
                proxy: Proxy,
                protocol: okhttp3.Protocol?
            ) {
                connectMs = (System.nanoTime() - connectStart) / 1_000_000.0
            }

            override fun secureConnectStart(call: Call) {
                secureStart = System.nanoTime()
            }

            override fun secureConnectEnd(call: Call, handshake: okhttp3.Handshake?) {
                tlsMs = (System.nanoTime() - secureStart) / 1_000_000.0
            }
        }
    }
}
