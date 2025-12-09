package com.example.mykazi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
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
        adapter = UserAdapter()
        recyclerView.adapter = adapter

        db = FirebaseDatabase.getInstance().reference.child("users")

        setupSearchView()
        fetchUsersFromFirebase()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterUsers(query: String) {
        val q = query.trim().lowercase()

        if (q.isEmpty()) {
            val sorted = originalUserList.sortedBy { it.name.lowercase() }
            adapter.updateUsers(sorted)
            return
        }

        // Split into words: "bot nakuru" → ["bot", "nakuru"]
        val queryWords = q.split("\\s+".toRegex()).filter { it.isNotEmpty() }

        val filtered = originalUserList.filter { user ->
            val nameLower = user.name.lowercase()
            val jobLower = user.job.lowercase()
            val locationLower = user.location.lowercase()

            // Match if ANY word appears in name, job, or location
            queryWords.any { word ->
                nameLower.contains(word) ||
                        jobLower.contains(word) ||
                        locationLower.contains(word)
            }
        }.sortedBy { it.name.lowercase() }

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
                // Re-apply current search/filter
                filterUsers(searchView.query.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }
}