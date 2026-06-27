package com.minimaltask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimaltask.data.preferences.AppPreferences
import com.minimaltask.data.repository.FocusRepository
import com.minimaltask.notification.FocusAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FocusMode(val label: String, val minutes: Int, val premium: Boolean) {
    BASIC("Base", 15, false),
    POMODORO("Pomodoro 25/5", 25, true),
    DEEP_WORK("Deep Work", 90, true),
    CUSTOM("Personalizzata", 30, true)
}

data class FocusUiState(
    val selectedMode: FocusMode = FocusMode.BASIC,
    val customMinutes: Int = 30,
    val remainingSeconds: Int = 15 * 60,
    val running: Boolean = false
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val repository: FocusRepository,
    private val preferences: AppPreferences,
    private val focusAlarmScheduler: FocusAlarmScheduler
) : ViewModel() {
    private var timerJob: Job? = null
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState
    val totalFocusMinutes: StateFlow<Int> = repository.observeTotalCompletedMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun selectMode(mode: FocusMode, premiumActive: Boolean) {
        if (mode.premium && !premiumActive) return
        val minutes = if (mode == FocusMode.CUSTOM) _uiState.value.customMinutes else mode.minutes
        _uiState.value = _uiState.value.copy(
            selectedMode = mode,
            remainingSeconds = minutes * 60,
            running = false
        )
        timerJob?.cancel()
        focusAlarmScheduler.cancel()
    }

    fun setCustomMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(5, 180)
        _uiState.value = _uiState.value.copy(
            customMinutes = clamped,
            remainingSeconds = if (_uiState.value.selectedMode == FocusMode.CUSTOM) clamped * 60 else _uiState.value.remainingSeconds
        )
        viewModelScope.launch { preferences.setFocusPreferences(clamped, 5) }
    }

    fun start() {
        if (_uiState.value.running) return
        _uiState.value = _uiState.value.copy(running = true)
        focusAlarmScheduler.scheduleFinish(_uiState.value.remainingSeconds * 1_000L)
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.running) {
                delay(1_000)
                _uiState.value = _uiState.value.copy(remainingSeconds = _uiState.value.remainingSeconds - 1)
            }
            if (_uiState.value.remainingSeconds <= 0) finish(completed = true)
        }
    }

    fun pause() {
        timerJob?.cancel()
        focusAlarmScheduler.cancel()
        _uiState.value = _uiState.value.copy(running = false)
    }

    fun reset() {
        pause()
        val minutes = if (_uiState.value.selectedMode == FocusMode.CUSTOM) {
            _uiState.value.customMinutes
        } else {
            _uiState.value.selectedMode.minutes
        }
        _uiState.value = _uiState.value.copy(remainingSeconds = minutes * 60)
    }

    private fun finish(completed: Boolean) {
        timerJob?.cancel()
        val totalMinutes = if (_uiState.value.selectedMode == FocusMode.CUSTOM) {
            _uiState.value.customMinutes
        } else {
            _uiState.value.selectedMode.minutes
        }
        viewModelScope.launch { repository.saveSession(totalMinutes, completed) }
        _uiState.value = _uiState.value.copy(running = false, remainingSeconds = 0)
    }
}
