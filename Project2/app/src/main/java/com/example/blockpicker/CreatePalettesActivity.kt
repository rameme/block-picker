package com.example.blockpicker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import android.widget.TextView
import android.widget.ArrayAdapter
import java.util.*
import android.widget.EditText
import android.widget.AdapterView.OnItemClickListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.collections.ArrayList

class CreatePalettesActivity : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase

    // Init variables
    private lateinit var createBlock : ArrayList<ImageView>
    private lateinit var currentBlock : ImageView
    private lateinit var paletteList : ArrayList<TextView>
    private lateinit var paletteName : EditText
    private lateinit var createButton : Button

    // Store palette information
    private lateinit var blockName : Array<String>
    private var currentBlockIndex = 0
    private var currentPaletteSize = 0;

    private lateinit var block : TextView
    private lateinit var blockList : ArrayList<String>
    private lateinit var dialog : Dialog

    // TODO: progress bar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_palettes_activity)

        // Log it
        Log.d("CreatePalettesActivity", "onCreate called!")

        // Set title
        title = resources.getText(R.string.create_palettes);

        // SharedPreferences
        val sharedPrefs: SharedPreferences = getSharedPreferences("block-picker", Context.MODE_PRIVATE)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Set value in array list
        blockList = ArrayList<String>()
        blockList = arrayListOf<String>(*resources.getStringArray(R.array.blocks))

        // Text View
        block = findViewById(R.id.SearchBlocks)

        /* Image View */
        createBlock = ArrayList<ImageView>(6)
        blockName = arrayOf("","","","","","")

        var imageViewId = arrayOf(R.id.CreateBlock1,R.id.CreateBlock2,R.id.CreateBlock3,R.id.CreateBlock4,R.id.CreateBlock5,R.id.CreateBlock6);

        // Get ImageView by ID and set OnClickListeners
        for(i in imageViewId.indices){
            var createBlockView : ImageView = findViewById(imageViewId[i])

            // OnClickListeners for the image views
            createBlockView.setOnClickListener() {
                currentBlock = createBlockView
                currentBlockIndex = i
                selectBlock()
            }
            createBlock.add(createBlockView)
        }

        /* TextView */
        paletteList = ArrayList<TextView>(6)
        var paletteBlockId = arrayOf(R.id.PaletteBlock1,R.id.PaletteBlock2,R.id.PaletteBlock3,R.id.PaletteBlock4,R.id.PaletteBlock5,R.id.PaletteBlock6);

        // Get ImageView by and TODO: set on click listeners to highlight block
        for(i in paletteBlockId.indices){
            var createPaletteText : TextView = findViewById(paletteBlockId[i])
            paletteList.add(createPaletteText)
        }

        // PaletteName input
        paletteName = findViewById(R.id.NamePalette)

        // Create palette
        createButton = findViewById(R.id.PaletteCreate)
        createButton.setOnClickListener(){
            firebaseAnalytics.logEvent("create_button_clicked", null)

            val UID: String = FirebaseAuth.getInstance().currentUser!!.uid!!

            // Get paletteName
            var inputtedPaletteName = paletteName.text.toString().trim()
            if (inputtedPaletteName.isBlank()){
                inputtedPaletteName = paletteName.hint.toString().trim()
            }

            // Get author and minecraft UUID from sharedPrefs
            val author = sharedPrefs.getString("USERNAME", "")
            val minecraftUUID = sharedPrefs.getString("UUID", "")

            // Create palette object
            val palette = Palettes(
                name = inputtedPaletteName,
                author = author!!,
                authorUID = UID,
                minecraftUUID = minecraftUUID!!,
                likes = 0,
                block1 = blockName[0].trim(),
                block2 = blockName[1].trim(),
                block3 = blockName[2].trim(),
                block4 = blockName[3].trim(),
                block5 = blockName[4].trim(),
                block6 = blockName[5].trim()
            )

            // Store palette on Firebase
            val referencePalettes = firebaseDatabase.getReference("palettes")
            referencePalettes.push().setValue(palette)

            // Go to PalettesActivity
            val intent = Intent(createButton.context, PalettesActivity::class.java)
            startActivity(intent)

            // Prevent user from backing into the palettes screen
            finish()

        }

        // Disable the create button
        createButton.isEnabled = false
    }

    private fun selectBlock (){
        // Initialize dialog
        val builder = AlertDialog.Builder(this@CreatePalettesActivity)
        builder.setView(R.layout.searchable_spinner)

        // Show dialog to search for blocks
        dialog = builder.create()
        dialog.show()

        val searchBlock : EditText = dialog.findViewById(R.id.SearchBlock)
        val listBlocks : ListView = dialog.findViewById(R.id.ListBlocks)

        val adapter: ArrayAdapter<*> = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, blockList)

        listBlocks.adapter = adapter

        // Filter blocks by search item
        searchBlock.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filter.filter(p0);
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // Click on the desired block name to set the image bitmap
        listBlocks.onItemClickListener = OnItemClickListener { parent, view, position, id -> // when item selected from list
            // Set selected item on textView
            var blockSelected = parent.getItemAtPosition(position) as String

            // Increase palette size if the imageView was empty
            if(blockName[currentBlockIndex].isEmpty()){
                currentPaletteSize += 1
            }

            // Set the block name
            blockName[currentBlockIndex] = "$blockSelected\n"

            // If first block is changed change palette hint
            if(currentBlockIndex == 0){
                paletteName.hint = "$blockSelected Palette"
            }

            // Change text to current block
            paletteList[currentBlockIndex].text = blockName[currentBlockIndex]

            // Get the drawable ID from block name
            var blockId = blockSelected.lowercase().replace(" ","_")
            val resId = resources.getIdentifier(
                blockId, "drawable",
                packageName
            )

            // Load image
            Picasso
                .get()
                .load(resId)
                .into(currentBlock)

            // Dismiss dialog
            dialog.dismiss()

            // If 6 images are set, then enable the save button
            if(currentPaletteSize == 6){
                createButton.isEnabled = true
            }
        }
    }

    /* Close Create Palettes Menu */
    // Create an action bar button
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.close, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // go to palettes activity
        when (item.itemId) {
            R.id.CloseMenu -> {
                Log.d("CreatePalettesActivity", "Switch to PalettesActivity!")
                val intent = Intent(this, PalettesActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}