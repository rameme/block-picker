package com.example.blockpicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import java.lang.Exception

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileAvatar : ImageView
    private lateinit var usernameText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        // log it
        Log.d("ProfileActivity", "onCreate called!")

        // set title
        title = resources.getText(R.string.profile);

        // username
        var username = "alphabugs"
        profileAvatar = findViewById(R.id.AvatarProfile)
        getAvatar(username)

        usernameText = findViewById(R.id.UsernameProfile)
        usernameText.text = username

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
                    // no need make a second API
                    Picasso
                        .get()
                        .load("https://crafatar.com/avatars/$avatar")
                        .into(profileAvatar)

                    Log.e("ProfileActivity", "Set Avatar")
                } else {
                    Picasso
                        .get()
                        .load(R.drawable.avatar)
                        .into(profileAvatar)
                }
            }
        }
    }
}