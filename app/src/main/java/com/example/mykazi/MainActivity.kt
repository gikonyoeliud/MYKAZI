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

    private lateinit var buttonlogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect UI elements
        buttonlogin=findViewById(R.id.buttonlogin)
        textViewTitle = findViewById(R.id.personaldetails)
        editTextName = findViewById(R.id.editText1)
        editTextJob = findViewById(R.id.editText2)
        editTextPhoneNumber = findViewById(R.id.editText3)
        editTextLocation = findViewById(R.id.editText4)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        buttonlogin.setOnClickListener {

            val intent= Intent (this, LoginActivity::class.java)
                    startActivity(intent)
        }

        buttonSubmit.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val job = editTextJob.text.toString().trim()
            val phone = editTextPhoneNumber.text.toString().trim()
            val location = editTextLocation.text.toString().trim()

            if (name.isEmpty() || job.isEmpty() || phone.isEmpty() || location.isEmpty()) {
                textViewTitle.text = "Please fill all details"
                return@setOnClickListener
            }

            val user = User(name, job, phone, location)

            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users")

            usersRef.child(phone).setValue(user)
                .addOnSuccessListener {
                    textViewTitle.text = "Uploaded successfully!"
                }
                .addOnFailureListener {
                    textViewTitle.text = "Failed to upload"
                }
        }
    }
}
