package com.finora.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.local.TokenManager
import com.finora.app.data.network.AuthApi
import com.finora.app.data.network.LoginRequest
import com.finora.app.data.network.RegisterRequest
import com.finora.app.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit)) // Idle state is represented as Unit for now
    val loginState: StateFlow<Resource<Unit>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val registerState: StateFlow<Resource<Unit>> = _registerState.asStateFlow()

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            try {
                val response = authApi.login(request)
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    val data = response.body()!!.data!!
                    tokenManager.saveTokens(data.token, data.refreshToken)
                    _loginState.value = Resource.Success(Unit)
                } else {
                    _loginState.value = Resource.Error(response.body()?.error?.message ?: response.message() ?: "Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }
    
    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            try {
                val response = authApi.register(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _registerState.value = Resource.Success(Unit)
                } else {
                    _registerState.value = Resource.Error(response.body()?.error?.message ?: response.message() ?: "Registration failed")
                }
            } catch (e: Exception) {
                _registerState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }
}
