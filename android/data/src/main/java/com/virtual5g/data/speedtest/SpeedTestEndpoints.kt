package com.virtual5g.data.speedtest

import com.virtual5g.domain.model.SpeedTestTier

/**
 * Endpoints are intentionally not hardcoded deep into the engine - the
 * backend's GET /speedtest/config exposes this same shape, so ops can point
 * the fleet at a different CDN/region without an app release. Defaults use
 * Cloudflare's public speed-test endpoints as a reasonable out-of-the-box
 * value; production deployments should point downloadUrl/uploadUrl at
 * infrastructure you control so results are reproducible per spec section 9.
 */
data class SpeedTestEndpoints(
    val pingUrl: String = "https://speed.cloudflare.com/__down?bytes=0",
    val downloadUrl: String = "https://speed.cloudflare.com/__down",
    val uploadUrl: String = "https://speed.cloudflare.com/__up",
    val dnsProbeHost: String = "speed.cloudflare.com"
) {
    fun downloadBytesFor(tier: SpeedTestTier): Long = when (tier) {
        SpeedTestTier.QUICK -> 2_000_000L // ~2 MB
        SpeedTestTier.STANDARD -> 10_000_000L // ~10 MB
        SpeedTestTier.DEEP -> 25_000_000L // ~25 MB
    }

    fun uploadBytesFor(tier: SpeedTestTier): Long = when (tier) {
        SpeedTestTier.QUICK -> 1_000_000L
        SpeedTestTier.STANDARD -> 5_000_000L
        SpeedTestTier.DEEP -> 10_000_000L
    }

    fun downloadUrlFor(tier: SpeedTestTier): String = "$downloadUrl?bytes=${downloadBytesFor(tier)}"
}
