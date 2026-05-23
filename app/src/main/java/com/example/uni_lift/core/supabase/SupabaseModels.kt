package com.example.uni_lift.core.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRow(
    val id: String = "",
    val email: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val role: String = "rider",
    val university: String = "CIT-U",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val status: String = "ACTIVE",
    @SerialName("account_status") val accountStatus: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false,
    val rating: Double? = null,
    @SerialName("rides_completed") val ridesCompleted: Int? = null,
    @SerialName("driver_status") val driverStatus: String = "OFFLINE",
    val vehicle: String? = null,
    @SerialName("vehicle_type") val vehicleType: String? = null,
    @SerialName("driver_verification_status") val driverVerificationStatus: String? = null
)

@Serializable
data class RideRow(
    val id: String = "",
    @SerialName("driver_id") val driverId: String? = null,
    @SerialName("rider_id") val riderId: String = "",
    val pickup: String = "",
    val dropoff: String = "",
    @SerialName("pickup_lat") val pickupLat: Double? = null,
    @SerialName("pickup_lng") val pickupLng: Double? = null,
    @SerialName("dropoff_lat") val dropoffLat: Double? = null,
    @SerialName("dropoff_lng") val dropoffLng: Double? = null,
    @SerialName("driver_lat") val driverLat: Double? = null,
    @SerialName("driver_lng") val driverLng: Double? = null,
    val fare: Double? = null,
    val status: String = "SEARCHING",
    @SerialName("ride_type") val rideType: String? = null,
    @SerialName("rider_name") val riderName: String? = null,
    @SerialName("rider_rating") val riderRating: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class RideInsert(
    @SerialName("rider_id") val riderId: String,
    val pickup: String,
    val dropoff: String,
    @SerialName("pickup_lat") val pickupLat: Double? = null,
    @SerialName("pickup_lng") val pickupLng: Double? = null,
    @SerialName("dropoff_lat") val dropoffLat: Double? = null,
    @SerialName("dropoff_lng") val dropoffLng: Double? = null,
    val fare: Double,
    @SerialName("ride_type") val rideType: String,
    @SerialName("rider_name") val riderName: String? = null,
    val status: String = "SEARCHING"
)

@Serializable
data class RideStatusUpdate(
    val status: String,
    @SerialName("driver_id") val driverId: String? = null
)

@Serializable
data class DriverLocationUpdate(
    @SerialName("driver_lat") val driverLat: Double,
    @SerialName("driver_lng") val driverLng: Double
)

@Serializable
data class RideRatingUpdate(
    @SerialName("rider_rating") val riderRating: Double
)

@Serializable
data class ProfileUpdateRow(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val university: String? = null,
    val vehicle: String? = null,
    @SerialName("vehicle_type") val vehicleType: String? = null
)

@Serializable
data class ConversationRow(
    val id: String = "",
    @SerialName("rider_id") val riderId: String = "",
    @SerialName("driver_id") val driverId: String = "",
    val status: String = "REQUESTED",
    val pickup: String? = null,
    val dropoff: String? = null,
    @SerialName("agreed_fare") val agreedFare: Double? = null,
    @SerialName("ride_id") val rideId: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null
)

@Serializable
data class MessageRow(
    val id: String = "",
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("ride_id") val rideId: String? = null,
    @SerialName("sender_id") val senderId: String = "",
    val content: String = "",
    val type: String = "text",
    @SerialName("offer_amount") val offerAmount: Double? = null,
    @SerialName("offer_status") val offerStatus: String? = null,
    @SerialName("sent_at") val sentAt: String? = null
)

@Serializable
data class MessageInsert(
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("ride_id") val rideId: String? = null,
    @SerialName("sender_id") val senderId: String,
    val content: String,
    val type: String = "text",
    @SerialName("offer_amount") val offerAmount: Double? = null,
    @SerialName("offer_status") val offerStatus: String? = null
)

@Serializable
data class ConversationInsert(
    @SerialName("rider_id") val riderId: String,
    @SerialName("driver_id") val driverId: String,
    val status: String = "ACTIVE",
    val pickup: String? = null,
    val dropoff: String? = null
)

@Serializable
data class LastMessageAtUpdate(
    @SerialName("last_message_at") val lastMessageAt: String
)

@Serializable
data class AccountStatusUpdate(
    @SerialName("account_status") val accountStatus: String
)

@Serializable
data class VerificationRow(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("vehicle_type") val vehicleType: String? = null,
    @SerialName("plate_number") val plateNumber: String? = null,
    val status: String = "PENDING",
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("submitted_at") val submittedAt: String? = null
)

@Serializable
data class VerificationStatusUpdate(
    val status: String
)

@Serializable
data class VerificationStatusWithReason(
    val status: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null
)
