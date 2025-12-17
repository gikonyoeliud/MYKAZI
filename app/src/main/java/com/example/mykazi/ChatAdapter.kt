package com.example.mykazi

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val container: View = itemView.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text

        val params = holder.container.layoutParams as RecyclerView.LayoutParams

        if (message.isSentByCurrentUser) {
            // Sent message: align right
            holder.container.setBackgroundResource(R.drawable.bubble_sent)
            params.marginStart = 50
            params.marginEnd = 0
        } else {
            // Received message: align left
            holder.container.setBackgroundResource(R.drawable.bubble_received)
            params.marginStart = 0
            params.marginEnd = 50
        }
        holder.container.layoutParams = params
    }

    override fun getItemCount(): Int = messages.size
}
