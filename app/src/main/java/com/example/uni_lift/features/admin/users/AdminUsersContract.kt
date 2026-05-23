package com.example.uni_lift.features.admin.users

import com.example.uni_lift.core.models.User

interface AdminUsersContract {

    data class UiState(
        val users: List<User> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val actionSuccess: String? = null
    )

    interface View {
        fun render(state: UiState)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onSuspendUser(userId: String)
        fun onReactivateUser(userId: String)
    }

    interface Repository {
        suspend fun getUsers(token: String): Result<List<User>>
        suspend fun suspendUser(token: String, userId: String): Result<Unit>
        suspend fun reactivateUser(token: String, userId: String): Result<Unit>
    }
}
