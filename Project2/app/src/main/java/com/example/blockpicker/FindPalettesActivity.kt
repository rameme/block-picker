package com.example.blockpicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class FindPalettesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_palettes_activity)

        // log it
        Log.d("Find Palettes Activity", "onCreate called!")
    }
}