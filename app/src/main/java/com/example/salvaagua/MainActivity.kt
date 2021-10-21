package com.example.salvaagua

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    lateinit var startupPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startupPreferences = getSharedPreferences("startup", MODE_PRIVATE)
        if(!startupPreferences.getBoolean("register", false)) {
            startActivity(Intent(this@MainActivity, SetupActivity::class.java))
            finish()
        }
    }
}