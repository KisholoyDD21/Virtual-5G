package com.virtual5g.data.repository

import com.virtual5g.data.local.NetworkMeasurementDao
import com.virtual5g.data.local.NetworkMeasurementEntity
import com.virtual5g.data.telephony.CellularTelephonyObserver
import com.virtual5g.data.telephony.ConnectivityObserver
import com.virtual5g.data.telephony.DeviceCapabilityManager
import com.virtual5g.data.telephony.LightweightLatencyProbe
import com.virtual5g.data.telephony.Transport
import com.virtual5g.domain.model.ConnectionType
import com.virtual5g.domain.model.NetworkMetrics
import com.virtual5g.domain.model.NrAttachmentState
import com.virtual5g.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class NetworkRepositoryImpl(
    private val connectivityObserver: ConnectivityObserver,
    private val cellularTelephonyObserver: CellularTelephonyObserver,
    private val latencyProbe: LightweightLatencyProbe,
    private val deviceCapabilityManager: DeviceCapabilityManager,
    private val measurementDao: NetworkMeasurementDao
) : NetworkRepository {

    override fun observeNetworkMetrics(): Flow<NetworkMetrics> =
        combine(
            connectivityObserver.observe(),
            cellularTelephonyObserver.observe(),
            latencyProbe.observe()
        ) { transport, cellular, latency ->
            if (cellular.nrAttachment == NrAttachmentState.NSA ||
                cellular.nrAttachment == NrAttachmentState.SA ||
                cellular.nrAttachment == NrAttachmentState.ADVANCED
            ) {
                deviceCapabilityManager.recordNrAttachObserved()
            }

            NetworkMetrics(
                connectionType = resolveConnectionType(transport, cellular.nrAttachment),
                nrAttachment = cellular.nrAttachment,
                downloadMbps = null, // populated by SpeedTestEngine, not passive monitoring
                uploadMbps = null,
                pingMs = latency.pingMs,
                jitterMs = latency.jitterMs,
                packetLossPercent = null,
                signalStrengthDbm = cellular.signalDbm,
                signalLevel = cellular.signalLevel,
                carrierName = cellular.carrierName,
                timestampMillis = System.currentTimeMillis()
            )
        }
            .distinctUntilChanged()
            .onEach { saveLastKnownMetrics(it) }

    override suspend fun getLastKnownMetrics(): NetworkMetrics? =
        measurementDao.getLast()?.toDomain()

    override suspend fun saveLastKnownMetrics(metrics: NetworkMetrics) {
        measurementDao.upsert(metrics.toEntity())
    }

    private fun resolveConnectionType(transport: Transport, nr: NrAttachmentState): ConnectionType = when (transport) {
        Transport.WIFI -> ConnectionType.WIFI
        Transport.NONE -> ConnectionType.NONE
        Transport.CELLULAR -> when (nr) {
            NrAttachmentState.NSA, NrAttachmentState.SA, NrAttachmentState.ADVANCED -> ConnectionType.CELLULAR_5G
            NrAttachmentState.NONE, NrAttachmentState.UNKNOWN -> ConnectionType.CELLULAR_4G
        }
        Transport.OTHER -> ConnectionType.UNKNOWN
    }
}

private fun NetworkMetrics.toEntity() = NetworkMeasurementEntity(
    connectionType = connectionType.name,
    nrAttachment = nrAttachment.name,
    downloadMbps = downloadMbps,
    uploadMbps = uploadMbps,
    pingMs = pingMs,
    jitterMs = jitterMs,
    packetLossPercent = packetLossPercent,
    signalStrengthDbm = signalStrengthDbm,
    signalLevel = signalLevel,
    carrierName = carrierName,
    timestampMillis = timestampMillis
)

private fun NetworkMeasurementEntity.toDomain() = NetworkMetrics(
    connectionType = runCatching { ConnectionType.valueOf(connectionType) }.getOrDefault(ConnectionType.UNKNOWN),
    nrAttachment = runCatching { NrAttachmentState.valueOf(nrAttachment) }.getOrDefault(NrAttachmentState.UNKNOWN),
    downloadMbps = downloadMbps,
    uploadMbps = uploadMbps,
    pingMs = pingMs,
    jitterMs = jitterMs,
    packetLossPercent = packetLossPercent,
    signalStrengthDbm = signalStrengthDbm,
    signalLevel = signalLevel,
    carrierName = carrierName,
    timestampMillis = timestampMillis
)
