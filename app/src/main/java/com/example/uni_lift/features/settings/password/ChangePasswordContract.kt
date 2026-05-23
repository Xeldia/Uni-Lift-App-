package com.example.uni_lift.features.settings.password

interface ChangePasswordContract {

    data class UiState(
        val oldPassword: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
        fun navigateBack()
        fun navigateAfterPasswordChange()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onOldPasswordChanged(value: String)
        fun onNewPasswordChanged(value: String)
        fun onConfirmPasswordChanged(value: String)
        fun onUpdatePasswordClicked()
        fun onCancelClicked()
    }

    interface Repository {
        suspend fun changePassword(token: String, oldPassword: String, newPassword: String): Result<Unit>
    }
}
