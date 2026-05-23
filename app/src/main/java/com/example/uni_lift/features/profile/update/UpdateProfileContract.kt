package com.example.uni_lift.features.profile.update

interface UpdateProfileContract {

    data class UiState(
        val name: String = "",
        val email: String = "",
        val studentId: String = "",
        val contactNumber: String = "",
        val campusLocation: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
        fun navigateBack()
        fun navigateAfterSuccess()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onNameChanged(value: String)
        fun onEmailChanged(value: String)
        fun onStudentIdChanged(value: String)
        fun onContactNumberChanged(value: String)
        fun onCampusLocationChanged(value: String)
        fun onSaveClicked()
        fun onCancelClicked()
    }

    interface Repository {
        suspend fun update(token: String, state: UiState): Result<Unit>
    }
}
