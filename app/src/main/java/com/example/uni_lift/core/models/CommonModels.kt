package com.example.uni_lift.core.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("student_id") val studentId: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("role") val role: String = "RIDER",
    @SerializedName("university") val university: String? = null,
    @SerializedName("is_verified") val isVerified: Boolean = false,
    @SerializedName("account_status") val accountStatus: String = "PENDING",
    @SerializedName("driver_verification_status") val driverVerificationStatus: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("vehicle") val vehicle: String? = null,
    @SerializedName("vehicle_type") val vehicleType: String? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("rides_completed") val ridesCompleted: Int? = null,
)

data class RideRecord(
    @SerializedName("id") val id: String,
    @SerializedName("rider_id") val riderId: String,
    @SerializedName("driver_id") val driverId: String? = null,
    @SerializedName("pickup") val pickup: String,
    @SerializedName("dropoff") val dropoff: String,
    @SerializedName("pickup_lat") val pickupLat: Double? = null,
    @SerializedName("pickup_lng") val pickupLng: Double? = null,
    @SerializedName("dropoff_lat") val dropoffLat: Double? = null,
    @SerializedName("dropoff_lng") val dropoffLng: Double? = null,
    @SerializedName("driver_lat") val driverLat: Double? = null,
    @SerializedName("driver_lng") val driverLng: Double? = null,
    @SerializedName("rider_rating") val riderRating: Double? = null,
    @SerializedName("passenger_count") val passengerCount: Int = 1,
    @SerializedName("fare") val fare: Double = 0.0,
    @SerializedName("ride_type") val rideType: String = "MOTO",
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class ApiError(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: String? = null
)

data class GenericResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)
