package com.example.uni_lift.features.rides.history

import com.example.uni_lift.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RideHistoryPresenter(
    private val repository: RideHistoryContract.Repository,
    private val sessionManager: SessionManager
) : RideHistoryContract.Presenter,
    CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: RideHistoryContract.View? = null
    private var state = RideHistoryContract.UiState()

    override fun attach(view: RideHistoryContract.View) {
        this.view = view
        view.render(state)
        loadRides()
    }

    override fun detach() {
        view = null
        cancel()
    }

    private fun loadRides() {
        val token = sessionManager.fetchAuthToken() ?: run {
            state = state.copy(error = "Session expired. Please log in again.")
            view?.render(state)
            return
        }

        state = state.copy(isLoading = true, error = null)
        view?.render(state)

        launch {
            val result = repository.getRides(token)
            result.fold(
                onSuccess = { rides ->
                    state = state.copy(isLoading = false, rides = rides, error = null)
                    view?.render(state)
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message ?: "Failed to load rides")
                    view?.render(state)
                }
            )
        }
    }
}
