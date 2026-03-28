package com.example.uni_lift.api

import com.example.uni_lift.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("users/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<LoginResponse> // Using LoginResponse structure as it contains User data

    @PUT("users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>
}
