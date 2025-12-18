package com.example.mykazi

import com.google.firebase.auth.FirebaseAuth




data class Message(
    val text: String = "",
    val senderPhone: String = "",
    val timestamp: Long = 0L
)
