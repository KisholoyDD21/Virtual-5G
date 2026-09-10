package com.virtual5g.domain.repository

import com.virtual5g.domain.model.DeviceCapability
import com.virtual5g.domain.model.NetworkMetrics
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDeviceCapability(): DeviceCapability
    /** True once READ_PHONE_STATE (or the API 33+ granular equivalent) is granted. */
    fun hasTelephonyPermission(): Boolean
}

interface NetworkRepository {
    /** Live stream of network metrics; a new value is emitted on every connectivity/telephony change. */
    fun observeNetworkMetrics(): Flow<NetworkMetrics>

    /** The most recent metrics snapshot, persisted so Offline Mode has something to show. */
    suspend fun getLastKnownMetrics(): NetworkMetrics?

    suspend fun saveLastKnownMetrics(metrics: NetworkMetrics)
}
