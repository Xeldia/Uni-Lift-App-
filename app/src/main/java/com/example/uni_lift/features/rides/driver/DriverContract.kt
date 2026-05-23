package com.example.uni_lift.features.rides.driver

import com.example.uni_lift.core.models.RideRecord

interface DriverContract {

    data class UiState(
        val isOnline: Boolean = false,
        val requests: List<RideRecord> = emptyList(),
        val activeRide: RideRecord? = null,
        val isLoading: Boolean = false,
        val isSimulating: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
        fun showError(message: String)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onToggleOnline()
        fun onAcceptRide(rideId: String)
        fun onDeclineRide(rideId: String)
        fun onStartRide(rideId: String)
        fun onSimulateRide(rideId: String)
        fun onCompleteRide(rideId: String)
    }

    interface Repository {
        suspend fun getRidingRequests(token: String): Result<List<RideRecord>>
        suspend fun acceptRide(token: String, id: String): Result<Unit>
        suspend fun startRide(token: String, id: String): Result<Unit>
        suspend fun completeRide(token: String, id: String): Result<Unit>
        suspend fun updateDriverLocation(rideId: String, lat: Double, lng: Double): Result<Unit>
    }
}
