package com.rootssecure.nriplotsentinel.api

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/mobile/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
