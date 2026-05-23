package com.example.uni_lift.features.auth.login

interface LoginContract {

    data class UiState(
        val email: String = "",
        val password: String = "",
        val saveActive: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
        fun navigateToDashboard(role: String)
        fun navigateToRegister()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onEmailChanged(value: String)
        fun onPasswordChanged(value: String)
        fun onSaveActiveChanged(value: Boolean)
        fun onLoginClicked()
        fun onRegisterClicked()
    }

    interface Repository {
        suspend fun login(email: String, password: String): Result<AuthData>
    }
}
