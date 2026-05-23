package com.example.uni_lift.features.profile.view

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ProfilePresenter(
    private val repository: ProfileContract.Repository
) : ProfileContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: ProfileContract.View? = null
    private var state = ProfileContract.UiState()

    override fun attach(view: ProfileContract.View) {
        this.view = view
        view.render(state)
        loadProfile()
    }

    override fun detach() { view = null; cancel() }

    private fun loadProfile() {
        state = state.copy(isLoading = true)
        view?.render(state)
        launch {
            val result = repository.loadProfile("", "")
            state = state.copy(isLoading = false)
            result.fold(
                onSuccess = { profile -> state = state.copy(profile = profile); view?.render(state) },
                onFailure = { err -> state = state.copy(error = err.message); view?.render(state) }
            )
        }
    }

    override fun onBackClicked() { view?.navigateBack() }
    override fun onUpdateProfileClicked() { view?.navigateToUpdateProfile() }
    override fun onSettingsClicked() { view?.navigateToSettings() }
    override fun onRideHistoryClicked() { view?.navigateToRideHistory() }
}
