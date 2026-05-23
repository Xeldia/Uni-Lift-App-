package com.example.uni_lift.features.messages

import com.example.uni_lift.core.supabase.ConversationRow
import com.example.uni_lift.core.supabase.LastMessageAtUpdate
import com.example.uni_lift.core.supabase.MessageInsert
import com.example.uni_lift.core.supabase.MessageRow
import com.example.uni_lift.core.supabase.SupabaseProvider
import com.example.uni_lift.core.supabase.UserRow
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ConversationRepository : ConversationContract.Repository {

    private var realtimeChannel: RealtimeChannel? = null
    private var realtimeScope: CoroutineScope? = null

    override suspend fun getConversation(
        token: String,
        conversationId: String
    ): Result<Pair<Conversation, List<ChatMessage>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val client = SupabaseProvider.client
                val currentUserId = client.auth.currentSessionOrNull()?.user?.id ?: ""

                val convRow = client.from("conversations")
                    .select { filter { eq("id", conversationId) } }
                    .decodeSingle<ConversationRow>()

                // Resolve partner name
                val partnerId = if (convRow.riderId == currentUserId) convRow.driverId else convRow.riderId
                val partnerName = try {
                    client.from("users")
                        .select { filter { eq("id", partnerId) } }
                        .decodeList<UserRow>()
                        .firstOrNull()?.fullName ?: "User"
                } catch (_: Exception) { "User" }

                val initials = partnerName
                    .split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2)
                    .joinToString("")
                    .ifEmpty { "?" }

                val conversation = Conversation(
                    id = convRow.id,
                    partnerId = partnerId,
                    partnerName = partnerName,
                    partnerInitials = initials,
                    rideId = convRow.rideId ?: "",
                    pickup = convRow.pickup ?: "",
                    dropoff = convRow.dropoff ?: "",
                    lastMessage = "",
                    timeAgo = convRow.lastMessageAt ?: "",
                    status = convRow.status
                )

                val messageRows = client.from("messages")
                    .select {
                        filter { eq("conversation_id", conversationId) }
                        order("sent_at", Order.ASCENDING)
                    }
                    .decodeList<MessageRow>()

                val messages = messageRows.map { row ->
                    val sender = when {
                        row.type == "system" -> "system"
                        row.senderId == currentUserId -> "self"
                        else -> "other"
                    }
                    ChatMessage(
                        id = row.id,
                        sender = sender,
                        text = row.content,
                        time = formatMessageTime(row.sentAt),
                        offerAmount = row.offerAmount,
                        offerStatus = row.offerStatus
                    )
                }

                conversation to messages
            }
        }

    override suspend fun sendMessage(
        conversationId: String,
        content: String,
        type: String,
        offerAmount: Double?
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val client = SupabaseProvider.client
                val senderId = client.auth.currentSessionOrNull()?.user?.id
                    ?: error("Not authenticated")
                client.from("messages").insert(
                    MessageInsert(
                        conversationId = conversationId,
                        rideId = null,
                        senderId = senderId,
                        content = content,
                        type = type,
                        offerAmount = offerAmount,
                        offerStatus = if (type == "offer") "PENDING" else null
                    )
                )
                // Update last_message_at on the conversation
                client.from("conversations")
                    .update(LastMessageAtUpdate(lastMessageAt = Instant.now().toString())) {
                        filter { eq("id", conversationId) }
                    }
                Unit
            }
        }

    override suspend fun subscribeToMessages(
        conversationId: String,
        currentUserId: String,
        onNewMessage: (ChatMessage) -> Unit
    ) {
        try {
            val client = SupabaseProvider.client
            // Ensure realtime socket is connected before subscribing.
            client.realtime.connect()
            realtimeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

            val channel = client.channel("messages-$conversationId")
            realtimeChannel = channel

            val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "messages"
                // Server-side filter keeps the channel focused on this conversation.
                filter("conversation_id", FilterOperator.EQ, conversationId)
            }

            flow.onEach { action ->
                val record = action.record
                val msgConvId = record["conversation_id"]?.jsonPrimitive?.content ?: ""
                if (msgConvId != conversationId) return@onEach

                val senderId = record["sender_id"]?.jsonPrimitive?.content ?: ""
                val msgType = record["type"]?.jsonPrimitive?.content ?: "text"
                val sender = when {
                    msgType == "system" -> "system"
                    senderId == currentUserId -> "self"
                    else -> "other"
                }
                val chatMessage = ChatMessage(
                    id = record["id"]?.jsonPrimitive?.content ?: "rt_${System.currentTimeMillis()}",
                    sender = sender,
                    text = record["content"]?.jsonPrimitive?.content ?: "",
                    time = formatMessageTime(record["sent_at"]?.jsonPrimitive?.content),
                    offerAmount = record["offer_amount"]?.jsonPrimitive?.content?.toDoubleOrNull(),
                    offerStatus = record["offer_status"]?.jsonPrimitive?.content
                )
                onNewMessage(chatMessage)
            }.launchIn(realtimeScope!!)

            channel.subscribe()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun unsubscribeFromMessages() {
        try {
            realtimeChannel?.unsubscribe()
            realtimeScope?.cancel()
            realtimeChannel = null
            realtimeScope = null
        } catch (_: Exception) {}
    }

    private fun formatMessageTime(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return ""
        return try {
            val instant = Instant.parse(isoTimestamp)
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            instant.atZone(ZoneId.systemDefault()).format(formatter)
        } catch (_: Exception) {
            isoTimestamp.take(5)
        }
    }
}
