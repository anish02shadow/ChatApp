package com.example69.chatapp.data

data class Message(
    val message: String,
    val timestamp: Long,
    val direction: Boolean
)