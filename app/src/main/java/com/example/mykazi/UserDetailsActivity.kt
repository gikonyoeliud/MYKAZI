package com.example.mykazi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        //val name =intent.getIntExtra("name")
        val name = intent.getStringExtra("name")
        val job = intent.getStringExtra("job")
        val phone = intent.getStringExtra("phone")
        val location = intent.getStringExtra("location")
        val whatsappBtn: Button = findViewById(R.id.whatsappBtn)
        val cleanedPhone = phone?.replace("+", "")?.replace(" ", "") ?: ""

        val nameTxt: TextView = findViewById(R.id.detailName)

        val jobTxt: TextView = findViewById(R.id.detailJob)
        val phoneTxt: TextView = findViewById(R.id.detailPhone)
        val locationTxt: TextView = findViewById(R.id.detailLocation)
        val callBtn: Button = findViewById(R.id.callBtn)
        val smsBtn: Button = findViewById(R.id.smsBtn)


        nameTxt.text=name
        jobTxt.text = job
        phoneTxt.text = phone
        locationTxt.text = location



        // CALL
        callBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        }

        // SMS
        smsBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
            intent.putExtra("sms_body", "Hello, I saw your service on MyKazi")
            startActivity(intent)
        }
        whatsappBtn.setOnClickListener {
            try {
                val url = "https://wa.me/$cleanedPhone"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
