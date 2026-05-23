package com.example.uni_lift.features.profile.view

data class ProfileUiData(
    val initials: String = "",
    val fullName: String = "",
    val studentId: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val campusLocation: String = "",
    val role: String = "RIDER",
    val accountStatus: String = "PENDING",
    val isVerified: Boolean = false,
    val driverVerificationStatus: String? = null,
    val avatarUrl: String? = null,
    val vehicle: String? = null,
    val vehicleType: String? = null,
    val rating: Double? = null,
    val ridesCompleted: Int? = null,
    val userId: String = ""
)
