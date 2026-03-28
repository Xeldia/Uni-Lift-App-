package com.example.uni_lift.models

import com.google.gson.annotations.SerializedName

// Profile Update Models
data class UpdateProfileRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("studentId") val studentId: String?,
    @SerializedName("contactNumber") val contactNumber: String?,
    @SerializedName("campusLocation") val campusLocation: String?
)

data class UpdateProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: User?
)

// Password Change Models
data class ChangePasswordRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ChangePasswordResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)
