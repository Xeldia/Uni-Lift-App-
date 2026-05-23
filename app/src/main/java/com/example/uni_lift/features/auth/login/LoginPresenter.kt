package com.example.uni_lift.features.auth.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LoginPresenter(
    private val repository: LoginContract.Repository
) : LoginContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: LoginContract.View? = null
    private var state = LoginContract.UiState()

    override fun attach(view: LoginContract.View) {
        this.view = view
        view.render(state)
    }

    override fun detach() {
        view = null
        cancel()
    }

    override fun onEmailChanged(value: String) {
        state = state.copy(email = value, error = null)
        view?.render(state)
    }

    override fun onPasswordChanged(value: String) {
        state = state.copy(password = value, error = null)
        view?.render(state)
    }

    override fun onSaveActiveChanged(value: Boolean) {
        state = state.copy(saveActive = value)
        view?.render(state)
    }

    override fun onLoginClicked() {
        if (state.email.isBlank() || state.password.isBlank()) {
            state = state.copy(error = "Email and password required")
            view?.render(state)
            return
        }
        state = state.copy(isLoading = true, error = null)
        view?.render(state)

        launch {
            val result = repository.login(state.email.trim(), state.password)
            state = state.copy(isLoading = false)
            result.fold(
                onSuccess = { authData ->
                    view?.render(state)
                    view?.navigateToDashboard(authData.user.role)
                },
                onFailure = { err ->
                    state = state.copy(error = err.message ?: "Login failed")
                    view?.render(state)
                }
            )
        }
    }

    override fun onRegisterClicked() {
        view?.navigateToRegister()
    }
}
