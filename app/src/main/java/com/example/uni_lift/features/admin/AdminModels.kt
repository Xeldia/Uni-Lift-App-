package com.example.uni_lift.features.admin

data class AdminStats(
    val totalUsers: Int,
    val activeDrivers: Int,
    val ridesInProgress: Int,
    val pendingVerifications: Int,
    val completedRides: Int
)
