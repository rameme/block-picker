package com.example.blockpicker

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import android.widget.Toast
import com.google.firebase.database.*

// Database listener
var findPaletteListener: ValueEventListener? = null

class FindPalettesActivity : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase

    // UI elements
    private lateinit var resultText : TextView
    private lateinit var searchBlockText : EditText
    private lateinit var recyclerView : RecyclerView

    // Dialog
    private lateinit var blockList : ArrayList<String>
    private lateinit var dialog : Dialog

    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_palettes_activity)

        // log it
        Log.d("Find Palettes Activity", "onCreate called!")

        // Set title
        title = resources.getText(R.string.find_title)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Get block and put it in ArrayList
        blockList = ArrayList()
        blockList = arrayListOf(*resources.getStringArray(R.array.blocks))

        /* recyclerView */
        recyclerView = findViewById(R.id.ResultView)

        resultText = findViewById(R.id.ResultText)
        searchBlockText = findViewById(R.id.SearchBlockFind)
        searchBlockText.isFocusable = false

        // search block
        searchBlockText.setOnClickListener(){
            searchBlock()
        }

        resultText.text = getString(R.string.find_result_general)

        // Progress bar
        progressBar = findViewById(R.id.progressBarFind)
        progressBar.visibility = View.GONE
    }

    /* Show block menu and search for palettes by block name */
    private fun searchBlock () {

        // Searchable Dialog: https://www.geeksforgeeks.org/how-to-implement-custom-searchable-spinner-in-android/
        // Initialize dialog
        val builder = AlertDialog.Builder(this@FindPalettesActivity)
        builder.setView(R.layout.searchable_spinner)

        // Show dialog to search for blocks
        dialog = builder.create()
        dialog.show()

        // Set dialog
        var dialogTitle: TextView = dialog.findViewById(R.id.SearchBlockTitle)
        dialogTitle.text = getString(R.string.select_block_to_search)

        val searchBlock: EditText = dialog.findViewById(R.id.SearchBlock)
        val listBlocks: ListView = dialog.findViewById(R.id.ListBlocks)

        val adapter: ArrayAdapter<*> = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, blockList
        )

        listBlocks.adapter = adapter

        // Filter blocks by search item
        searchBlock.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filter.filter(p0);
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // Click on the desired block name and search for it
        listBlocks.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // Block selected
            var blockSelected = parent.getItemAtPosition(position) as String

            // Search by block
            if (blockSelected.isNotBlank()){
                searchFirebase(blockSelected)
            } else {
                searchBlockText.setText(getString(R.string.find_result))
            }

            // Dismiss dialog
            dialog.dismiss()
        }
    }

    /* Search firebase */
    private fun searchFirebase(blockSelected : String){

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Search Firebase
        val referencePalettes : Query = firebaseDatabase.getReference("palettes").orderByChild("blocks/$blockSelected").equalTo(true)
        findPaletteListener = referencePalettes.addValueEventListener(object : ValueEventListener {

            // Could not palettes information, show error and log it
            override fun onCancelled(error: DatabaseError) {
                firebaseAnalytics.logEvent("firebasedb_cancelled", null)
                Toast.makeText(
                    this@FindPalettesActivity,
                    R.string.failed_to_retrieve_palettes,
                    Toast.LENGTH_LONG
                ).show()

                resultText.text = getString(R.string.find_result_fail, blockSelected)
                searchBlockText.setText(getString(R.string.find_result))

                // Hide progress bar
                progressBar.visibility = View.GONE

                Log.e("FindPalettesActivity", "DB connection issue", error.toException())
                Firebase.crashlytics.recordException(error.toException())
            }

            // Found palettes data, show it in the recyclerView
            override fun onDataChange(snapshot: DataSnapshot) {
                firebaseAnalytics.logEvent("firebasedb_data_change", null)

                searchBlockText.setText(blockSelected)
                resultText.text = getString(R.string.find_result_block, blockSelected)

                // Get the data and display it
                val palettes = mutableListOf<Palettes>()
                snapshot.children.forEach { childSnapshot: DataSnapshot ->
                    try {
                        val UID = firebaseAuth.currentUser!!.uid

                        val palette = childSnapshot.getValue(Palettes::class.java)

                        if (palette != null && palette.blocks!!.containsKey(blockSelected)) {
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

                // Set text
                if(palettes.isNotEmpty()){
                    resultText.text = getString(R.string.find_result_block, blockSelected)
                } else {
                    resultText.text = getString(R.string.find_result_fail, blockSelected)
                }

                val adapter = PalettesAdapter(palettes)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@FindPalettesActivity)

                // Hide progress bar
                progressBar.visibility = View.GONE
            }
        })
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