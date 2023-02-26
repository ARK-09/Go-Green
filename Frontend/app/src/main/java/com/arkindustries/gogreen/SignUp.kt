package com.arkindustries.gogreen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class SignUp : AppCompatActivity() {
    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        supportActionBar?.hide()

        backBtn = findViewById(R.id.back_btn)

        backBtn.setOnClickListener{
            finish()
        }
    }
}