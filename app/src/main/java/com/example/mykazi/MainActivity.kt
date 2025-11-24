package com.yourname.myapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.mykazi.R

class MainActivity : AppCompatActivity() {

    private lateinit var textViewTitle: TextView
    private lateinit var editTextName: EditText
    private lateinit var editTextJob: EditText
    private lateinit var buttonSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect UI elements
        textViewTitle = findViewById(R.id.personaldetails)
        editTextName = findViewById(R.id.editText1)
        editTextJob = findViewById(R.id.editText2)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        // Button click listener
        buttonSubmit.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val job = editTextJob.text.toString().trim()

            // Optional: validate input
            if (name.isEmpty() || job.isEmpty()) {
                textViewTitle.text = "Please fill in both fields"
            } else {
                textViewTitle.text = "Name: $name\nJob: $job"
            }
        }
    }
}
