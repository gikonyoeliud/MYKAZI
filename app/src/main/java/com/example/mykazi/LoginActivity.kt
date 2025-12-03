package com.example.mykazi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextidName: EditText
    private lateinit var editTextphoneNumber: EditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonEnter: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        editTextidName=findViewById<EditText>(R.id.idName)
        editTextphoneNumber=findViewById<EditText>(R.id.phoneNumber)
        buttonRegister=findViewById<Button>(R.id.buttonRegister)
        buttonEnter=findViewById<Button>(R.id.buttonLogin)

        buttonEnter.setOnClickListener {
            val intenta = Intent (this, UserListActivity::class.java)
            startActivity(intenta)
        }

        buttonRegister.setOnClickListener {

            val intentb = Intent (this, MainActivity::class.java)
            startActivity(intentb)
        }

    }
}