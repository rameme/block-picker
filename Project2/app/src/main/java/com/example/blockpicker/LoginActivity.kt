package com.example.blockpicker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText

class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton : Button
    private lateinit var signupButton : Button
    private lateinit var profileButton : Button
    private lateinit var createPaletteButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        // log it
        Log.d("LoginActivity", "onCreate called!")

        // set title
        title = resources.getText(R.string.login_title);

        /* PalettesActivity */
        loginButton = findViewById(R.id.Login)
        loginButton.setOnClickListener(){ view ->
            val intent = Intent(this, PalettesActivity::class.java)
            startActivity(intent)
        }

        signupButton = findViewById(R.id.Signup)
        signupButton.setOnClickListener(){ view ->
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        profileButton = findViewById(R.id.Profile)
        profileButton.setOnClickListener(){ view ->
            Log.d("LoginActivity", "Switch to ProfileActivity!")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        createPaletteButton = findViewById(R.id.Create)
        createPaletteButton.setOnClickListener(){ view ->
            Log.d("LoginActivity", "Switch to CreatePalettesActivity!")
            val intent = Intent(this, CreatePalettesActivity::class.java)
            startActivity(intent)
        }
    }
}