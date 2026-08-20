package com.finora.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.network.BudgetApi
import com.finora.app.data.network.BudgetDto
import com.finora.app.data.network.CreateBudgetRequest
import com.finora.app.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetApi: BudgetApi
) : ViewModel() {

    private val _budgets = MutableStateFlow<Resource<List<BudgetDto>>>(Resource.Loading())
    val budgets: StateFlow<Resource<List<BudgetDto>>> = _budgets.asStateFlow()

    init {
        fetchBudgets()
    }

    fun fetchBudgets() {
        viewModelScope.launch {
            _budgets.value = Resource.Loading()
            try {
                val response = budgetApi.getBudgets()
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    _budgets.value = Resource.Success(response.body()!!.data!!)
                } else {
                    _budgets.value = Resource.Error(response.body()?.error?.message ?: "Failed to load budgets")
                }
            } catch (e: Exception) {
                _budgets.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }

    fun createBudget(request: CreateBudgetRequest) {
        viewModelScope.launch {
            _createState.value = Resource.Loading()
            try {
                val response = budgetApi.createBudget(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _createState.value = Resource.Success(Unit)
                    fetchBudgets() // refresh list
                } else {
                    _createState.value = Resource.Error(response.body()?.error?.message ?: "Failed to create budget")
                }
            } catch (e: Exception) {
                _createState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }
}
