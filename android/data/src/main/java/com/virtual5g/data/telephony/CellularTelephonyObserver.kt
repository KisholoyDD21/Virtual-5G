package com.virtual5g.data.telephony

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.virtual5g.core.logging.SecureLogger
import com.virtual5g.core.util.PermissionUtils
import com.virtual5g.domain.model.NrAttachmentState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class CellularReading(
    val nrAttachment: NrAttachmentState,
    val signalLevel: Int?,
    val signalDbm: Int?,
    val carrierName: String?
)

/**
 * Facade that picks the right implementation for the running OS version at
 * construction time and delegates to it. This is deliberate: each nested
 * source class below only references telephony types available at *its own*
 * minimum API level, so a class that touches TelephonyDisplayInfo or
 * TelephonyCallback (both API 30/31+) is never instantiated - and therefore
 * never class-loaded or verified - on an older device. Mixing those
 * references into one class with runtime `if (SDK_INT >= X)` branches is a
 * known source of verifier problems on some OS/ART versions; separate
 * classes selected at construction time avoid that entirely (spec section 3:
 * "clearly isolate compatibility code").
 */
class CellularTelephonyObserver(private val context: Context) {

    private val telephonyManager =
        context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    fun observe(): Flow<CellularReading> {
        val tm = telephonyManager
        if (tm == null || !PermissionUtils.hasBasicOrFullPhoneStatePermission(context)) {
            return permissionOrHardwareUnavailableFlow()
        }
        val source: CellularSignalSource = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> ModernTelephonyCallbackSource(context, tm)
            Build.VERSION.SDK_INT == Build.VERSION_CODES.R -> DisplayInfoPhoneStateListenerSource(tm)
            else -> SignalOnlyPhoneStateListenerSource(tm)
        }
        return source.observe()
    }

    private fun permissionOrHardwareUnavailableFlow(): Flow<CellularReading> = callbackFlow {
        trySend(CellularReading(NrAttachmentState.UNKNOWN, signalLevel = null, signalDbm = null, carrierName = null))
        awaitClose { }
    }
}

private interface CellularSignalSource {
    fun observe(): Flow<CellularReading>
}

/** API 31+ (Android 12): the current, non-deprecated path. */
@SuppressLint("MissingPermission")
private class ModernTelephonyCallbackSource(
    private val context: Context,
    private val telephonyManager: TelephonyManager
) : CellularSignalSource {

    override fun observe(): Flow<CellularReading> = callbackFlow {
        val carrierName = telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() }
        var lastNr = NrAttachmentState.NONE

        val callback = object :
            TelephonyCallback(),
            TelephonyCallback.DisplayInfoListener,
            TelephonyCallback.SignalStrengthsListener {

            override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                lastNr = nrStateFromDisplayInfo(telephonyDisplayInfo, telephonyManager)
                trySend(CellularReading(lastNr, null, null, carrierName))
            }

            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                trySend(
                    CellularReading(
                        nrAttachment = nrStateFromDataNetworkType(telephonyManager),
                        signalLevel = runCatching { signalStrength.level }.getOrNull(),
                        signalDbm = extractDbm(signalStrength),
                        carrierName = carrierName
                    )
                )
            }
        }

        runCatching {
            telephonyManager.registerTelephonyCallback(ContextCompat.getMainExecutor(context), callback)
        }.onFailure { error ->
            SecureLogger.e("TelephonyCallbackSource", "registerTelephonyCallback failed", error)
            trySend(CellularReading(NrAttachmentState.UNKNOWN, null, null, carrierName))
        }

        awaitClose { runCatching { telephonyManager.unregisterTelephonyCallback(callback) } }
    }
}

/** API 30 (Android 11) only: TelephonyDisplayInfo exists, but via the deprecated PhoneStateListener. */
@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
private class DisplayInfoPhoneStateListenerSource(
    private val telephonyManager: TelephonyManager
) : CellularSignalSource {

    override fun observe(): Flow<CellularReading> = callbackFlow {
        val carrierName = telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() }

        val listener = object : PhoneStateListener() {
            override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                trySend(CellularReading(nrStateFromDisplayInfo(telephonyDisplayInfo, telephonyManager), null, null, carrierName))
            }

            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                trySend(
                    CellularReading(
                        nrAttachment = nrStateFromDataNetworkType(telephonyManager),
                        signalLevel = runCatching { signalStrength.level }.getOrNull(),
                        signalDbm = extractDbm(signalStrength),
                        carrierName = carrierName
                    )
                )
            }
        }

        val events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED
        runCatching { telephonyManager.listen(listener, events) }
            .onFailure { error -> SecureLogger.e("DisplayInfoPSLSource", "listen failed", error) }

        awaitClose { runCatching { telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE) } }
    }
}

/**
 * API 26-29: no TelephonyDisplayInfo at all. NR is only detectable here via
 * TelephonyManager.dataNetworkType == NETWORK_TYPE_NR (standalone 5G,
 * available since API 29); on API 26-28 NR is simply unreachable and we
 * always report NONE/UNKNOWN, which is honest given the platform's own
 * limits at that API level.
 */
@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
private class SignalOnlyPhoneStateListenerSource(
    private val telephonyManager: TelephonyManager
) : CellularSignalSource {

    override fun observe(): Flow<CellularReading> = callbackFlow {
        val carrierName = telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() }

        val listener = object : PhoneStateListener() {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                trySend(
                    CellularReading(
                        nrAttachment = nrStateFromDataNetworkType(telephonyManager),
                        signalLevel = runCatching { signalStrength.level }.getOrNull(),
                        signalDbm = extractDbm(signalStrength),
                        carrierName = carrierName
                    )
                )
            }
        }

        runCatching { telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS) }
            .onFailure { error -> SecureLogger.e("SignalOnlyPSLSource", "listen failed", error) }

        awaitClose { runCatching { telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE) } }
    }
}

@SuppressLint("MissingPermission")
private fun nrStateFromDisplayInfo(displayInfo: TelephonyDisplayInfo, tm: TelephonyManager): NrAttachmentState =
    when (displayInfo.overrideNetworkType) {
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> NrAttachmentState.NSA
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> NrAttachmentState.ADVANCED
        else -> nrStateFromDataNetworkType(tm)
    }

@SuppressLint("MissingPermission")
private fun nrStateFromDataNetworkType(tm: TelephonyManager): NrAttachmentState = runCatching {
    if (tm.dataNetworkType == TelephonyManager.NETWORK_TYPE_NR) NrAttachmentState.SA else NrAttachmentState.NONE
}.getOrDefault(NrAttachmentState.UNKNOWN)

/** Real per-cell dBm, not derived/guessed from level. Only available from API 29's getCellSignalStrengths(). */
private fun extractDbm(signalStrength: SignalStrength?): Int? {
    if (signalStrength == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    return runCatching { signalStrength.cellSignalStrengths.firstOrNull()?.dbm }.getOrNull()
}
