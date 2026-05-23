package com.example.uni_lift.features.messages

import com.example.uni_lift.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MessagesPresenter(
    private val repository: MessagesContract.Repository,
    private val sessionManager: SessionManager
) : MessagesContract.Presenter {

    private var view: MessagesContract.View? = null
    private var state = MessagesContract.UiState()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun attach(view: MessagesContract.View) {
        this.view = view
        view.render(state)
        loadConversations()
        loadUsersForSearch()
    }

    override fun detach() {
        view = null
        scope.cancel()
    }

    override fun onConversationClicked(id: String) {
        view?.navigateToConversation(id)
    }

    override fun onNewChatClicked() {
        val userId = sessionManager.fetchUserId() ?: return
        state = state.copy(showNewChatDialog = true, isCreatingChat = false)
        view?.render(state)
        scope.launch {
            repository.getAvailableUsers(userId).fold(
                onSuccess = { users ->
                    state = state.copy(availableUsers = users)
                    view?.render(state)
                },
                onFailure = {
                    state = state.copy(error = it.message)
                    view?.render(state)
                }
            )
        }
    }

    override fun onDismissNewChat() {
        state = state.copy(showNewChatDialog = false, availableUsers = emptyList())
        view?.render(state)
    }

    override fun onUserSelected(userId: String) {
        val currentUserId = sessionManager.fetchUserId() ?: return
        state = state.copy(isCreatingChat = true)
        view?.render(state)
        scope.launch {
            repository.startNewConversation(currentUserId, userId).fold(
                onSuccess = { conversationId ->
                    state = state.copy(showNewChatDialog = false, isCreatingChat = false, availableUsers = emptyList())
                    view?.render(state)
                    view?.navigateToConversation(conversationId)
                },
                onFailure = { err ->
                    state = state.copy(isCreatingChat = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }

    private fun loadConversations() {
        state = state.copy(isLoading = true, error = null)
        view?.render(state)
        scope.launch {
            repository.getConversations().fold(
                onSuccess = { conversations ->
                    state = state.copy(conversations = conversations, isLoading = false)
                    view?.render(state)
                },
                onFailure = { err ->
                    state = state.copy(isLoading = false, error = err.message)
                    view?.render(state)
                }
            )
        }
    }

    private fun loadUsersForSearch() {
        val currentUserId = sessionManager.fetchUserId() ?: return
        scope.launch {
            repository.getAvailableUsers(currentUserId).fold(
                onSuccess = { users ->
                    state = state.copy(availableUsers = users)
                    view?.render(state)
                },
                onFailure = { err ->
                    state = state.copy(error = err.message)
                    view?.render(state)
                }
            )
        }
    }
}
