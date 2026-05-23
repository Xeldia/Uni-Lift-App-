package com.example.uni_lift.features.profile.update

import android.content.Context
import com.example.uni_lift.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class UpdateProfilePresenter(
    private val repository: UpdateProfileContract.Repository,
    private val context: Context
) : UpdateProfileContract.Presenter, CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob()) {

    private var view: UpdateProfileContract.View? = null
    private var state = UpdateProfileContract.UiState()

    override fun attach(view: UpdateProfileContract.View) {
        this.view = view
        // Pre-fill from session cache
        val session = SessionManager(context)
        state = state.copy(
            name = session.fetchUserName(),
            email = session.fetchUserEmail()
        )
        view.render(state)
    }

    override fun detach() { view = null; cancel() }

    override fun onNameChanged(value: String) { state = state.copy(name = value, error = null); view?.render(state) }
    override fun onEmailChanged(value: String) { state = state.copy(email = value, error = null); view?.render(state) }
    override fun onStudentIdChanged(value: String) { state = state.copy(studentId = value, error = null); view?.render(state) }
    override fun onContactNumberChanged(value: String) { state = state.copy(contactNumber = value, error = null); view?.render(state) }
    override fun onCampusLocationChanged(value: String) { state = state.copy(campusLocation = value, error = null); view?.render(state) }

    override fun onSaveClicked() {
        if (state.name.isBlank()) {
            state = state.copy(error = "Name cannot be blank")
            view?.render(state); return
        }
        state = state.copy(isLoading = true, error = null)
        view?.render(state)
        launch {
            val token = "Bearer ${SessionManager(context).fetchAuthToken() ?: ""}"
            val result = repository.update(token, state)
            state = state.copy(isLoading = false)
            result.fold(
                onSuccess = { view?.render(state); view?.navigateAfterSuccess() },
                onFailure = { err -> state = state.copy(error = err.message ?: "Update failed"); view?.render(state) }
            )
        }
    }

    override fun onCancelClicked() { view?.navigateBack() }
}
