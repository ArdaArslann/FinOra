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
            _insightState.value = Resource.Loading()
            
            try {
                val summaryResponse = dashboardApi.getSummary()
                if (summaryResponse.isSuccessful && summaryResponse.body()?.success == true && summaryResponse.body()?.data != null) {
                    _summaryState.value = Resource.Success(summaryResponse.body()!!.data!!)
                } else {
                    _summaryState.value = Resource.Error(summaryResponse.body()?.error?.message ?: "Failed to load summary")
                }

                val insightResponse = dashboardApi.getAiInsights()
                if (insightResponse.isSuccessful && insightResponse.body()?.success == true && insightResponse.body()?.data != null) {
                    _insightState.value = Resource.Success(insightResponse.body()!!.data!!)
                } else {
                    _insightState.value = Resource.Error(insightResponse.body()?.error?.message ?: "Failed to load insights")
                }
            } catch (e: Exception) {
                _summaryState.value = Resource.Error(e.localizedMessage ?: "Connection error")
                _insightState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }
}
