package com.virtual5g.data.repository

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.virtual5g.core.logging.SecureLogger
import com.virtual5g.data.local.NetworkMeasurementDb
import com.virtual5g.data.local.SecureFlagStore
import com.virtual5g.data.telephony.CellularTelephonyObserver
import com.virtual5g.data.telephony.ConnectivityObserver
import com.virtual5g.data.telephony.DeviceCapabilityManager
import com.virtual5g.data.telephony.LightweightLatencyProbe
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Refreshes the persisted "last known measurement" snapshot roughly every
 * 15 minutes even while the app isn't in the foreground, so Offline Mode
 * has a reasonably fresh number to show (spec section 17). Deliberately not
 * more frequent than this - background telephony polling has a real battery
 * cost, and the live dashboard already gets fresh data from NetworkMonitor
 * while the app is open.
 */
class NetworkSamplingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val db = NetworkMeasurementDb.getInstance(applicationContext)
        val deviceCapabilityManager = DeviceCapabilityManager(applicationContext, SecureFlagStore(applicationContext))
        val repository = NetworkRepositoryImpl(
            connectivityObserver = ConnectivityObserver(applicationContext),
            cellularTelephonyObserver = CellularTelephonyObserver(applicationContext),
            latencyProbe = LightweightLatencyProbe(),
            deviceCapabilityManager = deviceCapabilityManager,
            measurementDao = db.networkMeasurementDao()
        )
        val sample = repository.observeNetworkMetrics().first()
        repository.saveLastKnownMetrics(sample)
        Result.success()
    } catch (error: Exception) {
        SecureLogger.w("NetworkSamplingWorker", "background sample failed", error)
        Result.retry()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "virtual5g_network_sampling"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<NetworkSamplingWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
