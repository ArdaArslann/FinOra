package com.finora.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.network.DashboardApi
import com.finora.app.data.network.DashboardSummaryResponse
import com.finora.app.data.network.FinancialInsightResponse
import com.finora.app.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardApi: DashboardApi
) : ViewModel() {

    private val _summaryState = MutableStateFlow<Resource<DashboardSummaryResponse>>(Resource.Loading())
    val summaryState: StateFlow<Resource<DashboardSummaryResponse>> = _summaryState.asStateFlow()

    private val _insightState = MutableStateFlow<Resource<FinancialInsightResponse>>(Resource.Loading())
    val insightState: StateFlow<Resource<FinancialInsightResponse>> = _insightState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _summaryState.value = Resource.Loading()
            try {
                val response = dashboardApi.getSummary()
                if (response.isSuccessful && response.body() != null) {
                    _summaryState.value = Resource.Success(response.body()!!)
                } else {
                    _summaryState.value = Resource.Error(response.message() ?: "An error occurred")
                }
            } catch (e: Exception) {
                _summaryState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }

        viewModelScope.launch {
            _insightState.value = Resource.Loading()
            try {
                val response = dashboardApi.getAiInsights()
                if (response.isSuccessful && response.body() != null) {
                    _insightState.value = Resource.Success(response.body()!!)
                } else {
                    _insightState.value = Resource.Error(response.message() ?: "An error occurred")
                }
            } catch (e: Exception) {
                _insightState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }
}
