package com.example.mykazi

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChatAdapter(
    private val messages: List<Message>,
    private val myPhone: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val left: TextView = view.findViewById(R.id.textLeft)
        val right: TextView = view.findViewById(R.id.textRight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]

        if (msg.senderPhone == myPhone) {
            holder.right.text = msg.text
            holder.right.visibility = View.VISIBLE
            holder.left.visibility = View.GONE
        } else {
            holder.left.text = msg.text
            holder.left.visibility = View.VISIBLE
            holder.right.visibility = View.GONE
        }
    }

    override fun getItemCount() = messages.size
}
