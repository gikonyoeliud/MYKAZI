package com.example.mykazi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var callBtn: Button
    private lateinit var smsBtn: Button
    private lateinit var whatsappBtn: Button
    private lateinit var messageInput: EditText
    private lateinit var sendBtn: Button
    private lateinit var chatRecycler: RecyclerView

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    private lateinit var database: DatabaseReference
    private var myPhone: String = "anonymous"
    private var otherPhone: String = ""
    private var myUid: String = ""
    private var isBlocked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        // ------------------------
        // Profile Title
        // ------------------------
        val profileTitle: TextView = findViewById(R.id.profileTitle)
        profileTitle.text = "PROFILE"
        profileTitle.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        profileTitle.textSize = 22f
        profileTitle.setTypeface(profileTitle.typeface, android.graphics.Typeface.BOLD)

        // ------------------------
        // UI Elements
        // ------------------------
        val nameTxt: TextView = findViewById(R.id.detailName)
        val jobTxt: TextView = findViewById(R.id.detailJob)
        val locationTxt: TextView = findViewById(R.id.detailLocation)
        val phoneTxt: TextView = findViewById(R.id.detailPhone)

        callBtn = findViewById(R.id.callBtn)
        smsBtn = findViewById(R.id.smsBtn)
        whatsappBtn = findViewById(R.id.whatsappBtn)
        messageInput = findViewById(R.id.messageInput)
        sendBtn = findViewById(R.id.sendBtn)
        chatRecycler = findViewById(R.id.chatRecycler)

        chatRecycler.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(messages)
        chatRecycler.adapter = chatAdapter

        // ------------------------
        // Intent Data
        // ------------------------
        val name = intent.getStringExtra("name") ?: "N/A"
        val job = intent.getStringExtra("job") ?: "N/A"
        val phone = intent.getStringExtra("phone") ?: ""
        val location = intent.getStringExtra("location") ?: "N/A"

        nameTxt.text = name
        jobTxt.text = job
        locationTxt.text = location
        phoneTxt.text = phone

        if (phone.isBlank()) {
            disableChat(callBtn, smsBtn, whatsappBtn)
            Toast.makeText(this, "User has no phone number", Toast.LENGTH_LONG).show()
            return
        }

        // ------------------------
        // Firebase Current User
        // ------------------------
        val user = FirebaseAuth.getInstance().currentUser
        myUid = user?.uid ?: ""
        myPhone = user?.phoneNumber?.let { normalizePhone(it) } ?: "anonymous"
        otherPhone = normalizePhone(phone)

        // ------------------------
        // Check Block Status
        // ------------------------
        checkIfBlockedAndSetupChat()

        // ------------------------
        // Button Actions
        // ------------------------
        callBtn.setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }

        smsBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
            intent.putExtra("sms_body", "Hello, I saw your service on MyKazi")
            startActivity(intent)
        }

        whatsappBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$otherPhone"))
            try { startActivity(intent) }
            catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }

        // ------------------------
        // Chat Setup
        // ------------------------
        val chatId = if (myPhone < otherPhone) "$myPhone-$otherPhone" else "$otherPhone-$myPhone"
        database = FirebaseDatabase.getInstance().getReference("chats").child(chatId)

        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    messages.add(it)
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    chatRecycler.scrollToPosition(messages.size - 1)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserDetailsActivity, error.message, Toast.LENGTH_LONG).show()
            }
        })

        sendBtn.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isNotEmpty() && !isBlocked) {
                val message = Message(text = text, senderId = myPhone, timestamp = System.currentTimeMillis())
                database.push().setValue(message)
                messageInput.text.clear()
            }
        }
    }

    // ------------------------
    // Menu
    // ------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_details_menu, menu)
        updateMenuItems(menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        updateMenuItems(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_edit_details -> { showEditDetailsDialog(); true }
            R.id.action_block_user -> { confirmBlockUser(); true }
            R.id.action_unblock_user -> { unblockUser(); true }
            R.id.action_sign_out -> { signOut(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateMenuItems(menu: Menu?) {
        menu?.findItem(R.id.action_block_user)?.isVisible = !isBlocked
        menu?.findItem(R.id.action_unblock_user)?.isVisible = isBlocked
    }

    // ------------------------
    // Block / Unblock
    // ------------------------
    private fun checkIfBlockedAndSetupChat() {
        if (myUid.isEmpty()) return
        FirebaseDatabase.getInstance()
            .getReference("blocked")
            .child(myUid)
            .child(otherPhone)
            .get()
            .addOnSuccessListener { snapshot ->
                isBlocked = snapshot.exists() && snapshot.value == true
                if (isBlocked) disableChat(callBtn, smsBtn, whatsappBtn)
                invalidateOptionsMenu()
            }
    }

    private fun confirmBlockUser() {
        AlertDialog.Builder(this)
            .setTitle("Block User")
            .setMessage("You will no longer receive messages from this user.")
            .setPositiveButton("Block") { _, _ -> blockUser() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun blockUser() {
        if (myUid.isEmpty()) return
        FirebaseDatabase.getInstance()
            .getReference("blocked")
            .child(myUid)
            .child(otherPhone)
            .setValue(true)
            .addOnSuccessListener {
                isBlocked = true
                disableChat(callBtn, smsBtn, whatsappBtn)
                Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show()
                invalidateOptionsMenu()
            }
    }

    private fun unblockUser() {
        if (myUid.isEmpty()) return
        FirebaseDatabase.getInstance()
            .getReference("blocked")
            .child(myUid)
            .child(otherPhone)
            .removeValue()
            .addOnSuccessListener {
                isBlocked = false
                sendBtn.isEnabled = true
                messageInput.isEnabled = true
                callBtn.isEnabled = true
                smsBtn.isEnabled = true
                whatsappBtn.isEnabled = true
                Toast.makeText(this, "User unblocked", Toast.LENGTH_SHORT).show()
                invalidateOptionsMenu()
            }
    }

    // ------------------------
    // Edit Details
    // ------------------------
    private fun showEditDetailsDialog() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        if (otherPhone != normalizePhone(user.phoneNumber ?: "")) {
            Toast.makeText(this, "You can only edit your own details", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null)
        val editJob: EditText = dialogView.findViewById(R.id.editJob)
        val editPhone: EditText = dialogView.findViewById(R.id.editPhone)
        val editLocation: EditText = dialogView.findViewById(R.id.editLocation)

        editJob.setText(findViewById<TextView>(R.id.detailJob).text)
        editPhone.setText(findViewById<TextView>(R.id.detailPhone).text)
        editLocation.setText(findViewById<TextView>(R.id.detailLocation).text)

        AlertDialog.Builder(this)
            .setTitle("Edit Details")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newJob = editJob.text.toString().trim()
                val newPhone = editPhone.text.toString().trim()
                val newLocation = editLocation.text.toString().trim()
                updateUserDetails(newJob, newPhone, newLocation)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserDetails(job: String, phone: String, location: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        val updates = mapOf(
            "job" to job,
            "phone" to phone,
            "location" to location
        )

        userRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show()
            findViewById<TextView>(R.id.detailJob).text = job
            findViewById<TextView>(R.id.detailPhone).text = phone
            findViewById<TextView>(R.id.detailLocation).text = location
            otherPhone = normalizePhone(phone)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ------------------------
    // Sign Out
    // ------------------------
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, StartActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
    }

    // ------------------------
    // Helper Functions
    // ------------------------
    private fun normalizePhone(phone: String): String {
        return phone.replace("+","").replace(" ","").replace("-","").takeLast(9)
    }

    private fun disableChat(vararg buttons: Button) {
        buttons.forEach { it.isEnabled = false }
        sendBtn.isEnabled = false
        messageInput.isEnabled = false
    }
}
