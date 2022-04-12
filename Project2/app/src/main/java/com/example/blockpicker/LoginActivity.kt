package com.example.blockpicker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase

    // UI elements
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton : Button
    private lateinit var signupButton : Button

    // TODO: progress bar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        // Log it
        Log.d("LoginActivity", "onCreate called!")

        // SharedPreferences
        val sharedPrefs: SharedPreferences = getSharedPreferences("block-picker", Context.MODE_PRIVATE)

        // Set title
        title = resources.getText(R.string.login_activity_title)

        // Get UI elements
        email = findViewById(R.id.EmailLogin)
        password = findViewById(R.id.PasswordLogin)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()

        /* Login User */
        loginButton = findViewById(R.id.LoginButton)
        loginButton.isEnabled = false

        // Login the user using FirebaseAuth and Store additional information to sharedPreferences
        loginButton.setOnClickListener(){ view ->

            val inputtedEmail: String = email.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()

            // Login using FirebaseAuth
            firebaseAuth
                .signInWithEmailAndPassword(inputtedEmail,inputtedPassword)
                .addOnCompleteListener { task ->

                    // Successfully logged into account
                    if (task.isSuccessful) {

                        // Show toast
                        val user = firebaseAuth.currentUser
                        Toast.makeText(
                            this,
                            getString(R.string.login_success, user!!.email),
                            Toast.LENGTH_LONG
                        ).show()

                        // Get additional account information from firebase DB
                        firebaseAnalytics.logEvent("get_additional_account_info", null)

                        // Get Firebase UID to find account
                        val UID: String = FirebaseAuth.getInstance().currentUser!!.uid!!
                        val referenceAccounts = firebaseDatabase.getReference("accounts").child(UID)

                        // Search for Account
                        referenceAccounts.addValueEventListener(object : ValueEventListener {
                            // Could not find account, show error and log it
                            override fun onCancelled(error: DatabaseError) {
                                firebaseAnalytics.logEvent("firebasedb_cancelled", null)
                                Toast.makeText(
                                    this@LoginActivity,
                                    R.string.failed_to_retrieve_account,
                                    Toast.LENGTH_LONG
                                ).show()

                                Log.e("LoginActivity", "DB connection issue", error.toException())
                                Firebase.crashlytics.recordException(error.toException())
                            }

                            // Account found, store into in SharedPreferences
                            override fun onDataChange(snapshot: DataSnapshot) {
                                firebaseAnalytics.logEvent("firebasedb_data_change", null)

                                // Get account info
                                var account: Accounts? = null
                                snapshot.children.forEach { childSnapshot: DataSnapshot ->
                                    account = childSnapshot.getValue(Accounts::class.java)
                                }

                                // Save account information to sharedPreferences
                                sharedPrefs
                                    .edit()
                                    .putString("USERNAME", account!!.username)
                                    .apply()

                                sharedPrefs
                                    .edit()
                                    .putString("UUID", account!!.minecraft_UUID)
                                    .apply()

                                // Save the email to SharedPreferences
                                sharedPrefs
                                    .edit()
                                    .putString("EMAIL", inputtedEmail)
                                    .apply()

                                // Go to PalettesActivity
                                val intent = Intent(email.context, PalettesActivity::class.java)
                                startActivity(intent)

                                // Prevent users backing into this activity
                                finish()
                            }
                        })
                    }
                    // Could not login, show error and log it
                    else {
                        val exception = task.exception

                        if (exception != null) {
                            Firebase.crashlytics.recordException(exception)
                        }

                        // Show errors
                        when (exception) {
                            // Account does not exists
                            is FirebaseAuthInvalidUserException -> {
                                // Log the error to firebaseAnalytics
                                val bundle = Bundle()
                                bundle.putString("reason", "no_registered_account")
                                firebaseAnalytics.logEvent("login_failed", bundle)

                                email.error = getString(R.string.login_no_user)
                            }
                            // Invalid email format
                            is FirebaseAuthInvalidCredentialsException -> {
                                // Log the error to firebaseAnalytics
                                val bundle = Bundle()
                                bundle.putString("reason", "invalid_credentials")
                                firebaseAnalytics.logEvent("login_failed", bundle)

                                email.error = getString(R.string.login_failure_wrong_credentials)
                                password.error = getString(R.string.login_failure_wrong_credentials)
                            }
                            // Generic error: show toast
                            else -> {
                                // Log the error to firebaseAnalytics
                                val bundle = Bundle()
                                bundle.putString("reason", "generic")
                                firebaseAnalytics.logEvent("login_failed", bundle)
                                Toast.makeText(
                                    this,
                                    getString(R.string.login_failure_generic, exception),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }

        // Go to signup screen
        signupButton = findViewById(R.id.Signup)
        signupButton.setOnClickListener(){ view ->
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Call textWatcher
        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        // Restore saved username
        val savedEmail = sharedPrefs.getString("EMAIL", "")
        email.setText(savedEmail)
    }

    /* TextWatcher */
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        // Enable login button when all the required fields are completed
        override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            val inputtedEmail: String = email.text.toString()
            val inputtedPassword: String = password.text.toString()

            // enable signup button
            val enableButton: Boolean = inputtedEmail.isNotBlank() && inputtedPassword.isNotBlank()
            loginButton.isEnabled = enableButton
        }

        override fun afterTextChanged(p0: Editable?) {}
    }
}