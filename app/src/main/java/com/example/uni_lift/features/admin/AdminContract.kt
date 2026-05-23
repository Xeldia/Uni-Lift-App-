package com.example.uni_lift.features.admin

interface AdminContract {

    data class UiState(
        val stats: AdminStats? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
        fun navigateToUsers()
        fun navigateToVerifications()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onUsersClicked()
        fun onVerificationsClicked()
    }

    interface Repository {
        suspend fun getStats(token: String): Result<AdminStats>
    }
}
