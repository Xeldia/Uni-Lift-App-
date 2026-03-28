package com.example.uni_lift.models

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: DashboardData?,
    @SerializedName("error") val error: ApiError?,
    @SerializedName("timestamp") val timestamp: String
)

data class DashboardData(
    @SerializedName("welcomeMessage") val welcomeMessage: String,
    @SerializedName("activeRidesCount") val activeRidesCount: Int,
    @SerializedName("totalEarnings") val totalEarnings: Double?,
    @SerializedName("notifications") val notifications: List<String>
)
