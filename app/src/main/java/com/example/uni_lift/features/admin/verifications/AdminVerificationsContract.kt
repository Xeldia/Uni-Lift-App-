package com.example.uni_lift.features.admin.verifications

import com.example.uni_lift.core.models.User

interface AdminVerificationsContract {

    data class UiState(
        val tab: VerifTab = VerifTab.ACCOUNT,
        val items: List<User> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onTabSelected(tab: VerifTab)
        fun onApprove(userId: String)
        fun onReject(userId: String)
    }

    interface Repository {
        suspend fun getPendingAccount(token: String): Result<List<User>>
        suspend fun getPendingDriver(token: String): Result<List<User>>
        suspend fun approveAccount(token: String, userId: String): Result<Unit>
        suspend fun approveDriver(token: String, userId: String): Result<Unit>
        suspend fun rejectAccount(token: String, userId: String): Result<Unit>
    }
}
