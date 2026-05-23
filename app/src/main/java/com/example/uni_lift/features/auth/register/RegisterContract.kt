package com.example.uni_lift.features.auth.register

interface RegisterContract {

    data class UiState(
        val fullName: String = "",
        val studentId: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
        fun navigateAfterRegister()
        fun navigateBackToLogin()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onFullNameChanged(value: String)
        fun onStudentIdChanged(value: String)
        fun onEmailChanged(value: String)
        fun onPasswordChanged(value: String)
        fun onConfirmPasswordChanged(value: String)
        fun onRegisterClicked()
        fun onBackToLoginClicked()
    }

    interface Repository {
        suspend fun register(fullName: String, studentId: String, email: String, password: String): Result<Unit>
    }
}
