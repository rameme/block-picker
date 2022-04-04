package com.example.blockpicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        // log it
        Log.d("Sign Up Activity", "onCreate called!")

        // set title
        title = resources.getText(R.string.signup_title);
    }
}