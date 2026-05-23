package com.example.uni_lift.features.rides.driver

import com.example.uni_lift.core.models.RideRecord

data class DriverUiState(
    val isOnline: Boolean = false,
    val requests: List<RideRecord> = emptyList(),
    val activeRide: RideRecord? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
