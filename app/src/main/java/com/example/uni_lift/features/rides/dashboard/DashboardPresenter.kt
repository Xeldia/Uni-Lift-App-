package com.example.uni_lift.features.rides.dashboard

import android.content.Context
import com.example.uni_lift.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DashboardPresenter(
    private val repository: DashboardContract.Repository,
    private val context: Context
) : DashboardContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: DashboardContract.View? = null
    private var state = DashboardContract.UiState()
    private var subscribedRideId: String? = null

    override fun attach(view: DashboardContract.View) {
        this.view = view
        val session = SessionManager(context)
        state = state.copy(welcomeName = session.fetchUserName().split(" ").firstOrNull() ?: "")
        view.render(state)
        loadActiveRide()
    }

    override fun detach() {
        repository.unsubscribeFromRideUpdates()
        subscribedRideId = null
        view = null
        cancel()
    }

    private fun loadActiveRide() {
        launch {
            val session = SessionManager(context)
            val token = "Bearer ${session.fetchAuthToken() ?: return@launch}"
            val result = repository.getActiveRide(token)
            result.getOrNull()?.let { ride ->
                state = state.copy(activeRide = ride)
                view?.render(state)
                subscribeIfNeeded(ride.id)
            }
        }
    }

    private fun subscribeIfNeeded(rideId: String) {
        if (subscribedRideId == rideId) return
        subscribedRideId = rideId
        launch {
            repository.subscribeToRideUpdates(rideId) { updated ->
                state = state.copy(activeRide = updated)
                view?.render(state)
            }
        }
    }

    override fun onPickupLocationSelected(label: String, lat: Double, lng: Double) {
        state = state.copy(form = state.form.copy(pickup = label, pickupLat = lat, pickupLng = lng), error = null)
        view?.render(state)
    }

    override fun onDestinationLocationSelected(label: String, lat: Double, lng: Double) {
        state = state.copy(form = state.form.copy(dropoff = label, dropoffLat = lat, dropoffLng = lng), error = null)
        view?.render(state)
    }

    override fun onPassengerCountChanged(count: Int) {
        state = state.copy(form = state.form.copy(passengerCount = count.coerceIn(1, 4)))
        view?.render(state)
    }

    override fun onFareChanged(value: String) {
        state = state.copy(form = state.form.copy(fare = value), error = null)
        view?.render(state)
    }

    override fun onVehicleTypeChanged(type: String) {
        state = state.copy(form = state.form.copy(vehicleType = type))
        view?.render(state)
    }

    override fun onFindDriverClicked() {
        if (state.form.dropoff.isBlank()) {
            state = state.copy(error = "Please enter a destination")
            view?.render(state); return
        }
        if (state.form.fare.isBlank() || state.form.fare.toDoubleOrNull() == null) {
            state = state.copy(error = "Please enter a valid fare")
            view?.render(state); return
        }
        state = state.copy(isSearching = true, isLoading = true, error = null)
        view?.render(state)

        launch {
            val session = SessionManager(context)
            val token = "Bearer ${session.fetchAuthToken() ?: run {
                state = state.copy(isSearching = false, isLoading = false, error = "Not logged in")
                view?.render(state); return@launch
            }}"
            val result = repository.createRide(token, state.form)
            state = state.copy(isLoading = false)
            result.fold(
                onSuccess = { ride ->
                    state = state.copy(activeRide = ride, form = BookingForm(), isSearching = false,
                        successMessage = "Ride request created! Searching for a driver...")
                    view?.render(state)
                    subscribeIfNeeded(ride.id)
                },
                onFailure = { err ->
                    state = state.copy(isSearching = false, error = err.message ?: "Failed to create ride")
                    view?.render(state)
                }
            )
        }
    }

    override fun onCancelRideClicked() {
        val rideId = state.activeRide?.id ?: return
        state = state.copy(isLoading = true)
        view?.render(state)
        launch {
            val session = SessionManager(context)
            val token = "Bearer ${session.fetchAuthToken() ?: return@launch}"
            repository.cancelRide(token, rideId)
            repository.unsubscribeFromRideUpdates()
            subscribedRideId = null
            state = state.copy(isLoading = false, activeRide = null, successMessage = "Ride cancelled")
            view?.render(state)
        }
    }

    override fun onToggleMap() {
        state = state.copy(showMap = !state.showMap)
        view?.render(state)
    }

    override fun onProfileClicked() { view?.navigateToProfile() }
    override fun onLogoutClicked() {
        SessionManager(context).clearSession()
        view?.navigateToLogin()
    }
}
