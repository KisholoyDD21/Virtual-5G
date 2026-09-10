package com.virtual5g.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtual5g.domain.engine.OptimizationEngine
import com.virtual5g.domain.engine.OptimizationRecommendation
import com.virtual5g.domain.model.Virtual5GState
import com.virtual5g.domain.usecase.GetVirtual5GStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Ready(
        val state: Virtual5GState,
        val recommendations: List<OptimizationRecommendation>
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(
    private val getVirtual5GState: GetVirtual5GStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                getVirtual5GState().collect { state ->
                    _uiState.value = DashboardUiState.Ready(
                        state = state,
                        recommendations = OptimizationEngine.recommendationsFor(state.network, state.mode)
                    )
                }
            }.onFailure { error ->
                _uiState.value = DashboardUiState.Error(
                    error.message ?: "Unable to read network state. Pull down to retry."
                )
            }
        }
    }
}
