package com.example.blockpicker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

// Database listener
lateinit var paletteListener: ValueEventListener

class PalettesActivity: AppCompatActivity() {

    // Init variables
    private lateinit var recyclerView : RecyclerView

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.palettes_activity)

        // Log it
        Log.d("PalettesActivity", "onCreate called!")

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()

        /* palettes recyclerView */
        recyclerView = findViewById(R.id.PaletteView)

        progressBar = findViewById(R.id.progressBarPalette)
        progressBar.visibility = View.GONE

        // get data
        getPalettesFromFirebase()
    }

    /* getPalettesFromFirebase */
    // Get palettes data from FirebaseDB
    private fun getPalettesFromFirebase(){

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Get data from palettes tables
        val referencePalettes = firebaseDatabase.getReference("palettes").orderByChild("likes")
        paletteListener = referencePalettes.addValueEventListener(object : ValueEventListener {

            // Could not find palettes information, show error and log it
            override fun onCancelled(error: DatabaseError) {
                firebaseAnalytics.logEvent("firebasedb_cancelled", null)
                Toast.makeText(
                    this@PalettesActivity,
                    R.string.failed_to_retrieve_palettes,
                    Toast.LENGTH_LONG
                ).show()

                progressBar.visibility = View.GONE

                Log.e("PalettesActivity", "DB connection issue", error.toException())
                Firebase.crashlytics.recordException(error.toException())
            }

            // Found palettes data, show it in the recyclerView
            override fun onDataChange(snapshot: DataSnapshot) {
                firebaseAnalytics.logEvent("firebasedb_data_change", null)

                val palettes = mutableListOf<Palettes>()
                snapshot.children.forEach { childSnapshot: DataSnapshot ->
                    try {
                        val UID = firebaseAuth.currentUser!!.uid

                        // Store palette information
                        val palette = childSnapshot.getValue(Palettes::class.java)
                        if (palette != null) {
                            // check firebase for likes
                            val keys = childSnapshot.child("saved").toString()
                            if(UID in keys){
                                palette.liked = true
                            }
                            palettes.add(0, palette)
                        }

                    } catch (exception: Exception) {
                        Log.e("PalettesActivity", "Failed to read palettes", exception)
                        Firebase.crashlytics.recordException(exception)
                    }
                }

                val adapter = PalettesAdapter(palettes)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@PalettesActivity)

                progressBar.visibility = View.GONE
            }
        })

    }

    /* Navigation Menu */
    // Create an action bar button
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_person);
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            // Go to profile activity
            android.R.id.home -> {
                Log.d("PalettesActivity", "Switch to ProfileActivity!")
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

            // Go to create palettes activity
            R.id.CreateMenu -> {
                Log.d("PalettesActivity", "Switch to CreatePalettesActivity!")
                val intent = Intent(this, CreatePalettesActivity::class.java)
                startActivity(intent)
            }

            // Search, Go to FindPalettesActivity
            R.id.SearchMenu -> {
                Log.d("PalettesActivity", "Switch to FindPalettesActivity!")
                val intent = Intent(this, FindPalettesActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}