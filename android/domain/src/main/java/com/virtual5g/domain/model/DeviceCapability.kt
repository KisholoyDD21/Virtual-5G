package com.virtual5g.domain.model

/**
 * What we know about this device and how confident we are in it.
 *
 * There is no public Android API that answers "does this modem support 5G
 * NR" as a standing hardware fact independent of current coverage. The
 * platform only exposes what the device is doing *right now*
 * (TelephonyDisplayInfo / ServiceState). So [supports5GHardware] is an
 * inferred, persisted value: true the first time we ever observe an NR
 * attach on this device, unknown/false until then. [confidence] tells the
 * UI and the engine how much to trust it. See docs/limitations.md.
 */
data class DeviceCapability(
    val androidSdkInt: Int,
    val androidRelease: String,
    val manufacturer: String,
    val model: String,
    val supports5GHardware: Boolean,
    val capabilityConfidence: CapabilityConfidence,
    val supportedNetworkTypeNames: List<String>
)

enum class CapabilityConfidence {
    /** We have directly observed this device attach via 5G NR at least once. */
    CONFIRMED_BY_OBSERVATION,
    /** Never observed NR; device may still support it but has had no 5G coverage yet. */
    UNKNOWN_NO_COVERAGE_OBSERVED,
    /** Telephony permission was denied, so we could not observe anything. */
    UNKNOWN_PERMISSION_DENIED
}
