package com.example.uni_lift.core.api

import com.example.uni_lift.core.models.GenericResponse
import com.example.uni_lift.core.models.RideRecord
import com.example.uni_lift.core.models.User
import com.example.uni_lift.features.auth.login.LoginRequest
import com.example.uni_lift.features.auth.login.LoginResponse
import com.example.uni_lift.features.auth.register.RegisterRequest
import com.example.uni_lift.features.auth.register.RegisterResponse
import com.example.uni_lift.features.profile.update.UpdateProfileRequest
import com.example.uni_lift.features.profile.update.UpdateProfileResponse
import com.example.uni_lift.features.settings.password.ChangePasswordRequest
import com.example.uni_lift.features.settings.password.ChangePasswordResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

// ─── Ride models ──────────────────────────────────────────────────────────────

data class CreateRideRequest(
    @SerializedName("pickup") val pickup: String,
    @SerializedName("dropoff") val dropoff: String,
    @SerializedName("passenger_count") val passengerCount: Int,
    @SerializedName("fare") val fare: Double,
    @SerializedName("ride_type") val rideType: String
)

data class UpdateStatusRequest(
    @SerializedName("status") val status: String
)

data class RideResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: RideRecord?,
    @SerializedName("error") val error: String?
)

data class RidesListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<RideRecord>?,
    @SerializedName("error") val error: String?
)

// ─── User / profile models ────────────────────────────────────────────────────

data class UserResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: User?,
    @SerializedName("error") val error: String?
)

data class UsersListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<User>?,
    @SerializedName("error") val error: String?
)

data class PhotoUploadRequest(
    @SerializedName("fileName") val fileName: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("fileDataBase64") val fileDataBase64: String
)

data class PhotoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Map<String, String>?,
    @SerializedName("error") val error: String?
)

// ─── Admin models ─────────────────────────────────────────────────────────────

data class StatusRequest(@SerializedName("status") val status: String)

data class SuspendRequest(
    @SerializedName("reason") val reason: String,
    @SerializedName("durationHours") val durationHours: Int?
)

data class VerificationsListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<User>?,
    @SerializedName("error") val error: String?
)

// ─── Change password models ───────────────────────────────────────────────────

data class ChangePasswordApiRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

// ─── Interface ────────────────────────────────────────────────────────────────

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<GenericResponse>

    // Rides
    @POST("rides")
    suspend fun createRide(
        @Header("Authorization") token: String,
        @Body request: CreateRideRequest
    ): Response<RideResponse>

    @GET("rides")
    suspend fun getRides(
        @Header("Authorization") token: String
    ): Response<RidesListResponse>

    @GET("rides/{id}")
    suspend fun getRide(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<RideResponse>

    @PATCH("rides/{id}/status")
    suspend fun updateRideStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateStatusRequest
    ): Response<RideResponse>

    // Users / profile
    @GET("users/{id}")
    suspend fun getUser(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<UserResponse>

    @PUT("users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @POST("users/{id}/photo")
    suspend fun uploadPhoto(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: PhotoUploadRequest
    ): Response<PhotoResponse>

    @DELETE("users/{id}/photo")
    suspend fun deletePhoto(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    // Admin — users
    @GET("users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): Response<UsersListResponse>

    @PATCH("users/{id}/status")
    suspend fun updateUserStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: StatusRequest
    ): Response<GenericResponse>

    @PATCH("users/{id}/suspend")
    suspend fun suspendUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: SuspendRequest
    ): Response<GenericResponse>

    @PATCH("users/{id}/reactivate")
    suspend fun reactivateUser(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericResponse>

    @PATCH("users/{id}/role")
    suspend fun setUserRole(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: StatusRequest
    ): Response<GenericResponse>

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericResponse>

    // Admin — verifications
    @GET("users/verification/account/pending")
    suspend fun getPendingAccountVerifications(
        @Header("Authorization") token: String
    ): Response<VerificationsListResponse>

    @GET("users/verification/driver/pending")
    suspend fun getPendingDriverVerifications(
        @Header("Authorization") token: String
    ): Response<VerificationsListResponse>

    @PATCH("users/{id}/verification/account/approve")
    suspend fun approveAccountVerification(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericResponse>

    @PATCH("users/{id}/verification/account/reject")
    suspend fun rejectAccountVerification(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<GenericResponse>

    @PATCH("users/{id}/verification/driver/approve")
    suspend fun approveDriverVerification(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericResponse>

    @PATCH("users/{id}/verification/driver/reject")
    suspend fun rejectDriverVerification(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<GenericResponse>
}
