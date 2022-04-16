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
import android.widget.*
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
import org.jetbrains.anko.find

class FindPalettesActivity : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase

    // UI elements
    private lateinit var resultText : TextView
    private lateinit var recyclerView : RecyclerView


    // Dialog
    private lateinit var blockList : ArrayList<String>
    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_palettes_activity)

        // log it
        Log.d("Find Palettes Activity", "onCreate called!")

        // Set title
        // title = resources.getText(R.string.find_title)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Set value in array list
        blockList = ArrayList<String>()
        blockList = arrayListOf<String>(*resources.getStringArray(R.array.blocks))

        /* recyclerView */
        recyclerView = findViewById(R.id.ResultView)

        resultText = findViewById(R.id.ResultText)

        // search block
        searchBlock()

        resultText.text = getString(R.string.find_result_general)
    }

    /* Show block menu and search for palettes by block name */
    private fun searchBlock () {
        // Initialize dialog
        val builder = AlertDialog.Builder(this@FindPalettesActivity)
        builder.setView(R.layout.searchable_spinner)

        // Show dialog to search for blocks
        dialog = builder.create()
        dialog.show()

        // Set dialog title
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
            val referencePalettes = firebaseDatabase.getReference("palettes").orderByChild("blocks/$blockSelected").equalTo(true)
            referencePalettes.addListenerForSingleValueEvent(object : ValueEventListener {

                // Could not palettes information, show error and log it
                override fun onCancelled(error: DatabaseError) {
                    firebaseAnalytics.logEvent("firebasedb_cancelled", null)
                    Toast.makeText(
                        this@FindPalettesActivity,
                        R.string.failed_to_retrieve_palettes,
                        Toast.LENGTH_LONG
                    ).show()

                    resultText.text = getString(R.string.find_result_fail, blockSelected)

                    Log.e("FindPalettesActivity", "DB connection issue", error.toException())
                    Firebase.crashlytics.recordException(error.toException())
                }

                // Found palettes data, show it in the recyclerView
                override fun onDataChange(snapshot: DataSnapshot) {
                    firebaseAnalytics.logEvent("firebasedb_data_change", null)

                    resultText.text = getString(R.string.find_result_block, blockSelected)

                    // Get the data and display it
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
                                palettes.add(0, palette)
                            }
                        } catch (exception: Exception) {
                            Log.e("PalettesActivity", "Failed to read palettes", exception)
                            Firebase.crashlytics.recordException(exception)
                        }
                    }

                    if(palettes.isNotEmpty()){
                        resultText.text = getString(R.string.find_result_block, blockSelected)
                    } else {
                        resultText.text = getString(R.string.find_result_fail, blockSelected)
                    }

                    val adapter = PalettesAdapter(palettes)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(this@FindPalettesActivity)
                }
            })

            // Dismiss dialog
            dialog.dismiss()
        }
    }

    /* Close Create Palettes Menu */
    // Create an action bar button
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // go to palettes activity
        when (item.itemId) {
            R.id.SearchButton -> {
                // search block
                searchBlock()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}