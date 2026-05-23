package com.example.uni_lift.features.auth.register

import com.example.uni_lift.features.auth.login.AuthData
import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("student_id") val studentId: String
)

data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: AuthData?,
    @SerializedName("error") val error: String?,
    @SerializedName("timestamp") val timestamp: String? = null
)
