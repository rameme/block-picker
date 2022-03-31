package com.example.blockpicker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // log it
        Log.d("MainActivity", "onCreate called!")

        /* PalettesActivity */
        val intent = Intent(this, CreatePalettesActivity::class.java)
        startActivity(intent)
    }
}