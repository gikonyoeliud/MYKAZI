package com.example.mykazi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextName = findViewById(R.id.idName)
        editTextPhone = findViewById(R.id.phoneNumber)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegister = findViewById(R.id.buttonRegister)

        buttonLogin.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val phone = normalizePhone(editTextPhone.text.toString().trim())

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Enter both name and phone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(name, phone)
        }

        buttonRegister.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun loginUser(name: String, phone: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(phone)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val dbName = snapshot.child("name").getValue(String::class.java)
                val isBlocked = snapshot.child("blocked").getValue(Boolean::class.java) ?: false

                if (isBlocked) {
                    Toast.makeText(this, "Your account is blocked. Contact admin.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                if (dbName == name) {
                    // Save logged-in phone to SharedPreferences
                    val prefs = getSharedPreferences("MyKaziPrefs", MODE_PRIVATE)
                    prefs.edit().putString("loggedInPhone", phone).apply()

                    startActivity(Intent(this, UserListActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Name does not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Normalizes user input:
     * - Removes spaces and dashes
     * - Converts 0xxxx numbers to +254xxxx
     * - Leaves +254xxxx numbers unchanged
     */
    private fun normalizePhone(phone: String): String {
        var p = phone.replace(" ", "").replace("-", "")
        if (p.startsWith("0")) p = "+254" + p.drop(1)
        return p
    }
}
