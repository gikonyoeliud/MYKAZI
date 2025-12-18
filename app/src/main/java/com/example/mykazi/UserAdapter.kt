package com.example.mykazi

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var users: List<User>,
    private var blockedUsers: Set<String>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.userName)
        val jobTextView: TextView = itemView.findViewById(R.id.userJob)
        val locationTextView: TextView = itemView.findViewById(R.id.userLocation)
        val blockedLabel: TextView = itemView.findViewById(R.id.blockedLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.nameTextView.text = user.name
        holder.jobTextView.text = user.job
        holder.locationTextView.text = user.location

        if (blockedUsers.contains(user.phone)) {
            holder.blockedLabel.visibility = View.VISIBLE
            holder.nameTextView.setTextColor(Color.GRAY)
            holder.jobTextView.setTextColor(Color.GRAY)
            holder.locationTextView.setTextColor(Color.GRAY)
        } else {
            holder.blockedLabel.visibility = View.GONE
            holder.nameTextView.setTextColor(Color.BLACK)
            holder.jobTextView.setTextColor(Color.BLACK)
            holder.locationTextView.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener { onUserClick(user) }
    }

    fun updateUsers(newUsers: List<User>, newBlocked: Set<String>) {
        this.users = newUsers
        this.blockedUsers = newBlocked
        notifyDataSetChanged()
    }
}
