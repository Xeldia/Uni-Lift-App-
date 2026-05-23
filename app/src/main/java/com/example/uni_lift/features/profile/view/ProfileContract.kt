package com.example.uni_lift.features.profile.view

interface ProfileContract {

    data class UiState(
        val profile: ProfileUiData = ProfileUiData(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
        fun navigateBack()
        fun navigateToUpdateProfile()
        fun navigateToSettings()
        fun navigateToRideHistory()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onBackClicked()
        fun onUpdateProfileClicked()
        fun onSettingsClicked()
        fun onRideHistoryClicked()
    }

    interface Repository {
        suspend fun loadProfile(token: String, userId: String): Result<ProfileUiData>
    }
}
