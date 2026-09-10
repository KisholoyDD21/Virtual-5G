package com.virtual5g.data.telephony

import android.content.Context
import android.os.Build
import com.virtual5g.core.util.PermissionUtils
import com.virtual5g.data.local.SecureFlagStore
import com.virtual5g.domain.model.CapabilityConfidence
import com.virtual5g.domain.model.DeviceCapability
import com.virtual5g.domain.repository.DeviceRepository

/**
 * Implements DeviceRepository using android.os.Build (always available, no
 * permission needed) plus a persisted "have we ever observed this device
 * attach via 5G NR" flag.
 *
 * Why persisted rather than queried live: Android has no public,
 * non-privileged API that answers "does this modem support 5G NR" as a
 * standing hardware fact independent of current coverage. The platform
 * only exposes what's happening right now (TelephonyDisplayInfo /
 * ServiceState, see CellularTelephonyObserver). A device sitting in a
 * basement with no 5G coverage would otherwise be wrongly reported as
 * "no 5G hardware". See docs/limitations.md for the full writeup.
 */
class DeviceCapabilityManager(
    private val context: Context,
    private val secureFlagStore: SecureFlagStore
) : DeviceRepository {

    override fun hasTelephonyPermission(): Boolean =
        PermissionUtils.hasBasicOrFullPhoneStatePermission(context)

    override fun getDeviceCapability(): DeviceCapability {
        val everObserved = secureFlagStore.hasEverObservedNrAttach()
        val confidence = when {
            everObserved -> CapabilityConfidence.CONFIRMED_BY_OBSERVATION
            !hasTelephonyPermission() -> CapabilityConfidence.UNKNOWN_PERMISSION_DENIED
            else -> CapabilityConfidence.UNKNOWN_NO_COVERAGE_OBSERVED
        }

        return DeviceCapability(
            androidSdkInt = Build.VERSION.SDK_INT,
            androidRelease = Build.VERSION.RELEASE ?: "unknown",
            manufacturer = Build.MANUFACTURER ?: "unknown",
            model = Build.MODEL ?: "unknown",
            supports5GHardware = everObserved,
            capabilityConfidence = confidence,
            supportedNetworkTypeNames = supportedNetworkTypeNames(everObserved)
        )
    }

    /** Called by CellularTelephonyObserver the moment it sees a genuine NR attach. */
    fun recordNrAttachObserved() {
        secureFlagStore.markNrAttachObserved()
    }

    private fun supportedNetworkTypeNames(everObservedNr: Boolean): List<String> {
        // We only claim what we can back up: LTE is assumed present on any
        // modern telephony-capable device; NR is listed only once actually
        // observed, never inferred from model name or marketing specs.
        val types = mutableListOf("LTE")
        if (everObservedNr) types += "NR (5G)"
        return types
    }
}
