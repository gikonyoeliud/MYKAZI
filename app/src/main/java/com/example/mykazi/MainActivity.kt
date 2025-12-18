package com.example.mykazi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextJob: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextName = findViewById(R.id.editText1)
        editTextJob = findViewById(R.id.editText2)
        editTextPhoneNumber = findViewById(R.id.editText3)
        editTextLocation = findViewById(R.id.editText4)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonLogin = findViewById(R.id.buttonlogin)

        // Real-time validation
        setupTextWatchers()

        buttonLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        buttonSubmit.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val job = editTextJob.text.toString().trim()
            val phoneInput = editTextPhoneNumber.text.toString().trim()
            val location = editTextLocation.text.toString().trim()

            // Basic empty validation
            if (name.isEmpty() || job.isEmpty() || phoneInput.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Name and job length validation
            if (name.length < 2 || name.length > 50) {
                Toast.makeText(this, "Enter a valid name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (job.length < 2 || job.length > 50) {
                Toast.makeText(this, "Enter a valid job title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Phone validation
            if (!isValidPhone(phoneInput)) {
                Toast.makeText(this, "Enter a valid Kenyan phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val phone = normalizePhone(phoneInput)
            checkIfUserExists(phone, name, job, location)
        }
    }

    private fun setupTextWatchers() {
        editTextPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phone = s.toString()
                if (!isValidPhone(phone)) editTextPhoneNumber.error = "Invalid phone number"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        editTextName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = s.toString()
                if (name.length < 2 || name.length > 50) editTextName.error = "Enter a valid name"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        editTextJob.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val job = s.toString()
                if (job.length < 2 || job.length > 50) editTextJob.error = "Enter a valid job title"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkIfUserExists(phone: String, name: String, job: String, location: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(phone)

        usersRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "You are already registered. Please log in.", Toast.LENGTH_SHORT).show()
            } else {
                saveUserToFirebase(phone, name, job, location)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserToFirebase(phone: String, name: String, job: String, location: String) {
        val user = User(name, job, phone, location)
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        usersRef.child(phone).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Registered successfully! Please log in.", Toast.LENGTH_SHORT).show()
                clearFields()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        editTextName.text.clear()
        editTextJob.text.clear()
        editTextPhoneNumber.text.clear()
        editTextLocation.text.clear()
    }

    private fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace(" ", "").replace("-", "")
        return cleaned.matches(Regex("0\\d{9}")) || cleaned.matches(Regex("\\+254\\d{9}"))
    }

    private fun normalizePhone(phone: String): String {
        var p = phone.replace(" ", "").replace("-", "")
        if (p.startsWith("0")) p = "+254" + p.drop(1)
        return p
    }
}
