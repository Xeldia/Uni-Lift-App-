package com.example.uni_lift.models

import com.google.gson.annotations.SerializedName

// Login Models
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: AuthData?,
    @SerializedName("error") val error: ApiError?,
    @SerializedName("timestamp") val timestamp: String
)

data class AuthData(
    @SerializedName("user") val user: User,
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String?
)

// Register Models
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: User?,
    @SerializedName("error") val error: ApiError?,
    @SerializedName("timestamp") val timestamp: String
)

// Common Models
data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("currentRole") val currentRole: String? = null,
    @SerializedName("isVerified") val isVerified: Boolean? = null,
    @SerializedName("emergencyContact") val emergencyContact: EmergencyContact? = null
)

data class EmergencyContact(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
)

data class ApiError(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: String?
)
