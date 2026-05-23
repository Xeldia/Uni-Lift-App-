package com.example.uni_lift.features.rides.dashboard

import com.example.uni_lift.core.models.RideRecord

interface DashboardContract {

    data class UiState(
        val welcomeName: String = "",
        val form: BookingForm = BookingForm(),
        val activeRide: RideRecord? = null,
        val isSearching: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val showMap: Boolean = false
    )

    interface View {
        fun render(state: UiState)
        fun navigateToProfile()
        fun navigateToLogin()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onProfileClicked()
        fun onLogoutClicked()
        fun onPickupLocationSelected(label: String, lat: Double, lng: Double)
        fun onDestinationLocationSelected(label: String, lat: Double, lng: Double)
        fun onPassengerCountChanged(count: Int)
        fun onFareChanged(value: String)
        fun onVehicleTypeChanged(type: String)
        fun onFindDriverClicked()
        fun onCancelRideClicked()
        fun onToggleMap()
    }

    interface Repository {
        suspend fun getActiveRide(token: String): Result<RideRecord?>
        suspend fun createRide(token: String, form: BookingForm): Result<RideRecord>
        suspend fun cancelRide(token: String, rideId: String): Result<Unit>
        suspend fun subscribeToRideUpdates(rideId: String, onUpdate: (RideRecord) -> Unit)
        fun unsubscribeFromRideUpdates()
    }
}
