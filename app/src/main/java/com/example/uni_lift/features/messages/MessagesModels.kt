package com.example.uni_lift.features.messages

data class MessageItem(
    val id: String,
    val sender: String,
    val preview: String,
    val timestamp: String
)

data class Conversation(
    val id: String,
    val partnerId: String = "",
    val partnerName: String,
    val partnerInitials: String,
    val partnerEmail: String = "",
    val partnerRole: String = "",
    val rideId: String,
    val pickup: String,
    val dropoff: String,
    val lastMessage: String,
    val timeAgo: String,
    val status: String
)

data class ChatMessage(
    val id: String,
    val sender: String, // "self" | "other" | "system"
    val text: String,
    val time: String,
    val offerAmount: Double? = null,
    val offerStatus: String? = null
)
