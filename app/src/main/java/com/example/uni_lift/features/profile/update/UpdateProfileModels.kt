package com.example.uni_lift.features.profile.update

import com.example.uni_lift.core.models.User
import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("student_id") val studentId: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("university") val university: String?
)

data class UpdateProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: User?
)
