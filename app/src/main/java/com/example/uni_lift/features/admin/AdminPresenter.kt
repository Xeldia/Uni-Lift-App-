package com.example.uni_lift.features.admin

import com.example.uni_lift.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AdminPresenter(
    private val repository: AdminContract.Repository,
    private val sessionManager: SessionManager
) : AdminContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: AdminContract.View? = null
    private var state = AdminContract.UiState()

    override fun attach(view: AdminContract.View) {
        this.view = view
        view.render(state)
        loadStats()
    }

    override fun detach() {
        view = null
        cancel()
    }

    private fun loadStats() {
        val token = sessionManager.fetchAuthToken() ?: return
        state = state.copy(isLoading = true, error = null)
        view?.render(state)
        launch {
            repository.getStats(token).fold(
                onSuccess = { stats ->
                    state = state.copy(isLoading = false, stats = stats)
                    view?.render(state)
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }

    override fun onUsersClicked() {
        view?.navigateToUsers()
    }

    override fun onVerificationsClicked() {
        view?.navigateToVerifications()
    }
}
