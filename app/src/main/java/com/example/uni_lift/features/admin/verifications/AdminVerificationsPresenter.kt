package com.example.uni_lift.features.admin.verifications

import com.example.uni_lift.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AdminVerificationsPresenter(
    private val repository: AdminVerificationsContract.Repository,
    private val sessionManager: SessionManager
) : AdminVerificationsContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: AdminVerificationsContract.View? = null
    private var state = AdminVerificationsContract.UiState()

    override fun attach(view: AdminVerificationsContract.View) {
        this.view = view
        view.render(state)
        loadForCurrentTab()
    }

    override fun detach() {
        view = null
        cancel()
    }

    override fun onTabSelected(tab: VerifTab) {
        state = state.copy(tab = tab, items = emptyList(), error = null)
        view?.render(state)
        loadForCurrentTab()
    }

    override fun onApprove(userId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        state = state.copy(isLoading = true, error = null)
        view?.render(state)
        launch {
            val result = when (state.tab) {
                VerifTab.ACCOUNT -> repository.approveAccount(token, userId)
                VerifTab.DRIVER -> repository.approveDriver(token, userId)
            }
            result.fold(
                onSuccess = { loadForCurrentTab() },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }

    override fun onReject(userId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        state = state.copy(isLoading = true, error = null)
        view?.render(state)
        launch {
            val result = when (state.tab) {
                VerifTab.ACCOUNT -> repository.rejectAccount(token, userId)
                VerifTab.DRIVER -> repository.rejectAccount(token, userId)
            }
            result.fold(
                onSuccess = { loadForCurrentTab() },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }

    private fun loadForCurrentTab() {
        val token = sessionManager.fetchAuthToken() ?: return
        state = state.copy(isLoading = true, error = null)
        view?.render(state)
        launch {
            val result = when (state.tab) {
                VerifTab.ACCOUNT -> repository.getPendingAccount(token)
                VerifTab.DRIVER -> repository.getPendingDriver(token)
            }
            result.fold(
                onSuccess = { items ->
                    state = state.copy(isLoading = false, items = items)
                    view?.render(state)
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }
}
