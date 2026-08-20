package com.finora.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.network.CategoryApi
import com.finora.app.data.network.CategoryDto
import com.finora.app.data.network.CreateCategoryRequest
import com.finora.app.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryApi: CategoryApi
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<CategoryDto>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<CategoryDto>>> = _categories.asStateFlow()

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _categories.value = Resource.Loading()
            try {
                val response = categoryApi.getCategories()
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    _categories.value = Resource.Success(response.body()!!.data!!)
                } else {
                    _categories.value = Resource.Error(response.body()?.error?.message ?: "Failed to fetch categories")
                }
            } catch (e: Exception) {
                _categories.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }

    fun createCategory(request: CreateCategoryRequest) {
        viewModelScope.launch {
            try {
                val response = categoryApi.createCategory(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchCategories()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
