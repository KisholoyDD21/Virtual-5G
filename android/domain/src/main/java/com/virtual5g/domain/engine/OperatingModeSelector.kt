package com.virtual5g.domain.engine

import com.virtual5g.domain.model.ConnectionType
import com.virtual5g.domain.model.DeviceCapability
import com.virtual5g.domain.model.NetworkMetrics
import com.virtual5g.domain.model.OperatingMode
import com.virtual5g.domain.model.PlanInfo

/**
 * Decides which of the six operating states the app should be in, and why.
 *
 * This is the single source of truth for the "do not claim fake 5G" rule.
 * Every branch is deliberate:
 *  - REAL_5G only fires when the radio is actually attached via NR AND the
 *    device is known to support it.
 *  - VIRTUAL_5G always means "software optimization on a non-NR link" -
 *    covers both the plain 4G-device case (spec Mode A) and the "5G plan on
 *    a 4G device" case (spec Mode C). The explanation text is what tells
 *    those two apart for the user; the mode itself intentionally does not.
 *  - OPTIMIZED_4G is a 5G-capable device with no 5G-eligible plan detected,
 *    or currently outside 5G coverage.
 */
object OperatingModeSelector {

    fun select(device: DeviceCapability, network: NetworkMetrics, plan: PlanInfo?): OperatingMode {
        if (network.connectionType == ConnectionType.NONE) return OperatingMode.OFFLINE
        if (network.connectionType == ConnectionType.WIFI) return OperatingMode.WIFI

        val attachedViaRealNr = network.connectionType == ConnectionType.CELLULAR_5G
        val planEligible = plan?.fiveGEligible == true

        return when {
            attachedViaRealNr && device.supports5GHardware -> OperatingMode.REAL_5G
            !device.supports5GHardware -> OperatingMode.VIRTUAL_5G
            device.supports5GHardware && planEligible && !attachedViaRealNr -> OperatingMode.VIRTUAL_5G
            device.supports5GHardware && !planEligible -> OperatingMode.OPTIMIZED_4G
            else -> OperatingMode.LIMITED_MODE
        }
    }

    fun explanationFor(
        mode: OperatingMode,
        device: DeviceCapability,
        plan: PlanInfo?
    ): String = when (mode) {
        OperatingMode.REAL_5G ->
            "Connected to real 5G. Your device, network attachment, and plan all support genuine 5G NR."

        OperatingMode.VIRTUAL_5G -> when {
            !device.supports5GHardware && plan?.fiveGEligible == true ->
                "5G Plan Detected — Device Limited to 4G. Your recharge may include 5G benefits, but this " +
                    "phone's modem does not support 5G radio connectivity. Virtual 5G software features can " +
                    "still optimize your 4G experience, but real 5G cannot be enabled without compatible hardware."

            !device.supports5GHardware ->
                "This device's modem does not support 5G radio connectivity. Virtual 5G software " +
                    "optimizations (compression, caching, DNS and route selection) are active on your current connection."

            else ->
                "Your plan supports 5G, but you're not currently attached to 5G coverage. Virtual 5G " +
                    "software optimizations are active while you're on 4G."
        }

        OperatingMode.OPTIMIZED_4G ->
            if (device.supports5GHardware) {
                "Your device supports 5G, but no 5G-eligible plan was detected. Running optimized 4G — " +
                    "upgrade your plan to unlock real 5G when you're in coverage."
            } else {
                "Running optimized 4G."
            }

        OperatingMode.WIFI -> "Connected via Wi-Fi. Cellular mode selection does not apply."
        OperatingMode.LIMITED_MODE -> "Limited connectivity or insufficient signal data to determine an optimal mode."
        OperatingMode.OFFLINE -> "No active connection. Showing the last known network measurement."
    }

    /** Short label matching the exact on-screen strings from the spec ("Virtual 5G Active", "5G Connected", ...). */
    fun displayLabelFor(mode: OperatingMode): String = when (mode) {
        OperatingMode.REAL_5G -> "5G Connected"
        OperatingMode.VIRTUAL_5G -> "Virtual 5G Active"
        OperatingMode.OPTIMIZED_4G -> "Optimized 4G"
        OperatingMode.WIFI -> "Wi-Fi Connected"
        OperatingMode.LIMITED_MODE -> "Limited Mode"
        OperatingMode.OFFLINE -> "Offline"
    }
}
