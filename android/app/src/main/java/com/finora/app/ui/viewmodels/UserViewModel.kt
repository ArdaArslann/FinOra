package com.finora.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.local.TokenManager
import com.finora.app.data.network.AuthApi
import com.finora.app.data.network.UserApi
import com.finora.app.data.network.UserDto
import com.finora.app.data.network.getErrorMessage
import com.finora.app.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userApi: UserApi,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<UserDto>>(Resource.Loading())
    val userState: StateFlow<Resource<UserDto>> = _userState.asStateFlow()

    private val _logoutState = MutableStateFlow<Resource<Unit?>>(Resource.Success(null))
    val logoutState: StateFlow<Resource<Unit?>> = _logoutState.asStateFlow()

    init {
        fetchCurrentUser()
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            _userState.value = Resource.Loading()
            try {
                val response = userApi.getCurrentUser()
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    _userState.value = Resource.Success(response.body()!!.data!!)
                } else {
                    _userState.value = Resource.Error(response.getErrorMessage() ?: "Failed to fetch profile")
                }
            } catch (e: Exception) {
                _userState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = Resource.Loading()
            try {
                authApi.logout()
            } catch (e: Exception) {
                // Ignore API error on logout, we still want to clear local state
            }
            tokenManager.clearTokens()
            _logoutState.value = Resource.Success(Unit)
        }
    }
}
