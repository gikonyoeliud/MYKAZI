package com.example.mykazi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var textViewTitle: TextView
    private lateinit var editTextName: EditText
    private lateinit var editTextJob: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect UI elements
        textViewTitle = findViewById(R.id.personaldetails)
        editTextName = findViewById(R.id.editText1)
        editTextJob = findViewById(R.id.editText2)
        editTextPhoneNumber = findViewById(R.id.editText3)
        editTextLocation = findViewById(R.id.editText4)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonLogin = findViewById(R.id.buttonlogin)

        // Navigate to LoginActivity
        buttonLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Submit user details to Firebase
        buttonSubmit.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val job = editTextJob.text.toString().trim()
            val phone = editTextPhoneNumber.text.toString().trim()
            val location = editTextLocation.text.toString().trim()

            if (name.isEmpty() || job.isEmpty() || phone.isEmpty() || location.isEmpty()) {
                textViewTitle.text = "Please fill all details"
                return@setOnClickListener
            }

            checkIfUserExists(phone, name, job, location)
        }
    }

    private fun checkIfUserExists(phone: String, name: String, job: String, location: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(phone)

        usersRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // User already registered
                textViewTitle.text = "You are already registered. Please log in."
            } else {
                // User not registered, save to database
                saveUserToFirebase(phone, name, job, location)
            }
        }.addOnFailureListener { e ->
            textViewTitle.text = "Database error: ${e.message}"
        }
    }

    private fun saveUserToFirebase(phone: String, name: String, job: String, location: String) {
        val user = User(name, job, phone, location)
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        usersRef.child(phone).setValue(user)
            .addOnSuccessListener {
                textViewTitle.text = "Registered successfully! You can now log in."
                clearFields()
            }
            .addOnFailureListener {
                textViewTitle.text = "Failed to register. Please try again."
            }
    }

    private fun clearFields() {
        editTextName.text.clear()
        editTextJob.text.clear()
        editTextPhoneNumber.text.clear()
        editTextLocation.text.clear()
    }
}
