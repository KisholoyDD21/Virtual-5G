package com.virtual5g.domain.usecase

import com.virtual5g.domain.engine.NetworkQualityEngine
import com.virtual5g.domain.engine.OperatingModeSelector
import com.virtual5g.domain.model.ConnectionType
import com.virtual5g.domain.model.NetworkMetrics
import com.virtual5g.domain.model.Virtual5GState
import com.virtual5g.domain.repository.CarrierProvider
import com.virtual5g.domain.repository.DeviceRepository
import com.virtual5g.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold

/**
 * The "Virtual5GEngine" from the spec: continuously combines device
 * capability + live network metrics + plan eligibility into a single
 * [Virtual5GState], keeping a short rolling window of samples so
 * NetworkQualityEngine can score stability.
 */
class GetVirtual5GStateUseCase(
    private val deviceRepository: DeviceRepository,
    private val networkRepository: NetworkRepository,
    private val carrierProvider: CarrierProvider,
    private val qualityEngine: NetworkQualityEngine = NetworkQualityEngine()
) {
    operator fun invoke(): Flow<Virtual5GState> {
        val device = deviceRepository.getDeviceCapability()

        return networkRepository.observeNetworkMetrics()
            .runningFold(emptyList<NetworkMetrics>()) { history, latest ->
                (history + latest).takeLast(SAMPLE_WINDOW)
            }
            .filter { it.isNotEmpty() }
            .map { history ->
                val latest = history.last()
                val recent = history.dropLast(1)
                val plan = runCatching { carrierProvider.getPlanInfo() }.getOrNull()

                val mode = OperatingModeSelector.select(device, latest, plan)
                val score = qualityEngine.score(latest, recent)
                val explanation = OperatingModeSelector.explanationFor(mode, device, plan)

                Virtual5GState(
                    mode = mode,
                    device = device,
                    network = latest,
                    plan = plan,
                    score = score,
                    explanation = explanation,
                    isOffline = latest.connectionType == ConnectionType.NONE,
                    lastUpdatedMillis = latest.timestampMillis
                )
            }
            .distinctUntilChanged()
    }

    private companion object {
        const val SAMPLE_WINDOW = 10
    }
}
