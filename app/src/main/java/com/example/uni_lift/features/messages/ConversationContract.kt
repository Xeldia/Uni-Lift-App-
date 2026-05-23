package com.example.uni_lift.features.messages

interface ConversationContract {

    data class UiState(
        val conversation: Conversation? = null,
        val messages: List<ChatMessage> = emptyList(),
        val inputText: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    interface View {
        fun render(state: UiState)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onSendClicked()
        fun onInputChanged(text: String)
        fun onAcceptOffer(msgId: String)
        fun onDeclineOffer(msgId: String)
        fun onSosClicked()
    }

    interface Repository {
        suspend fun getConversation(token: String, conversationId: String): Result<Pair<Conversation, List<ChatMessage>>>
        suspend fun sendMessage(conversationId: String, content: String, type: String = "text", offerAmount: Double? = null): Result<Unit>
        suspend fun subscribeToMessages(conversationId: String, currentUserId: String, onNewMessage: (ChatMessage) -> Unit)
        suspend fun unsubscribeFromMessages()
    }
}
