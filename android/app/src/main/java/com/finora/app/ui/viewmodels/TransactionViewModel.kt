package com.finora.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.network.CreateTransactionRequest
import com.finora.app.data.network.TransactionApi
import com.finora.app.data.network.TransactionDto
import com.finora.app.data.network.getErrorMessage
import com.finora.app.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionApi: TransactionApi
) : ViewModel() {

    private val _transactions = MutableStateFlow<Resource<List<TransactionDto>>>(Resource.Loading())
    val transactions: StateFlow<Resource<List<TransactionDto>>> = _transactions.asStateFlow()

    init {
        fetchTransactions()
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            _transactions.value = Resource.Loading()
            try {
                val response = transactionApi.getTransactions()
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    _transactions.value = Resource.Success(response.body()!!.data!!)
                } else {
                    _transactions.value = Resource.Error(response.getErrorMessage() ?: "Failed to fetch transactions")
                }
            } catch (e: Exception) {
                _transactions.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                val response = transactionApi.deleteTransaction(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update state locally or re-fetch
                    val currentList = _transactions.value.data?.filter { it.id != id }
                    _transactions.value = Resource.Success(currentList ?: emptyList())
                }
            } catch (e: Exception) {
                // Handle error quietly or show a snackbar
            }
        }
    }
    
    fun createTransaction(request: CreateTransactionRequest) {
        viewModelScope.launch {
            try {
                val response = transactionApi.createTransaction(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchTransactions() // Refresh list
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun updateTransaction(id: String, request: CreateTransactionRequest) {
        viewModelScope.launch {
            try {
                val response = transactionApi.updateTransaction(id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchTransactions() // Refresh list
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }
}
