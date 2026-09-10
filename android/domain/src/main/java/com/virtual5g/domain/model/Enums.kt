package com.virtual5g.domain.model

/**
 * The five (plus offline) operating states the Virtual5GEngine can select.
 *
 * IMPORTANT: VIRTUAL_5G never means "5G radio is active". It always means
 * "software-level optimization is active on a non-5G-NR connection". See
 * docs/limitations.md for the full rationale.
 */
enum class OperatingMode {
    REAL_5G,
    VIRTUAL_5G,
    OPTIMIZED_4G,
    WIFI,
    LIMITED_MODE,
    OFFLINE
}

enum class NetworkQualityTier {
    EXCELLENT,
    GOOD,
    MODERATE,
    POOR,
    CRITICAL
}

enum class ConnectionType {
    WIFI,
    CELLULAR_5G,
    CELLULAR_4G_PLUS,
    CELLULAR_4G,
    CELLULAR_3G_OR_LOWER,
    UNKNOWN,
    NONE
}

/** NR attachment as reported by the platform right now (not a standing hardware claim). */
enum class NrAttachmentState {
    NONE,
    NSA,
    SA,
    ADVANCED,
    UNKNOWN
}

enum class PlanVerificationSource {
    CARRIER_API,
    USER_PROVIDED,
    RECEIPT_UPLOAD,
    UNVERIFIED,
    NONE
}

enum class SpeedTestTier { QUICK, STANDARD, DEEP }

enum class SpeedTestStage { PING, JITTER, DOWNLOAD, UPLOAD, DNS, HTTPS_HANDSHAKE, COMPLETE }
