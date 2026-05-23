package com.example.uni_lift.features.auth.register

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RegisterPresenter(
    private val repository: RegisterContract.Repository
) : RegisterContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: RegisterContract.View? = null
    private var state = RegisterContract.UiState()

    override fun attach(view: RegisterContract.View) {
        this.view = view
        view.render(state)
    }

    override fun detach() { view = null; cancel() }

    override fun onFullNameChanged(value: String) { state = state.copy(fullName = value, error = null); view?.render(state) }
    override fun onStudentIdChanged(value: String) { state = state.copy(studentId = value, error = null); view?.render(state) }
    override fun onEmailChanged(value: String) { state = state.copy(email = value, error = null); view?.render(state) }
    override fun onPasswordChanged(value: String) { state = state.copy(password = value, error = null); view?.render(state) }
    override fun onConfirmPasswordChanged(value: String) { state = state.copy(confirmPassword = value, error = null); view?.render(state) }

    override fun onRegisterClicked() {
        if (state.fullName.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            state = state.copy(error = "Please fill in all fields")
            view?.render(state); return
        }
        if (state.password != state.confirmPassword) {
            state = state.copy(error = "Passwords do not match")
            view?.render(state); return
        }
        if (state.password.length < 6) {
            state = state.copy(error = "Password must be at least 6 characters")
            view?.render(state); return
        }
        state = state.copy(isLoading = true, error = null)
        view?.render(state)

        launch {
            val result = repository.register(state.fullName.trim(), state.studentId.trim(), state.email.trim(), state.password)
            state = state.copy(isLoading = false)
            result.fold(
                onSuccess = { view?.render(state); view?.navigateAfterRegister() },
                onFailure = { err -> state = state.copy(error = err.message ?: "Registration failed"); view?.render(state) }
            )
        }
    }

    override fun onBackToLoginClicked() { view?.navigateBackToLogin() }
}
