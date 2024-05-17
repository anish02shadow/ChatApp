package com.example69.chatapp.data

data class FriendsData(
    val Username: String,
    val Email: String,
    val Mood: String?,
    val lastMessage: String?,
    val lastMessageTime: Long,
)
