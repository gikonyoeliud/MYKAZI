package com.example.mykazi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class UserListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private val originalUserList = mutableListOf<User>()
    private lateinit var adapter: UserAdapter
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = UserAdapter(originalUserList) { user ->
            Log.d("UserListActivity", "Opening details for ${user.name}")

            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("job", user.job)
            intent.putExtra("phone", user.phone)
            intent.putExtra("location", user.location)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        db = FirebaseDatabase.getInstance().reference.child("users")

        setupSearchView()
        fetchUsersFromFirebase()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterUsers(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) {
            originalUserList
        } else {
            originalUserList.filter {
                "${it.name} ${it.job} ${it.location}".lowercase().contains(q)
            }
        }
        adapter.updateUsers(filtered)
    }

    private fun fetchUsersFromFirebase() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalUserList.clear()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    user?.let { originalUserList.add(it) }
                }
                adapter.updateUsers(originalUserList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserListActivity, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}
