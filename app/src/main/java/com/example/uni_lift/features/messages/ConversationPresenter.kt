package com.example.uni_lift.features.messages

import com.example.uni_lift.core.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ConversationPresenter(
    private val repository: ConversationContract.Repository,
    private val token: String,
    private val conversationId: String
) : ConversationContract.Presenter {

    private var view: ConversationContract.View? = null
    private var state = ConversationContract.UiState()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var refreshJob: Job? = null

    override fun attach(view: ConversationContract.View) {
        this.view = view
        loadConversation()
    }

    override fun detach() {
        scope.launch {
            repository.unsubscribeFromMessages()
        }
        refreshJob?.cancel()
        refreshJob = null
        view = null
        scope.cancel()
    }

    override fun onInputChanged(text: String) {
        state = state.copy(inputText = text)
        view?.render(state)
    }

    override fun onSendClicked() {
        val text = state.inputText.trim()
        if (text.isBlank()) return

        // Clear input immediately for responsiveness
        state = state.copy(inputText = "")
        view?.render(state)

        // Optimistically add the message so the UI updates immediately.
        val localId = "local_${System.currentTimeMillis()}"
        val localMessage = ChatMessage(
            id = localId,
            sender = "self",
            text = text,
            time = LocalTime.now().format(timeFormatter)
        )
        state = state.copy(messages = state.messages + localMessage)
        view?.render(state)

        // Persist to Supabase — the realtime subscription will add it to the UI
        scope.launch {
            repository.sendMessage(conversationId, text).fold(
                onSuccess = { /* message will arrive via realtime */ },
                onFailure = { err ->
                    state = state.copy(error = err.message)
                    view?.render(state)
                }
            )
        }
    }

    override fun onAcceptOffer(msgId: String) {
        val updated = state.messages.map { msg ->
            if (msg.id == msgId) msg.copy(offerStatus = "ACCEPTED") else msg
        }
        val systemMsg = ChatMessage(
            id = "msg_sys_${System.currentTimeMillis()}",
            sender = "system",
            text = "OFFER ACCEPTED",
            time = LocalTime.now().format(timeFormatter)
        )
        state = state.copy(messages = updated + systemMsg)
        view?.render(state)
    }

    override fun onDeclineOffer(msgId: String) {
        val updated = state.messages.map { msg ->
            if (msg.id == msgId) msg.copy(offerStatus = "DECLINED") else msg
        }
        val systemMsg = ChatMessage(
            id = "msg_sys_${System.currentTimeMillis()}",
            sender = "system",
            text = "OFFER DECLINED",
            time = LocalTime.now().format(timeFormatter)
        )
        state = state.copy(messages = updated + systemMsg)
        view?.render(state)
    }

    override fun onSosClicked() {
        val sosMsg = ChatMessage(
            id = "msg_sos_${System.currentTimeMillis()}",
            sender = "system",
            text = "SOS ALERT SENT",
            time = LocalTime.now().format(timeFormatter)
        )
        state = state.copy(messages = state.messages + sosMsg)
        view?.render(state)
    }

    private fun loadConversation() {
        state = state.copy(isLoading = true)
        view?.render(state)

        scope.launch {
            val result = repository.getConversation(token, conversationId)
            result.fold(
                onSuccess = { (conversation, messages) ->
                    state = state.copy(
                        conversation = conversation,
                        messages = messages,
                        isLoading = false
                    )
                    view?.render(state)

                    // Subscribe to realtime messages
                    subscribeToRealtime()
                    startMessageRefresh()
                },
                onFailure = {
                    state = state.copy(isLoading = false, error = it.message)
                    view?.render(state)
                }
            )
        }
    }

    private fun subscribeToRealtime() {
        val currentUserId = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.id ?: return
        scope.launch {
            repository.subscribeToMessages(conversationId, currentUserId) { newMessage ->
                // Replace optimistic local self message if the realtime one arrives.
                val normalized = if (newMessage.sender == "self") {
                    val dropIndex = state.messages.indexOfFirst {
                        it.id.startsWith("local_") && it.sender == "self" && it.text == newMessage.text
                    }
                    if (dropIndex >= 0) state.messages.toMutableList().also { it.removeAt(dropIndex) } else state.messages
                } else {
                    state.messages
                }

                // Avoid duplicates: check if the message ID already exists
                if (normalized.none { it.id == newMessage.id }) {
                    state = state.copy(messages = normalized + newMessage)
                    view?.render(state)
                }
            }
        }
    }

    private fun startMessageRefresh() {
        if (refreshJob != null) return
        refreshJob = scope.launch {
            while (true) {
                delay(4000)
                repository.getConversation(token, conversationId).onSuccess { (_, messages) ->
                    if (messages.size > state.messages.size) {
                        state = state.copy(messages = messages)
                        view?.render(state)
                    }
                }
            }
        }
    }
}
