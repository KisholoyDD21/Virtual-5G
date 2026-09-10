package com.virtual5g.presentation.planverification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtual5g.domain.model.PlanInfo
import com.virtual5g.domain.usecase.GetPlanInfoUseCase
import com.virtual5g.domain.usecase.SubmitUserProvidedPlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlanVerificationUiState(
    val loading: Boolean = true,
    val plan: PlanInfo? = null
)

class PlanVerificationViewModel(
    private val getPlanInfo: GetPlanInfoUseCase,
    private val submitUserProvidedPlan: SubmitUserProvidedPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanVerificationUiState())
    val uiState: StateFlow<PlanVerificationUiState> = _uiState.asStateFlow()

    init {
        refreshFromCarrier()
    }

    fun refreshFromCarrier() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val plan = runCatching { getPlanInfo() }.getOrNull()
            _uiState.value = PlanVerificationUiState(loading = false, plan = plan)
        }
    }

    fun submitManualPlan(carrierName: String, planName: String, fiveGEligible: Boolean, expiry: String?) {
        val plan = submitUserProvidedPlan(carrierName, planName, fiveGEligible, expiry)
        _uiState.value = PlanVerificationUiState(loading = false, plan = plan)
    }
}
