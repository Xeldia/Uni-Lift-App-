package com.example.uni_lift.features.rides.driver

import android.content.Context
import android.location.LocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DriverPresenter(
    private val repository: DriverContract.Repository,
    private val context: Context
) : DriverContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: DriverContract.View? = null
    private var state = DriverContract.UiState()
    private var pollJob: Job? = null
    private var simulateJob: Job? = null
    private var locationManager: LocationManager? = null
    private var locationListener: android.location.LocationListener? = null

    override fun attach(view: DriverContract.View) {
        this.view = view
        view.render(state)
        if (state.isOnline) {
            startPolling()
        }
    }

    override fun detach() {
        view = null
        stopPolling()
        stopLocationUpdates()
        simulateJob?.cancel()
        cancel()
    }

    override fun onToggleOnline() {
        val goingOnline = !state.isOnline
        state = state.copy(isOnline = goingOnline, error = null)
        view?.render(state)

        if (goingOnline) {
            startPolling()
        } else {
            stopPolling()
            stopLocationUpdates()
            state = state.copy(requests = emptyList(), activeRide = null)
            view?.render(state)
        }
    }

    override fun onAcceptRide(rideId: String) {
        val token = (repository as? DriverRepository)?.getToken() ?: run {
            state = state.copy(error = "Session expired")
            view?.render(state)
            return
        }

        state = state.copy(isLoading = true, error = null)
        view?.render(state)

        launch {
            val result = repository.acceptRide(token, rideId)
            result.fold(
                onSuccess = {
                    val accepted = state.requests.find { it.id == rideId }
                    state = state.copy(isLoading = false, activeRide = accepted, requests = emptyList())
                    view?.render(state)
                    accepted?.let { startLocationUpdates(it.id) }
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message ?: "Failed to accept ride")
                    view?.render(state)
                }
            )
        }
    }

    override fun onDeclineRide(rideId: String) {
        state = state.copy(requests = state.requests.filter { it.id != rideId }, error = null)
        view?.render(state)
    }

    override fun onStartRide(rideId: String) {
        val token = (repository as? DriverRepository)?.getToken() ?: run {
            state = state.copy(error = "Session expired"); view?.render(state); return
        }
        state = state.copy(isLoading = true, error = null)
        view?.render(state)
        launch {
            repository.startRide(token, rideId).fold(
                onSuccess = {
                    state = state.copy(
                        isLoading = false,
                        activeRide = state.activeRide?.copy(status = "IN_PROGRESS")
                    )
                    view?.render(state)
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message ?: "Failed to start ride")
                    view?.render(state)
                }
            )
        }
    }

    override fun onSimulateRide(rideId: String) {
        val r = state.activeRide ?: return
        val pLat = r.pickupLat ?: return
        val pLng = r.pickupLng ?: return
        val dLat = r.dropoffLat ?: return
        val dLng = r.dropoffLng ?: return
        val token = (repository as? DriverRepository)?.getToken() ?: return
        simulateJob?.cancel()
        state = state.copy(isSimulating = true, activeRide = state.activeRide?.copy(status = "IN_PROGRESS"))
        view?.render(state)
        simulateJob = launch {
            repository.startRide(token, rideId)
            for (step in 0..60) {
                val t = step / 60.0
                val lat = pLat + (dLat - pLat) * t
                val lng = pLng + (dLng - pLng) * t
                repository.updateDriverLocation(rideId, lat, lng)
                state = state.copy(activeRide = state.activeRide?.copy(driverLat = lat, driverLng = lng))
                view?.render(state)
                delay(2000L)
            }
            state = state.copy(isSimulating = false)
            view?.render(state)
        }
    }

    override fun onCompleteRide(rideId: String) {
        val token = (repository as? DriverRepository)?.getToken() ?: run {
            state = state.copy(error = "Session expired")
            view?.render(state)
            return
        }

        state = state.copy(isLoading = true, error = null)
        view?.render(state)

        launch {
            val result = repository.completeRide(token, rideId)
            result.fold(
                onSuccess = {
                    stopLocationUpdates()
                    state = state.copy(isLoading = false, activeRide = null, requests = emptyList())
                    view?.render(state)
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message ?: "Failed to complete ride")
                    view?.render(state)
                }
            )
        }
    }

    private fun startLocationUpdates(rideId: String) {
        if (locationListener != null) return
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = lm
        val listener = android.location.LocationListener { location ->
            launch {
                repository.updateDriverLocation(rideId, location.latitude, location.longitude)
            }
        }
        locationListener = listener
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000L, 10f, listener)
        } catch (_: SecurityException) {}
    }

    private fun stopLocationUpdates() {
        locationListener?.let { locationManager?.removeUpdates(it) }
        locationListener = null
        locationManager = null
    }

    private fun startPolling() {
        stopPolling()
        pollJob = launch {
            while (true) {
                loadRequests()
                delay(10_000L)
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private suspend fun loadRequests() {
        val token = (repository as? DriverRepository)?.getToken() ?: return
        val result = repository.getRidingRequests(token)
        result.fold(
            onSuccess = { rides ->
                if (state.activeRide == null) {
                    state = state.copy(requests = rides, error = null)
                    view?.render(state)
                }
            },
            onFailure = { err ->
                state = state.copy(error = err.message ?: "Failed to load requests")
                view?.render(state)
            }
        )
    }
}
