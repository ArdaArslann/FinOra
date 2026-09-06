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
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsApi: StatisticsApi
) : ViewModel() {

    private val _statisticsState = MutableStateFlow<Resource<StatisticsResponse>>(Resource.Loading())
    val statisticsState: StateFlow<Resource<StatisticsResponse>> = _statisticsState.asStateFlow()

    init {
        fetchStatistics()
    }

    fun fetchStatistics() {
        viewModelScope.launch {
            _statisticsState.value = Resource.Loading()
            try {
                val startDate = java.time.LocalDate.now().withDayOfMonth(1).toString()
                val endDate = java.time.LocalDate.now().withDayOfMonth(java.time.LocalDate.now().lengthOfMonth()).toString()
                val response = statisticsApi.getStatistics(startDate, endDate)
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
