package com.example.mykazi

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView =
            itemView.findViewById(R.id.nameTextView)
        private val jobTextView: TextView =
            itemView.findViewById(R.id.jobTextView)
        private val locationTextView: TextView =
            itemView.findViewById(R.id.locationTextView)
        private val phoneTextView: TextView =
            itemView.findViewById(R.id.phoneTextView)

        fun bind(user: User) {
            nameTextView.text = user.name ?: "N/A"
            jobTextView.text = user.job ?: "N/A"
            locationTextView.text = user.location ?: "N/A"
            phoneTextView.text = user.phone ?: "N/A"

            itemView.setOnClickListener {
                Log.d("UserAdapter", "Clicked user: ${user.name}")
                onUserClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
