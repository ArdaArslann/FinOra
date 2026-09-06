package com.finora.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.network.StatisticsApi
import com.finora.app.data.network.StatisticsResponse
import com.finora.app.data.network.getErrorMessage
import com.finora.app.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class StatsPeriod { WEEKLY, MONTHLY, YEARLY, ALL_TIME }

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsApi: StatisticsApi
) : ViewModel() {

    private val _statisticsState = MutableStateFlow<Resource<StatisticsResponse>>(Resource.Loading())
    val statisticsState: StateFlow<Resource<StatisticsResponse>> = _statisticsState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.MONTHLY)
    val selectedPeriod: StateFlow<StatsPeriod> = _selectedPeriod.asStateFlow()

    init {
        fetchStatistics(StatsPeriod.MONTHLY)
    }

    fun selectPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
        fetchStatistics(period)
    }

    fun fetchStatistics(period: StatsPeriod = _selectedPeriod.value) {
        viewModelScope.launch {
            // Keep existing data visible during reload
            val existing = _statisticsState.value.data
            _statisticsState.value = Resource.Loading(existing)
            try {
                val today = LocalDate.now()
                val (startDate, endDate) = when (period) {
                    StatsPeriod.WEEKLY -> {
                        val start = today.with(DayOfWeek.MONDAY)
                        val end = today.with(DayOfWeek.SUNDAY)
                        start to end
                    }
                    StatsPeriod.MONTHLY -> {
                        val start = today.withDayOfMonth(1)
                        val end = today.with(TemporalAdjusters.lastDayOfMonth())
                        start to end
                    }
                    StatsPeriod.YEARLY -> {
                        val start = today.withDayOfYear(1)
                        val end = today.with(TemporalAdjusters.lastDayOfYear())
                        start to end
                    }
                    StatsPeriod.ALL_TIME -> {
                        val start = LocalDate.of(2000, 1, 1)
                        val end = LocalDate.of(2100, 1, 1)
                        start to end
                    }
                }
                val response = statisticsApi.getStatistics(startDate.toString(), endDate.toString())
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    _statisticsState.value = Resource.Success(response.body()!!.data!!)
                } else {
                    _statisticsState.value = Resource.Error(response.getErrorMessage() ?: "Failed to fetch statistics")
                }
            } catch (e: Exception) {
                _statisticsState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }
}
