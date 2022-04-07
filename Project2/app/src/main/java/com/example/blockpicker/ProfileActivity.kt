package com.example.blockpicker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import java.lang.Exception


class ProfileActivity : AppCompatActivity() {

    private lateinit var profileAvatar : ImageView
    private lateinit var usernameText : TextView

    private lateinit var createRecyclerView : RecyclerView
    private lateinit var savedRecyclerView : RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        // log it
        Log.d("ProfileActivity", "onCreate called!")

        // set title
        title = resources.getText(R.string.profile);

        // username, TODO: get username from FIREBASE
        var username = "alphabugs"
        profileAvatar = findViewById(R.id.AvatarProfile)
        getAvatar(username)

        usernameText = findViewById(R.id.UsernameProfile)
        usernameText.text = username

        // logout user
        usernameText.setOnClickListener(){
            Log.d("ProfileActivity", "Switch to LoginActivity!")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        /* palettes created recyclerView */
        createRecyclerView = findViewById(R.id.CreateView)

        val createPalettes = getFakePalettes()
        val createAdapter = PalettesAdapter(createPalettes)
        createRecyclerView.adapter = createAdapter

        createRecyclerView.layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)

        /* palettes saved recyclerView */
        savedRecyclerView = findViewById(R.id.SaveView)

        val savedPalettes = getFakePalettes()
        val savedAdapter = PalettesAdapter(savedPalettes)
        savedRecyclerView.adapter = savedAdapter

        savedRecyclerView.layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)

    }

    private fun getAvatar (username: String) {
        val profileManager = ProfileManager()

        Log.e("ProfileActivity", "Retrieving Avatar")

        // Networking on a background thread
        doAsync {
            val avatar : String = try {
                profileManager.retrieveUUID(username)
            } catch (exception: Exception) {
                Log.e("ProfileActivity", "Retrieving UUID failed", exception)
            }.toString()

            runOnUiThread {
                if (avatar.isNotEmpty()){
                    // get image from URL (no need to make API call)
                    Picasso
                        .get()
                        .load("https://crafatar.com/avatars/$avatar")
                        .into(profileAvatar)

                    Log.e("ProfileActivity", "Set Avatar")
                } else {
                    // set default profile
                    Picasso
                        .get()
                        .load(R.drawable.creeper)
                        .into(profileAvatar)
                }
            }
        }
    }

    /* Close Create Palettes Menu */
    // create an action bar button
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.close, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // go to palettes activity
        when (item.itemId) {
            R.id.CloseMenu -> {
                Log.d("ProfileActivity", "Switch to PalettesActivity!")
                val intent = Intent(this, PalettesActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}