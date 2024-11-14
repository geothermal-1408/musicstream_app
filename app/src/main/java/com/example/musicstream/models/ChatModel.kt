package com.example.musicstream.models

data class ChatModel(
    val message: String = "",
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
