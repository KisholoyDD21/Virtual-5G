package com.virtual5g.app.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.virtual5g.core.util.PermissionUtils
import com.virtual5g.data.carrier.MockCarrierProvider
import com.virtual5g.data.local.NetworkMeasurementDb
import com.virtual5g.data.local.SecureFlagStore
import com.virtual5g.data.repository.NetworkRepositoryImpl
import com.virtual5g.data.speedtest.SpeedTestEngineImpl
import com.virtual5g.data.telephony.CellularTelephonyObserver
import com.virtual5g.data.telephony.ConnectivityObserver
import com.virtual5g.data.telephony.DeviceCapabilityManager
import com.virtual5g.data.telephony.LightweightLatencyProbe
import com.virtual5g.domain.repository.CarrierProvider
import com.virtual5g.domain.repository.DeviceRepository
import com.virtual5g.domain.repository.NetworkRepository
import com.virtual5g.domain.repository.SpeedTestRepository
import com.virtual5g.domain.usecase.GetPlanInfoUseCase
import com.virtual5g.domain.usecase.GetVirtual5GStateUseCase
import com.virtual5g.domain.usecase.RunSpeedTestUseCase
import com.virtual5g.domain.usecase.SubmitUserProvidedPlanUseCase
import com.virtual5g.presentation.dashboard.DashboardViewModel
import com.virtual5g.presentation.navigation.NavGraphDependencies
import com.virtual5g.presentation.planverification.PlanVerificationViewModel
import com.virtual5g.presentation.speedtest.SpeedTestViewModel

/**
 * Everything below is constructed once, here, in dependency order. This is
 * the project's composition root: the one place that is allowed to know
 * about concrete :data implementations, so :presentation only ever sees
 * :domain interfaces (see NavGraphDependencies). Swapping this for
 * Hilt/Koin later only touches this file.
 */
class AppContainer(context: Context) : NavGraphDependencies {

    private val appContext = context.applicationContext

    private val secureFlagStore = SecureFlagStore(appContext)
    private val database = NetworkMeasurementDb.getInstance(appContext)

    private val deviceCapabilityManager = DeviceCapabilityManager(appContext, secureFlagStore)
    private val deviceRepository: DeviceRepository = deviceCapabilityManager

    private val networkRepository: NetworkRepository = NetworkRepositoryImpl(
        connectivityObserver = ConnectivityObserver(appContext),
        cellularTelephonyObserver = CellularTelephonyObserver(appContext),
        latencyProbe = LightweightLatencyProbe(),
        deviceCapabilityManager = deviceCapabilityManager,
        measurementDao = database.networkMeasurementDao()
    )

    private val carrierProvider: CarrierProvider = MockCarrierProvider()
    private val speedTestRepository: SpeedTestRepository = SpeedTestEngineImpl()

    private val getVirtual5GState = GetVirtual5GStateUseCase(deviceRepository, networkRepository, carrierProvider)
    private val runSpeedTest = RunSpeedTestUseCase(speedTestRepository)
    private val getPlanInfo = GetPlanInfoUseCase(carrierProvider)
    private val submitUserProvidedPlan = SubmitUserProvidedPlanUseCase()

    override val dashboardViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { DashboardViewModel(getVirtual5GState) }
    }

    override val speedTestViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { SpeedTestViewModel(runSpeedTest) }
    }

    override val planVerificationViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { PlanVerificationViewModel(getPlanInfo, submitUserProvidedPlan) }
    }

    override val hasTelephonyPermission: () -> Boolean = { PermissionUtils.hasBasicOrFullPhoneStatePermission(appContext) }

    // Actual permission request dialog is triggered from MainActivity (needs an Activity,
    // not just a Context) - this is overridden per-instance from there. See MainActivity.kt.
    override var onRequestTelephonyPermission: () -> Unit = {}
}
