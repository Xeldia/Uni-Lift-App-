package com.example.uni_lift.features.settings.password

import android.content.Context
import com.example.uni_lift.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ChangePasswordPresenter(
    private val repository: ChangePasswordContract.Repository,
    private val context: Context
) : ChangePasswordContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: ChangePasswordContract.View? = null
    private var state = ChangePasswordContract.UiState()

    override fun attach(view: ChangePasswordContract.View) { this.view = view; view.render(state) }
    override fun detach() { view = null; cancel() }

    override fun onOldPasswordChanged(value: String) { state = state.copy(oldPassword = value, error = null); view?.render(state) }
    override fun onNewPasswordChanged(value: String) { state = state.copy(newPassword = value, error = null); view?.render(state) }
    override fun onConfirmPasswordChanged(value: String) { state = state.copy(confirmPassword = value, error = null); view?.render(state) }

    override fun onUpdatePasswordClicked() {
        if (state.oldPassword.isBlank() || state.newPassword.isBlank()) {
            state = state.copy(error = "All fields required"); view?.render(state); return
        }
        if (state.newPassword != state.confirmPassword) {
            state = state.copy(error = "Passwords do not match"); view?.render(state); return
        }
        if (state.newPassword.length < 6) {
            state = state.copy(error = "Password must be at least 6 characters"); view?.render(state); return
        }
        state = state.copy(isLoading = true, error = null); view?.render(state)
        launch {
            val token = "Bearer ${SessionManager(context).fetchAuthToken() ?: ""}"
            val result = repository.changePassword(token, state.oldPassword, state.newPassword)
            state = state.copy(isLoading = false)
            result.fold(
                onSuccess = { view?.render(state); view?.navigateAfterPasswordChange() },
                onFailure = { err -> state = state.copy(error = err.message ?: "Failed"); view?.render(state) }
            )
        }
    }

    override fun onCancelClicked() { view?.navigateBack() }
}
