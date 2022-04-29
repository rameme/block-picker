package com.example.blockpicker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.jetbrains.anko.doAsync
import java.lang.Exception

class SignupActivity : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStore: FirebaseStorage

    // UI elements
    private lateinit var email: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var verifyPassword: EditText
    private lateinit var minecraftUsername: EditText
    private lateinit var signUpButton: Button
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        // Log it
        Log.d("Sign Up Activity", "onCreate called!")

        // Set title
        title = resources.getText(R.string.signup_activity_title);

        // Get UI elements
        email = findViewById(R.id.EmailSignup)
        username = findViewById(R.id.UsernameSignup)
        password = findViewById(R.id.PasswordSignup)
        verifyPassword = findViewById(R.id.PasswordVerifySignup)
        minecraftUsername = findViewById(R.id.McUsernameSignup)
        signUpButton = findViewById(R.id.SignupButton)
        progressBar = findViewById(R.id.progressBarSignup)

        // Disable signup button by default
        signUpButton.isEnabled = false

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStore = FirebaseStorage.getInstance()

        // Create account
        signUpButton.setOnClickListener {
            // show progress bar when creating account
            progressBar.visibility = View.VISIBLE
            createAccount()
        }

        // Text watchers
        email.addTextChangedListener(textWatcher)
        username.addTextChangedListener(textWatcher)
        verifyPassword.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
        minecraftUsername.addTextChangedListener(textWatcher)

        // Hide progress bar
        progressBar.visibility = View.GONE
    }

    /* createAccount */
    // Use the MojangAPI to get minecraft UUID from minecraft username
    // Then, create a account using Firebase auth
    // Lastly, store additional information (minecraft UUID, Username) to FirebaseRealTime database
    private fun createAccount(){

        // Log it
        firebaseAnalytics.logEvent("signup_clicked", null)

        // Get minecraft username
        val inputtedMinecraftUsername: String = minecraftUsername.text.toString().trim()

        // Get profile manager
        val profileManager = SignupManager()
        Log.e("SignupActivity", "Retrieving UUID")

        // Networking on a background thread
        doAsync {
            // Use the Mojang API to retrieve minecraft UUID
            var UUID: String = ""
            try {
                UUID = profileManager.retrieveUUID(inputtedMinecraftUsername)
            } catch (exception: Exception) {
                // Could not find username, log it
                Log.e("SignupActivity", "Retrieving UUID failed", exception)

                // hide progress bar
                progressBar.visibility = View.GONE
            }.toString()

            runOnUiThread {

                // If a valid UUID is returned, create an account
                if(UUID.isNotBlank()){

                    val inputtedEmail: String = email.text.toString().trim()
                    val inputtedPassword: String = password.text.toString().trim()
                    val inputtedUsername: String = username.text.toString().trim()
                    val context = minecraftUsername.context

                    // Create account with firebaseAuth
                    firebaseAuth
                        .createUserWithEmailAndPassword(inputtedEmail,inputtedPassword)
                        .addOnCompleteListener { task ->

                            // Successfully created account, now store additional data on firebase DB
                            if (task.isSuccessful) {
                                val user = firebaseAuth.currentUser

                                // Store username, minecraft username, and minecraft UUID to FirebaseRealTime Database
                                val reference = firebaseDatabase.getReference("accounts")
                                val UID = user!!.uid

                                // Update data to DB
                                val account = Accounts(inputtedUsername, inputtedMinecraftUsername, UUID)
                                reference.child(UID).push().setValue(account).addOnCompleteListener{
                                    if(it.isSuccessful){
                                        // Successfully added accounts
                                        Toast.makeText(context, R.string.signup_success, Toast.LENGTH_LONG).show()

                                        // SharedPreferences
                                        val sharedPrefs: SharedPreferences = getSharedPreferences("block-picker", Context.MODE_PRIVATE)

                                        // Save the username, UUID, and EMAIL to SharedPreferences
                                        sharedPrefs
                                            .edit()
                                            .putString("USERNAME", inputtedUsername)
                                            .apply()

                                        sharedPrefs
                                            .edit()
                                            .putString("UUID", UUID)
                                            .apply()

                                        sharedPrefs
                                            .edit()
                                            .putString("EMAIL", inputtedEmail)
                                            .apply()

                                        // Go to PalettesActivity
                                        val intent = Intent(context, PalettesActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)

                                        // Prevent user from backing into signup screen
                                        finish()
                                    }
                                    // Signout the user
                                    else {
                                        firebaseAuth.signOut()
                                        Toast.makeText(this@SignupActivity, R.string.signup_failure_db_update, Toast.LENGTH_LONG).show()

                                        // Failed to add new user to database, log it
                                        val bundle = Bundle()
                                        bundle.putString("reason", "failed_to_add_$UUID")
                                        firebaseAnalytics.logEvent("failed_to_add_user_to_database", bundle)
                                    }
                                }
                            }
                            // Could not create account, show error
                            else {
                                val exception = task.exception

                                // Log the error to crashlytics
                                if (exception != null) {
                                    Firebase.crashlytics.recordException(exception)
                                }

                                // Show error
                                when (exception) {
                                    // Account already exists
                                    is FirebaseAuthUserCollisionException -> {
                                        // log the error to firebaseAnalytics
                                        val bundle = Bundle()
                                        bundle.putString("reason", "existing_account")
                                        firebaseAnalytics.logEvent("signup_failed", bundle)

                                        email.error = getString(R.string.signup_failure_already_exists)
                                    }
                                    // Invalid email format
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        // log the error to firebaseAnalytics
                                        val bundle = Bundle()
                                        bundle.putString("reason", "invalid_credentials")
                                        firebaseAnalytics.logEvent("signup_failed", bundle)

                                        email.error = getString(R.string.signup_failure_invalid_format)
                                    }
                                    // Generic error: show toast
                                    else -> {
                                        // Log the error to firebaseAnalytics
                                        val bundle = Bundle()
                                        bundle.putString("reason", "generic")
                                        firebaseAnalytics.logEvent("signup_failed", bundle)

                                        // Display error
                                        Toast.makeText(
                                            context,
                                            getString(R.string.signup_failure_generic, exception),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                }
                // No minecraft account found, show error
                else {
                    // hide progress bar
                    progressBar.visibility = View.GONE

                    // Log the error to firebaseAnalytics
                    val bundle = Bundle()
                    bundle.putString("reason", "invalid-uuid")
                    firebaseAnalytics.logEvent("signup_failed", bundle)
                    minecraftUsername.error = getString(R.string.signup_failure_invalid_mc_username)
                }

            }
        }
    }

    /* textWatcher */
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        // Enable signup button when all the required fields are completed
        override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            val inputtedEmail: String = email.text.toString()
            val inputtedPassword: String = password.text.toString()
            val inputtedVerifyPassword : String = verifyPassword.text.toString()
            val inputtedMinecraftUsername: String = minecraftUsername.text.toString()
            val inputtedUsername: String = username.text.toString()

            // Check password size, show error if less than 6
            if (inputtedPassword.isNotEmpty() && inputtedPassword.length < 6){
                password.error = getString(R.string.signup_failure_weak_password)
            }

            // Check username size
            if (inputtedUsername.isNotEmpty() && inputtedUsername.length < 5){
                username.error = getString(R.string.signup_failure_weak_username)
            }

            // Check password verification, show error if they don't match
            var verifiedPassword = false
            if (inputtedVerifyPassword.isNotEmpty()){
                if (inputtedVerifyPassword != inputtedPassword) {
                    verifyPassword.error = getString(R.string.password_verify)
                } else {
                    verifiedPassword = true
                }
            }

            // Enable signup button when all the fields are completed
            val enableButton: Boolean = inputtedEmail.isNotBlank() && inputtedPassword.isNotBlank() && verifiedPassword && inputtedPassword.length >= 6 && inputtedMinecraftUsername.isNotBlank() &&  inputtedUsername.isNotBlank() && inputtedUsername.length >= 5
            signUpButton.isEnabled = enableButton
        }

        override fun afterTextChanged(p0: Editable?) {}
    }
}