package com.rootssecure.nriplotsentinel.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("user_name") val userName: String? = null
)
