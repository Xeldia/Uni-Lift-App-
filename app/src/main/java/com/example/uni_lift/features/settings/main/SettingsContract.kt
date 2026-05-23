package com.example.uni_lift.features.settings.main

interface SettingsContract {

    data class UiState(
        val title: String = "SETTINGS"
    )

    interface View {
        fun render(state: UiState)
        fun navigateBack()
        fun navigateToEditProfile()
        fun navigateToChangePassword()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onBackClicked()
        fun onEditProfileClicked()
        fun onChangePasswordClicked()
    }

    interface Repository {
        fun getInitialState(): UiState
    }
}
