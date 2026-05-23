package com.example.uni_lift.features.rides.dashboard

import com.example.uni_lift.core.models.RideRecord
import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: DashboardData?,
    @SerializedName("error") val error: String?
)

data class DashboardData(
    @SerializedName("welcomeMessage") val welcomeMessage: String,
    @SerializedName("activeRidesCount") val activeRidesCount: Int,
    @SerializedName("totalEarnings") val totalEarnings: Double?,
    @SerializedName("notifications") val notifications: List<String>
)

data class BookingForm(
    val pickup: String = "",
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val dropoff: String = "",
    val dropoffLat: Double? = null,
    val dropoffLng: Double? = null,
    val passengerCount: Int = 1,
    val fare: String = "",
    val vehicleType: String = "MOTO"
)

data class PickupSelection(
    val label: String,
    val lat: Double,
    val lng: Double
)

data class DestinationSelection(
    val label: String,
    val lat: Double,
    val lng: Double
)
