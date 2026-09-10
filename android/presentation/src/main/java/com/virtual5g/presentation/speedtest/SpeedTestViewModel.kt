package com.virtual5g.presentation.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtual5g.domain.model.SpeedTestResult
import com.virtual5g.domain.model.SpeedTestStage
import com.virtual5g.domain.model.SpeedTestTier
import com.virtual5g.domain.usecase.RunSpeedTestUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SpeedTestUiState {
    data object Idle : SpeedTestUiState
    data class Running(val stage: SpeedTestStage) : SpeedTestUiState
    data class Done(val result: SpeedTestResult) : SpeedTestUiState
}

class SpeedTestViewModel(private val runSpeedTest: RunSpeedTestUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<SpeedTestUiState>(SpeedTestUiState.Idle)
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()

    fun start(tier: SpeedTestTier) {
        viewModelScope.launch {
            runSpeedTest(tier).collect { progress ->
                _uiState.value = progress.partialResult?.let { SpeedTestUiState.Done(it) }
                    ?: SpeedTestUiState.Running(progress.stage)
            }
        }
    }

    fun reset() {
        _uiState.value = SpeedTestUiState.Idle
    }
}
