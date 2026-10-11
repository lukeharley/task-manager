package com.minimaltask.viewmodel

import android.os.SystemClock
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
        val clamped = minutes.coerceIn(MIN_CUSTOM_MINUTES, MAX_CUSTOM_MINUTES)
        val state = _uiState.value
        _uiState.value = state.copy(
            customMinutes = clamped,
            // Non si altera una sessione già in corso: il nuovo valore vale dalla prossima.
            remainingSeconds = if (state.selectedMode == FocusMode.CUSTOM && !state.running) clamped * 60 else state.remainingSeconds
        )
        viewModelScope.launch { preferences.setFocusPreferences(clamped, 5) }
    }

    fun start() {
        if (_uiState.value.running) return
        // Una sessione appena terminata riparte da capo invece di registrarne subito un'altra.
        if (_uiState.value.remainingSeconds <= 0) reset()
        val remainingMillis = _uiState.value.remainingSeconds * 1_000L
        // Il tempo residuo si calcola dall'orologio di sistema: delay() da solo accumula ritardi.
        val endAt = SystemClock.elapsedRealtime() + remainingMillis
        _uiState.value = _uiState.value.copy(running = true)
        focusAlarmScheduler.scheduleFinish(remainingMillis)
        timerJob = viewModelScope.launch {
            while (true) {
                val left = ((endAt - SystemClock.elapsedRealtime() + 999) / 1_000).toInt().coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(remainingSeconds = left)
                if (left == 0) break
                delay(TICK_MILLIS)
            }
            finish()
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

    private fun finish() {
        val totalMinutes = if (_uiState.value.selectedMode == FocusMode.CUSTOM) {
            _uiState.value.customMinutes
        } else {
            _uiState.value.selectedMode.minutes
        }
        viewModelScope.launch { repository.saveSession(totalMinutes, completed = true) }
        _uiState.value = _uiState.value.copy(running = false, remainingSeconds = 0)
    }

    companion object {
        const val MIN_CUSTOM_MINUTES = 5
        const val MAX_CUSTOM_MINUTES = 180
        private const val TICK_MILLIS = 250L
    }
}
