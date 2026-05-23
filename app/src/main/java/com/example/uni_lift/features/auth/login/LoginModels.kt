package com.example.uni_lift.features.auth.login

import com.example.uni_lift.core.models.User
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: AuthData?,
    @SerializedName("error") val error: String?,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class AuthData(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("user") val user: User
)
