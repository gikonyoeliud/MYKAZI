package com.example.mykazi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextIdName: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize views
        editTextIdName = findViewById(R.id.idName)
        editTextPhoneNumber = findViewById(R.id.phoneNumber)
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonLogin = findViewById(R.id.buttonLogin)

        // Login button click
        buttonLogin.setOnClickListener {
            val name = editTextIdName.text.toString().trim()
            val phone = editTextPhoneNumber.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter both name and phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(name, phone)
        }

        // Register button click
        buttonRegister.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(name: String, phone: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(phone)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val dbName = snapshot.child("name").getValue(String::class.java)
                if (dbName != null && dbName == name) {
                    // Login successful, go to UserListActivity
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, UserListActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Name does not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
