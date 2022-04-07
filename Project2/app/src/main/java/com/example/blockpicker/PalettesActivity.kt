package com.example.blockpicker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PalettesActivity: AppCompatActivity() {

    // init variables
    private lateinit var recyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.palettes_activity)

        // log it
        Log.d("PalettesActivity", "onCreate called!")

        // set title
        title = resources.getText(R.string.palettes_activity_title);

        /* palettes recyclerView */
        recyclerView = findViewById(R.id.ResultView)

        val palettes = getFakePalettes()
        val adapter = PalettesAdapter(palettes)
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    /* Navigation Menu */
    // create an action bar button
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // go to profile activity
            R.id.ProfileMenu -> {
                Log.d("PalettesActivity", "Switch to ProfileActivity!")
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

            // go to create palettes activity
            R.id.CreateMenu -> {
                Log.d("PalettesActivity", "Switch to CreatePalettesActivity!")
                val intent = Intent(this, CreatePalettesActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}