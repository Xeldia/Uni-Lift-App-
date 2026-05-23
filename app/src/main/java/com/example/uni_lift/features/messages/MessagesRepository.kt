package com.example.uni_lift.features.messages

import com.example.uni_lift.core.models.User
import com.example.uni_lift.core.supabase.ConversationInsert
import com.example.uni_lift.core.supabase.ConversationRow
import com.example.uni_lift.core.supabase.MessageRow
import com.example.uni_lift.core.supabase.SupabaseProvider
import com.example.uni_lift.core.supabase.UserRow
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MessagesRepository : MessagesContract.Repository {

    override suspend fun getConversations(): Result<List<Conversation>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val client = SupabaseProvider.client
                val userId = client.auth.currentSessionOrNull()?.user?.id
                    ?: error("Not authenticated")

                val convRows = client.from("conversations")
                    .select {
                        filter {
                            or {
                                eq("rider_id", userId)
                                eq("driver_id", userId)
                            }
                        }
                        order("last_message_at", Order.DESCENDING)
                    }
                    .decodeList<ConversationRow>()

                if (convRows.isEmpty()) return@runCatching emptyList<Conversation>()

                // Collect all partner IDs to resolve names in one query
                val partnerIds = convRows.map { row ->
                    if (row.riderId == userId) row.driverId else row.riderId
                }.distinct()

                val userMap = if (partnerIds.isNotEmpty()) {
                    client.from("users")
                        .select {
                            filter { isIn("id", partnerIds) }
                        }
                        .decodeList<UserRow>()
                        .associateBy { it.id }
                } else emptyMap()

                convRows.map { row ->
                    val partnerId = if (row.riderId == userId) row.driverId else row.riderId
                    val partner = userMap[partnerId]
                    val partnerName = partner?.fullName ?: "Unknown User"
                    val partnerEmail = partner?.email ?: ""
                    val partnerRole = partner?.role?.uppercase() ?: ""
                    val initials = partnerName
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .take(2)
                        .joinToString("")
                        .ifEmpty { "?" }

                    // Fetch last message for this conversation
                    val lastMsg = try {
                        client.from("messages")
                            .select {
                                filter { eq("conversation_id", row.id) }
                                order("sent_at", Order.DESCENDING)
                                limit(1)
                            }
                            .decodeList<MessageRow>()
                            .firstOrNull()
                    } catch (_: Exception) { null }

                    Conversation(
                        id = row.id,
                        partnerId = partnerId,
                        partnerName = partnerName,
                        partnerInitials = initials,
                        partnerEmail = partnerEmail,
                        partnerRole = partnerRole,
                        rideId = row.rideId ?: "",
                        pickup = row.pickup ?: "",
                        dropoff = row.dropoff ?: "",
                        lastMessage = lastMsg?.content ?: "",
                        timeAgo = formatTimeAgo(row.lastMessageAt),
                        status = row.status
                    )
                }
            }
        }

    override suspend fun getAvailableUsers(currentUserId: String): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("users")
                    .select {
                        filter { neq("id", currentUserId) }
                    }
                    .decodeList<UserRow>()
                    .filter { (it.accountStatus ?: "ACTIVE") == "ACTIVE" }
                    .map { row ->
                        User(
                            id = row.id,
                            email = row.email,
                            fullName = row.fullName,
                            role = row.role.uppercase(),
                            accountStatus = row.accountStatus ?: "ACTIVE"
                        )
                    }
            }
        }

    override suspend fun startNewConversation(
        currentUserId: String,
        partnerId: String
    ): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val client = SupabaseProvider.client

                // Check if a conversation already exists between these two users
                val existing = client.from("conversations")
                    .select {
                        filter {
                            or {
                                and {
                                    eq("rider_id", currentUserId)
                                    eq("driver_id", partnerId)
                                }
                                and {
                                    eq("rider_id", partnerId)
                                    eq("driver_id", currentUserId)
                                }
                            }
                        }
                    }
                    .decodeList<ConversationRow>()
                    .firstOrNull()

                if (existing != null) {
                    return@runCatching existing.id
                }

                // Create a new conversation
                val inserted = client.from("conversations")
                    .insert(ConversationInsert(
                        riderId = currentUserId,
                        driverId = partnerId,
                        status = "ACTIVE"
                    )) {
                        select()
                    }
                    .decodeSingle<ConversationRow>()

                inserted.id
            }
        }

    private fun formatTimeAgo(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return ""
        return try {
            val instant = Instant.parse(isoTimestamp)
            val now = Instant.now()
            val minutes = ChronoUnit.MINUTES.between(instant, now)
            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                minutes < 1440 -> "${minutes / 60}h ago"
                minutes < 2880 -> "Yesterday"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("MMM d")
                    instant.atZone(ZoneId.systemDefault()).format(formatter)
                }
            }
        } catch (_: Exception) {
            isoTimestamp.take(10)
        }
    }
}
