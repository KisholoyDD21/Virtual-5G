package com.virtual5g.domain.model

data class PlanInfo(
    val carrierName: String?,
    val planName: String?,
    val fiveGEligible: Boolean,
    val expiry: String?,
    val verificationSource: PlanVerificationSource
)

/** One row of the "what works without real 5G hardware" comparison table. */
data class CapabilityComparisonRow(
    val capability: String,
    val available: Boolean,
    val note: String? = null
)

fun defaultCapabilityComparison(deviceSupports5G: Boolean): List<CapabilityComparisonRow> = listOf(
    CapabilityComparisonRow("Virtual 5G optimization", available = true),
    CapabilityComparisonRow("Network diagnostics", available = true),
    CapabilityComparisonRow("Speed monitoring", available = true),
    CapabilityComparisonRow("Latency optimization", available = true, note = "where technically possible"),
    CapabilityComparisonRow("Real 5G radio connection", available = deviceSupports5G, note = if (!deviceSupports5G) "requires 5G-capable hardware" else null),
    CapabilityComparisonRow("5G NR", available = deviceSupports5G),
    CapabilityComparisonRow("Hardware-level 5G", available = deviceSupports5G)
)
