package com.example.mykazi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvJob: TextView
    private lateinit var tvLocation: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendBtn: Button
    private lateinit var chatRecycler: RecyclerView
    private lateinit var callBtn: Button
    private lateinit var smsBtn: Button
    private lateinit var whatsappBtn: Button

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var db: DatabaseReference

    private var loggedInPhone = ""
    private var selectedPhone = ""
    private var isBlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        // UI references
        tvName = findViewById(R.id.tvName)
        tvJob = findViewById(R.id.tvJob)
        tvLocation = findViewById(R.id.tvLocation)
        messageInput = findViewById(R.id.messageInput)
        sendBtn = findViewById(R.id.sendBtn)
        chatRecycler = findViewById(R.id.chatRecycler)
        callBtn = findViewById(R.id.callBtn)
        smsBtn = findViewById(R.id.smsBtn)
        whatsappBtn = findViewById(R.id.whatsappBtn)

        chatRecycler.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(messages, "")
        chatRecycler.adapter = chatAdapter

        val prefs = getSharedPreferences("MyKaziPrefs", MODE_PRIVATE)
        loggedInPhone = prefs.getString("loggedInPhone", "") ?: ""
        selectedPhone = intent.getStringExtra("phone") ?: ""

        db = FirebaseDatabase.getInstance().reference

        loadUserProfile()
        setupChat()
        setupButtons()
    }

    // ---------------- Load user profile ----------------
    private fun loadUserProfile() {
        db.child("users").child(selectedPhone).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                    val job = snapshot.child("job").getValue(String::class.java) ?: "N/A"
                    val location = snapshot.child("location").getValue(String::class.java) ?: "N/A"

                    tvName.text = name
                    tvJob.text = job
                    tvLocation.text = location
                }
            }
    }

    // ---------------- Chat ----------------
    private fun setupChat() {
        val chatId =
            if (loggedInPhone < selectedPhone) "$loggedInPhone-$selectedPhone" else "$selectedPhone-$loggedInPhone"

        db.child("chats").child(chatId).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Message::class.java)?.let { msg ->
                    messages.add(msg)
                    chatAdapter = ChatAdapter(messages, loggedInPhone)
                    chatRecycler.adapter = chatAdapter
                    chatRecycler.scrollToPosition(messages.size - 1)

                    // In-app notification
                    if (msg.senderPhone != loggedInPhone) {
                        Toast.makeText(
                            this@UserDetailsActivity,
                            "New message from ${tvName.text}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        })

        sendBtn.setOnClickListener {
            if (isBlocked) {
                Toast.makeText(this, "You blocked this user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val text = messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(chatId, text)
                messageInput.text.clear()
            }
        }
    }

    private fun sendMessage(chatId: String, text: String) {
        val message = Message(text, loggedInPhone, System.currentTimeMillis())
        db.child("chats").child(chatId).push().setValue(message)

        // Trigger push notification via Cloud Function
        // Cloud Function automatically sends notification to receiver
    }

    // ---------------- Buttons ----------------
    private fun setupButtons() {
        callBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$selectedPhone")))
        }
        smsBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$selectedPhone")))
        }
        whatsappBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$selectedPhone")))
        }
    }
}
