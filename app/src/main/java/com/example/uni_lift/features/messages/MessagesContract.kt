package com.example.uni_lift.features.messages

import com.example.uni_lift.core.models.User

interface MessagesContract {

    data class UiState(
        val conversations: List<Conversation> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showNewChatDialog: Boolean = false,
        val availableUsers: List<User> = emptyList(),
        val isCreatingChat: Boolean = false
    )

    interface View {
        fun render(state: UiState)
        fun navigateToConversation(conversationId: String)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onConversationClicked(id: String)
        fun onNewChatClicked()
        fun onDismissNewChat()
        fun onUserSelected(userId: String)
    }

    interface Repository {
        suspend fun getConversations(): Result<List<Conversation>>
        suspend fun getAvailableUsers(currentUserId: String): Result<List<User>>
        suspend fun startNewConversation(currentUserId: String, partnerId: String): Result<String>
    }
}
