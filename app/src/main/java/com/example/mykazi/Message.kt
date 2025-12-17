package com.example.mykazi

import com.google.firebase.auth.FirebaseAuth




data class Message(
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L
) {
    val isSentByCurrentUser: Boolean
        get() = senderId == FirebaseAuth.getInstance().currentUser?.phoneNumber?.takeLast(9)
}
