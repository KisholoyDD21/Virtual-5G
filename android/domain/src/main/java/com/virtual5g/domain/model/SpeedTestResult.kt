package com.virtual5g.domain.model

data class SpeedTestResult(
    val tier: SpeedTestTier,
    val pingMs: Double?,
    val jitterMs: Double?,
    val downloadMbps: Double?,
    val uploadMbps: Double?,
    val packetLossPercent: Double?,
    val dnsLookupMs: Double?,
    val tcpConnectMs: Double?,
    val httpsHandshakeMs: Double?,
    val serverEndpoint: String,
    val timestampMillis: Long,
    val succeeded: Boolean,
    val errorMessage: String? = null
)

/** Progress emitted while a speed test runs, so the UI can show a live stage indicator. */
data class SpeedTestProgress(
    val stage: SpeedTestStage,
    val partialResult: SpeedTestResult? = null
)
