package com.example.blockpicker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
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
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class CreatePalettesActivity : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStore: FirebaseStorage

    // UI elements
    private lateinit var createBlock : ArrayList<ImageView>
    private lateinit var paletteList : ArrayList<TextView>
    private lateinit var currentBlock : ImageView
    private lateinit var currentBlockIcon : ImageView
    private lateinit var currentCardIcon : CardView
    private lateinit var paletteName : EditText
    private lateinit var createButton : Button
    private lateinit var progressBar : ProgressBar

    // Store palette/block information
    private lateinit var blockName : Array<String>
    private var currentBlockIndex = 0
    private var currentPaletteSize = 0;

    // Dialog
    private lateinit var block : TextView
    private lateinit var blockList : ArrayList<String>
    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_palettes_activity)

        // Log it
        Log.d("CreatePalettesActivity", "onCreate called!")

        // Set title
        title = resources.getText(R.string.create_palettes)

        // SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("block-picker", Context.MODE_PRIVATE)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStore = FirebaseStorage.getInstance()

        // Get block and put it in ArrayList
        blockList = arrayListOf(*resources.getStringArray(R.array.blocks))

        // Text View
        block = findViewById(R.id.SearchBlocks)

        /* Image View */
        createBlock = ArrayList(6)
        blockName = arrayOf("","","","","","")

        // IDs
        var imageViewId = arrayOf(R.id.Block1,R.id.Block2,R.id.Block3,R.id.Block4,R.id.Block5,R.id.Block6);
        var imageIconId = arrayOf(R.id.BlockIcon1,R.id.BlockIcon2,R.id.BlockIcon3,R.id.BlockIcon4,R.id.BlockIcon5,R.id.BlockIcon6)
        var cardIconId = arrayOf(R.id.CardIcon1,R.id.CardIcon2,R.id.CardIcon3,R.id.CardIcon4,R.id.CardIcon5,R.id.CardIcon6)

        // Get ImageView by ID and set OnClickListeners
        for(i in imageViewId.indices){
            var createBlockView : ImageView = findViewById(imageViewId[i])
            var createBlockIcon : ImageView = findViewById(imageIconId[i])
            var cardBlockIcon : CardView = findViewById(cardIconId[i])
            cardBlockIcon.visibility = View.INVISIBLE

            // OnClickListeners for the image views
            createBlockView.setOnClickListener() {
                currentBlock = createBlockView
                currentBlockIcon = createBlockIcon
                currentCardIcon = cardBlockIcon
                currentBlockIndex = i
                selectBlock()
            }
            createBlock.add(createBlockView)
        }

        /* TextView */
        // Set up TextView
        paletteList = ArrayList(6)
        var paletteBlockId = arrayOf(R.id.PaletteBlock1,R.id.PaletteBlock2,R.id.PaletteBlock3,R.id.PaletteBlock4,R.id.PaletteBlock5,R.id.PaletteBlock6);

        // Get TextView
        for(i in paletteBlockId.indices){
            var createPaletteText : TextView = findViewById(paletteBlockId[i])
            paletteList.add(createPaletteText)
        }

        // Progress bar
        progressBar = findViewById(R.id.progressBarCreate)

        // PaletteName input
        paletteName = findViewById(R.id.NamePalette)

        /* Create Palette */
        // Click on create button to upload palette
        createButton = findViewById(R.id.PaletteCreate)
        createButton.setOnClickListener(){

            // Show progress bar and disable button (to avoid multiple submission)
            progressBar.visibility = View.VISIBLE
            createButton.isEnabled = false

            // Log it
            firebaseAnalytics.logEvent("create_button_clicked", null)

            // Get UID from firebaseAuth
            val UID: String = FirebaseAuth.getInstance().currentUser!!.uid!!

            /* Get paletteName */
            // Use hint name if no palette name is set
            var inputtedPaletteName = paletteName.text.toString().trim()
            if (inputtedPaletteName.isBlank()){
                inputtedPaletteName = paletteName.hint.toString().trim()
            }

            // Get author and minecraft UUID from sharedPreferences
            val author = sharedPreferences.getString("USERNAME", "")
            val minecraftUUID = sharedPreferences.getString("UUID", "")

            // Get Firebase DB reference and key
            val referencePalettes = firebaseDatabase.getReference("palettes")
            val key = referencePalettes.push().key;

            /* Combine bitmap and upload to firebase */
            // Combine bitmap to create a palette bitmap of 6 images
            val combined = paletteBitmap()

            // Image ref and storage location
            val storageReference = FirebaseStorage.getInstance().reference
            val paletteRef: StorageReference = storageReference.child("palette/$key.jpg")

            // Get the data image bitmap as bytes
            val imageBAOS = ByteArrayOutputStream()
            combined!!.compress(Bitmap.CompressFormat.PNG, 100, imageBAOS)
            val data: ByteArray = imageBAOS.toByteArray()

            /* Firebase Storage */
            // Upload to palette bitmap Firebase storage
            val uploadData = paletteRef.putBytes(data)
            uploadData.addOnSuccessListener { task ->

                // Get the Url of the block palette
                task.metadata!!.reference!!.downloadUrl.addOnCompleteListener { uri ->

                    // Successfully uploaded image to Firebase Storage
                    if(uri.isSuccessful){

                        /* Upload palette information to Firebase DB */
                        // Get palette image url
                        val paletteUrl = uri.result!!.toString()

                        // Create blocks Hashmap, allows for easier searching
                        val blocks = HashMap<String, Boolean>()
                        for (block in blockName) {
                            blocks[block] = true
                        }

                        // Create palette object
                        val palette = Palettes(
                            paletteID = key!!,
                            name = inputtedPaletteName,
                            author = author!!,
                            authorUID = UID,
                            minecraftUUID = minecraftUUID!!,
                            likes = 1,
                            liked = false,
                            paletteUrl = paletteUrl,
                            block1 = blockName[0],
                            block2 = blockName[1],
                            block3 = blockName[2],
                            block4 = blockName[3],
                            block5 = blockName[4],
                            block6 = blockName[5],
                            blocks = blocks,
                        )

                        // Store palette data on Firebase
                        referencePalettes.child(key).setValue(palette).addOnCompleteListener {
                            // Success
                            if(it.isSuccessful){
                                // Successfully uploaded palette, show Toast
                                Toast.makeText(this,
                                    R.string.palette_success,
                                    Toast.LENGTH_LONG
                                ).show()

                                // Go to PalettesActivity
                                val intent = Intent(createButton.context, PalettesActivity::class.java)
                                startActivity(intent)

                                // Prevent user from backing into the palettes screen
                                finish()
                            }
                            // Error
                            else {
                                val exception = it.exception

                                // Log the error to crashlytics
                                if (exception != null) {
                                    Firebase.crashlytics.recordException(exception)
                                }

                                // Log the error to firebaseAnalytics
                                val bundle = Bundle()
                                bundle.putString("reason", "generic")
                                firebaseAnalytics.logEvent("palette_create_failed", bundle)

                                // Show error
                                Toast.makeText(
                                    this,
                                    R.string.palette_failure,
                                    Toast.LENGTH_LONG
                                ).show()

                                //Hide progress bar and Enable button on error
                                progressBar.visibility = View.GONE
                                createButton.isEnabled = true
                            }
                        }
                    }
                }
            }
            // Failed to upload image
            uploadData.addOnCanceledListener {
                // Log the error to firebaseAnalytics
                val bundle = Bundle()
                bundle.putString("reason", "generic")
                firebaseAnalytics.logEvent("palette_image_upload_failed", bundle)

                // Show error
                Toast.makeText(
                    this,
                    R.string.palette_failure,
                    Toast.LENGTH_LONG
                ).show()

                // Hide progress bar and Enable button on error
                progressBar.visibility = View.GONE
                createButton.isEnabled = true
            }
        }

        // Hide progress bar and disable the create button
        progressBar.visibility = View.GONE
        createButton.isEnabled = false
    }

    /* Show block select menu (dialog), update palette on screen, and store palette information */
    private fun selectBlock (){

        // Searchable Dialog: https://www.geeksforgeeks.org/how-to-implement-custom-searchable-spinner-in-android/
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
        listBlocks.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            // Block selected
            var blockSelected = parent.getItemAtPosition(position) as String

            // Increase palette size if the imageView was previously empty
            if(blockName[currentBlockIndex].isEmpty()){
                currentPaletteSize += 1
            }

            // Set the block name
            blockName[currentBlockIndex] = "$blockSelected"

            // If the first block has changed, update palette hint
            if(currentBlockIndex == 0){
                paletteName.hint = "$blockSelected Palette"
            }

            // Change text to new block
            paletteList[currentBlockIndex].text = blockName[currentBlockIndex]

            // Get the drawable ID from block name
            var blockId = blockSelected.lowercase().replace(" ","_")
            val resId = resources.getIdentifier(
                blockId, "drawable",
                packageName
            )

            // Load icon
            currentCardIcon.visibility = View.VISIBLE
            Picasso
                .get()
                .load(resId)
                .into(currentBlockIcon)

            // Load block image
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

    /* Combine 6 Bitmaps to create block palette*/
    private fun paletteBitmap() : Bitmap?{
        var left : Bitmap?
        var right: Bitmap?

        /* Combine top part of the palette */
        // Combine 0 + 1 -> top -> top + 2
        left = createBlock[0].drawable.toBitmap()
        right = createBlock[1].drawable.toBitmap()
        var topLeftHalf = combineHorizontal(left,right)

        right = createBlock[2].drawable.toBitmap()
        val top = combineHorizontal(topLeftHalf!!,right)

        /* Combine bottom part of the palette */
        // combine 3 + 4 -> bottom -> bottom + 5
        left = createBlock[3].drawable.toBitmap()
        right = createBlock[4].drawable.toBitmap()
        var bottomLeftHalf = combineHorizontal(left,right)

        right = createBlock[5].drawable.toBitmap()
        val bottom = combineHorizontal(bottomLeftHalf!!,right)

        return combineVertical(top!!, bottom!!)
    }

    /* Combine two Android Bitmaps top and bottom. */
    private fun combineVertical(top : Bitmap, bottom : Bitmap): Bitmap? {
        // Get the size of the images combined side by side.
        val width: Int = top.width
        val height: Int = top.height + bottom.height

        // Create a Bitmap large enough to hold both input images and a canvas to draw to this combined bitmap.
        val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        // Render both input images into the combined bitmap and return it.
        canvas.drawBitmap(top, Matrix(),  null)
        canvas.drawBitmap(bottom, 0f, bottom.height.toFloat(), null)

        return combined
    }

    /* Combine two Android Bitmaps side by side. */
    /* Credits: https://gist.github.com/miky-kr5/d4a14246f25adbc71637 */
    private fun combineHorizontal(left : Bitmap, right : Bitmap): Bitmap? {

        // Get the size of the images combined side by side.
        val width: Int = left.width + right.width
        val height: Int = if (left.height > right.height) { left.height } else { right.height }

        // Create a Bitmap large enough to hold both input images and a canvas to draw to this combined bitmap.
        val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        // Render both input images into the combined bitmap and return it.
        canvas.drawBitmap(left, 0f, 0f, null)
        canvas.drawBitmap(right, left.width.toFloat(), 0f, null)

        return combined
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