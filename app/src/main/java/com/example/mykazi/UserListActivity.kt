package com.example.mykazi

import android.content.Intent
import android.os.Bundle
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
    private var loggedInPhone = ""
    private var blockedSet: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        val prefs = getSharedPreferences("MyKaziPrefs", MODE_PRIVATE)
        loggedInPhone = prefs.getString("loggedInPhone", "") ?: ""

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = UserAdapter(originalUserList, blockedSet) { user ->
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("phone", user.phone)
            intent.putExtra("name", user.name)
            intent.putExtra("job", user.job)
            intent.putExtra("location", user.location)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        db = FirebaseDatabase.getInstance().getReference("users")

        fetchBlockedUsers()
        fetchUsersFromFirebase()
        setupSearchView()
    }

    private fun fetchBlockedUsers() {
        db.root.child("blocked").child(loggedInPhone)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blockedSet = snapshot.children.mapNotNull { it.key }.toSet()
                    adapter.updateUsers(originalUserList, blockedSet)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserListActivity, error.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun fetchUsersFromFirebase() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalUserList.clear()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    user?.let { originalUserList.add(it) }
                }
                adapter.updateUsers(originalUserList, blockedSet)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserListActivity, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                val q = newText?.trim()?.lowercase() ?: ""
                val filtered = if (q.isEmpty()) originalUserList
                else originalUserList.filter {
                    "${it.name} ${it.job} ${it.location}".lowercase().contains(q)
                }
                adapter.updateUsers(filtered, blockedSet)
                return true
            }
        })
    }
}
