package com.rootssecure.nriplotsentinel.repository

import com.rootssecure.nriplotsentinel.api.AuthApiService
import com.rootssecure.nriplotsentinel.api.LoginRequest
import com.rootssecure.nriplotsentinel.api.LoginResponse
import com.rootssecure.nriplotsentinel.storage.TokenDataStore

class AuthRepository(
    private val authApiService: AuthApiService,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return runCatching {
            authApiService.login(LoginRequest(email = email, password = password)).also { response ->
                tokenDataStore.saveToken(response.token, response.refreshToken)
            }
        }
    }
}
