package com.example.uni_lift.features.admin.users

import com.example.uni_lift.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AdminUsersPresenter(
    private val repository: AdminUsersContract.Repository,
    private val sessionManager: SessionManager
) : AdminUsersContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: AdminUsersContract.View? = null
    private var state = AdminUsersContract.UiState()

    override fun attach(view: AdminUsersContract.View) {
        this.view = view
        view.render(state)
        loadUsers()
    }

    override fun detach() {
        view = null
        cancel()
    }

    private fun loadUsers() {
        val token = sessionManager.fetchAuthToken() ?: return
        state = state.copy(isLoading = true, error = null, actionSuccess = null)
        view?.render(state)
        launch {
            repository.getUsers(token).fold(
                onSuccess = { users ->
                    state = state.copy(isLoading = false, users = users)
                    view?.render(state)
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }

    override fun onSuspendUser(userId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        state = state.copy(isLoading = true, error = null, actionSuccess = null)
        view?.render(state)
        launch {
            repository.suspendUser(token, userId).fold(
                onSuccess = {
                    state = state.copy(actionSuccess = "User suspended successfully.")
                    view?.render(state)
                    loadUsers()
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }

    override fun onReactivateUser(userId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        state = state.copy(isLoading = true, error = null, actionSuccess = null)
        view?.render(state)
        launch {
            repository.reactivateUser(token, userId).fold(
                onSuccess = {
                    state = state.copy(actionSuccess = "User reactivated successfully.")
                    view?.render(state)
                    loadUsers()
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }
}
