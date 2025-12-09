package com.example.mykazi

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val userList: List<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nameTextView: TextView=itemView.findViewById(R.id.nameTextView)
        val jobTextView: TextView = itemView.findViewById(R.id.jobTextView)
        val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.nameTextView.text="Name: ${user.name}"
        holder.jobTextView.text = "Job: ${user.job}"
        holder.phoneTextView.text = "Phone: ${user.phone}"
        holder.locationTextView.text = "Location: ${user.location}"

        // 👉 CLICK LISTENER HERE
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UserDetailsActivity::class.java)

            intent.putExtra("name", user.name)
            intent.putExtra("job", user.job)
            intent.putExtra("phone", user.phone)
            intent.putExtra("location", user.location)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size
}
