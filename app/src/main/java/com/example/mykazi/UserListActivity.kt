package com.example.mykazi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class UserListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val userList = mutableListOf<User>()
    private lateinit var adapter: UserAdapter
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = UserAdapter(userList)
        recyclerView.adapter = adapter

        db = FirebaseDatabase.getInstance().reference.child("users")
        fetchUsersFromFirebase()
    }

    private fun fetchUsersFromFirebase() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null) userList.add(user)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Optionally, show an error message
            }
        })
    }
}
