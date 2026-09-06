package com.finora.app.data.network

import com.finora.app.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiProvider: Provider<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val currentToken = runBlocking { tokenManager.accessToken.first() }
        val refreshToken = runBlocking { tokenManager.refreshToken.first() }

        if (currentToken == null || refreshToken == null) {
            return null
        }

        // Avoid infinite loop if refresh token itself fails
        if (response.request.url.encodedPath.contains("auth/refresh")) {
            return null
        }

        val authApi = authApiProvider.get()
        val refreshResponse = try {
            authApi.refreshTokenSync(RefreshRequest(refreshToken)).execute()
        } catch (e: Exception) {
            null
        }

        if (refreshResponse != null && refreshResponse.isSuccessful && refreshResponse.body()?.success == true && refreshResponse.body()?.data != null) {
            val newTokens = refreshResponse.body()!!.data!!
            runBlocking {
                tokenManager.saveTokens(newTokens.accessToken, refreshToken)
            }
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
        } else {
            // Refresh failed, clear tokens so user is logged out
            runBlocking {
                tokenManager.clearTokens()
            }
            return null
        }
    }
}
