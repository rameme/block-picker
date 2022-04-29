package com.example.blockpicker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileAvatar : ImageView
    private lateinit var usernameText : TextView
    private lateinit var createRecyclerView : RecyclerView
    private lateinit var savedRecyclerView : RecyclerView

    private lateinit var createPalettesText : TextView
    private lateinit var savePalettesText : TextView

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var progressBar : ProgressBar

    lateinit var savePaletteListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        // Log it
        Log.d("ProfileActivity", "onCreate called!")

        // Set title
        title = resources.getText(R.string.profile);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Progress bar
        progressBar = findViewById(R.id.progressBarProfile)
        progressBar.visibility = View.GONE

        // SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("block-picker", Context.MODE_PRIVATE)

        // Get author and minecraft UUID from sharedPrefs
        val username = sharedPreferences.getString("USERNAME", "")
        val minecraftUUID = sharedPreferences.getString("UUID", "")

        // Set profile avatar
        profileAvatar = findViewById(R.id.AvatarProfile)
        setAvatar(minecraftUUID!!)

        // Set profile name
        usernameText = findViewById(R.id.UsernameProfile)
        usernameText.text = username

        // Logout user
        usernameText.setOnClickListener(){
            Log.d("ProfileActivity", "Logout User")

            // Show progress bar
            progressBar.visibility = View.VISIBLE

            // Log it
            firebaseAnalytics.logEvent("logout_user", null)

            // Remove listeners
            firebaseDatabase
                .getReference("palettes")
                .removeEventListener(paletteListener)

            firebaseDatabase
                .getReference("palettes")
                .removeEventListener(savePaletteListener)

            if (findPaletteListener != null){
                firebaseDatabase
                    .getReference("palettes")
                    .removeEventListener(findPaletteListener!!)
            }

            firebaseAuth.signOut()

            // clear sharedPreferences
            sharedPreferences.edit().remove("USERNAME").apply();
            sharedPreferences.edit().remove("UUID").apply();

            // Hide progress bar
            progressBar.visibility = View.GONE

            // Go to login screen, clear backstack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        createPalettesText = findViewById(R.id.CreatePalettes)
        savePalettesText = findViewById(R.id.SavePalettes)

        /* palettes created recyclerView */
        createRecyclerView = findViewById(R.id.CreateView)
        getCreatedPalettesFromFirebase()

        /* palettes saved recyclerView */
        savedRecyclerView = findViewById(R.id.SaveView)
        getSavedPalettesFromFirebase()
    }

    /* getCreatedPalettesFromFirebase */
    // Get palettes data from FirebaseDB
    private fun getCreatedPalettesFromFirebase(){

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Log it
        firebaseAnalytics.logEvent("get_created_palettes", null)

        // Get data from palettes tables
        val UID: String = FirebaseAuth.getInstance().currentUser!!.uid!!

        val reference = firebaseDatabase.getReference("palettes").orderByChild("authorUID").equalTo(UID)
        reference.addListenerForSingleValueEvent( object : ValueEventListener {

            // Found palettes data, show it in the recyclerView
            override fun onDataChange(snapshot: DataSnapshot) {
                firebaseAnalytics.logEvent("firebasedb_data_change", null)

                // Get palette data and show it
                val palettes = mutableListOf<Palettes>()
                snapshot.children.forEach { childSnapshot: DataSnapshot ->
                    try {
                        val UID = firebaseAuth.currentUser!!.uid

                        val palette = childSnapshot.getValue(Palettes::class.java)

                        if (palette != null) {
                            // check firebase for likes
                            val keys = childSnapshot.child("saved").toString()
                            if(UID in keys){
                                palette.liked = true
                            }
                            palettes.add(palette)
                        }
                    } catch (exception: Exception) {
                        Log.e("ProfileActivity", "Failed to read palettes", exception)
                        Firebase.crashlytics.recordException(exception)
                    }
                }

                if(palettes.isEmpty()){
                    createPalettesText.visibility = View.GONE
                } else {
                    createPalettesText.visibility = View.VISIBLE
                }

                val adapter = PalettesAdapter(palettes)
                createRecyclerView.adapter = adapter
                createRecyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false)

                // Hide progress bar
                progressBar.visibility = View.GONE
            }

            // Could not palettes information, show error and log it
            override fun onCancelled(error: DatabaseError) {
                firebaseAnalytics.logEvent("firebasedb_cancelled", null)
                Toast.makeText(
                    this@ProfileActivity,
                    R.string.failed_to_retrieve_palettes,
                    Toast.LENGTH_LONG
                ).show()

                createPalettesText.visibility = View.GONE

                // Hide progress bar
                progressBar.visibility = View.GONE

                Log.e("ProfileActivity", "DB connection issue", error.toException())
                Firebase.crashlytics.recordException(error.toException())
            }

        })
    }

    /* getCreatedPalettesFromFirebase */
    // Get palettes data from FirebaseDB
    private fun getSavedPalettesFromFirebase(){

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Log it
        firebaseAnalytics.logEvent("get_saved_palettes", null)

        // Get data from palettes tables
        val UID: String = FirebaseAuth.getInstance().currentUser!!.uid!!

        val reference = firebaseDatabase.getReference("palettes").orderByChild("saved/$UID").equalTo(true)
        savePaletteListener = reference.addValueEventListener(object : ValueEventListener {

            // Found palettes data, show it in the recyclerView
            override fun onDataChange(snapshot: DataSnapshot) {
                firebaseAnalytics.logEvent("firebasedb_data_change", null)

                // Get palette data and show it
                val palettes = mutableListOf<Palettes>()
                snapshot.children.forEach { childSnapshot: DataSnapshot ->
                    try {
                        val UID = firebaseAuth.currentUser!!.uid

                        val palette = childSnapshot.getValue(Palettes::class.java)

                        if (palette != null) {
                            // check firebase for likes
                            val keys = childSnapshot.child("saved").toString()
                            if(UID in keys){
                                palette.liked = true
                            }
                            palettes.add(palette)
                        }
                    } catch (exception: Exception) {
                        Log.e("ProfileActivity", "Failed to read palettes", exception)
                        Firebase.crashlytics.recordException(exception)
                    }
                }

                // Show text
                if(palettes.isEmpty()){
                    savePalettesText.visibility = View.GONE
                } else {
                    savePalettesText.visibility = View.VISIBLE
                }

                val adapter = PalettesAdapter(palettes)
                savedRecyclerView.adapter = adapter
                savedRecyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false)

                // Hide progress bar
                progressBar.visibility = View.GONE
            }

            // Could not palettes information, show error and log it
            override fun onCancelled(error: DatabaseError) {
                firebaseAnalytics.logEvent("firebasedb_cancelled", null)
                Toast.makeText(
                    this@ProfileActivity,
                    R.string.failed_to_retrieve_palettes,
                    Toast.LENGTH_LONG
                ).show()

                savePalettesText.visibility = View.GONE

                // Hide progress bar
                progressBar.visibility = View.GONE

                Log.e("ProfileActivity", "DB connection issue", error.toException())
                Firebase.crashlytics.recordException(error.toException())
            }
        })
    }

    /* setAvatar */
    // Use minecraftUUID to get the player avatar
    private fun setAvatar (UUID: String) {
        if (UUID.isNotEmpty()){
            // Get image from URL (no need to make API call)
            Picasso
                .get()
                .load("https://crafatar.com/avatars/$UUID")
                .into(profileAvatar)

            Log.e("ProfileActivity", "Set Avatar")
        } else {
            // Set default profile
            Picasso
                .get()
                .load(R.drawable.avatar)
                .into(profileAvatar)
        }
    }

    /* Close CreatePalettesScreen */
    // Create an action bar button
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.close, menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // go to palettes activity
        when (item.itemId) {
            R.id.CloseMenu -> {
                // search block
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}