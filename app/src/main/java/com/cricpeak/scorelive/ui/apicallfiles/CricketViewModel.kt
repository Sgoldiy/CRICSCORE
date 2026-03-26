package com.cricpeak.scorelive.ui.apicallfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class FixturesState {
    object Loading : FixturesState()
    data class Success(val response: FixturesResponse) : FixturesState()
    data class Error(val message: String) : FixturesState()
}

class CricketViewModel : ViewModel() {
    private val apiService = CricketApiService.create()

    private val _fixturesState = MutableStateFlow<FixturesState>(FixturesState.Loading)
    val fixturesState: StateFlow<FixturesState> = _fixturesState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private var refreshJob: Job? = null

    init {
        fetchFixtures()
        startAutoRefresh()
    }

    fun fetchFixtures(isManual: Boolean = false) {
        viewModelScope.launch {
            if (isManual) {
                _isRefreshing.value = true
            } else if (_fixturesState.value !is FixturesState.Success) {
                _fixturesState.value = FixturesState.Loading
            }
            
            try {
                val response = apiService.getFixtures()
                _fixturesState.value = FixturesState.Success(response)
            } catch (e: Exception) {
                if (_fixturesState.value !is FixturesState.Success) {
                    _fixturesState.value = FixturesState.Error(e.localizedMessage ?: "Unknown Error")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(10000) // 10 seconds refresh rule
                fetchFixtures()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
