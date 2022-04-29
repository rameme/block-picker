package com.example.blockpicker

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.squareup.picasso.Picasso

class PalettesDetailActivity : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.palettes_detail_activity)

        /* Load Palette Data */
        // IDs
        var imageViewId = arrayOf(R.id.Block1,R.id.Block2,R.id.Block3,R.id.Block4,R.id.Block5,R.id.Block6);
        var imageIconId = arrayOf(R.id.BlockIcon1,R.id.BlockIcon2,R.id.BlockIcon3,R.id.BlockIcon4,R.id.BlockIcon5,R.id.BlockIcon6)
        var paletteBlockId = arrayOf(R.id.PaletteBlock1,R.id.PaletteBlock2,R.id.PaletteBlock3,R.id.PaletteBlock4,R.id.PaletteBlock5,R.id.PaletteBlock6);

        // Get palette information
        var palette : Palettes = intent.getSerializableExtra("PALETTE") as Palettes

        val blocks = arrayOf(palette.block1,palette.block2,palette.block3,palette.block4,palette.block5,palette.block6)

        // Set ImageView and TextView
        for(i in imageViewId.indices){
            var createBlockView : ImageView = findViewById(imageViewId[i])
            var createBlockIcon : ImageView = findViewById(imageIconId[i])
            var paletteBlockText : TextView = findViewById(paletteBlockId[i])

            paletteBlockText.text = blocks[i]

            // Get the drawable ID from block name
            var blockId = blocks[i].lowercase().replace(" ","_")
            val resId = resources.getIdentifier(
                blockId, "drawable",
                packageName
            )

            // Load icon
            Picasso
                .get()
                .load(resId)
                .into(createBlockIcon)

            // Load image
            Picasso
                .get()
                .load(resId)
                .into(createBlockView)
        }

        // Palette title
        var paletteName : TextView = findViewById(R.id.PaletteName)
        paletteName.text = palette.name
        title = palette.name

        // Author
        var paletteAuthor : TextView = findViewById(R.id.PaletteAuthor)
        paletteAuthor.text = palette.author

        // Likes
        var paletteSaved : TextView = findViewById(R.id.PaletteSaved)
        paletteSaved.text = palette.likes.toString()

        // Show player Avatar
        var avatar : ImageView = findViewById(R.id.PaletteAuthorAvatar)
        Picasso
            .get()
            .load("https://crafatar.com/avatars/${palette.minecraftUUID}")
            .into(avatar)

        // Share
        var paletteShareButton : ImageButton = findViewById(R.id.PaletteShareButton)
        paletteShareButton.setOnClickListener() {
            ShareCompat.IntentBuilder(this)
                .setType("text/plain")
                .setChooserTitle("Share URL")
                .setText(palette.paletteUrl)
                .startChooser()
        }

        /* likes */
        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseDatabase = FirebaseDatabase.getInstance()

        val UID = firebaseAuth.currentUser!!.uid

        var paletteSavedButton : ImageButton = findViewById(R.id.PaletteSavedButton)

        // Disable like button if we own the Palette
        if(palette.authorUID == UID){
            paletteSavedButton.isEnabled = false
            paletteSavedButton.setImageResource(R.drawable.ic_favorite)
        }
        // Set like button icon
        else {
            if(palette.liked){
                paletteSavedButton.setImageResource(R.drawable.ic_favorite_red)
            } else {
                paletteSavedButton.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        /* Update likes Firebase */
        paletteSavedButton.setOnClickListener(){

            val referencePalettes = firebaseDatabase.getReference("palettes")

            // Like the palette, increment the counter and add user to Saved
            if(!palette.liked){
                paletteSavedButton.setImageResource(R.drawable.ic_favorite_red)
                palette.liked = true

                // Log it
                firebaseAnalytics.logEvent("like_button_clicked_increment", null)

                // Add user to "saved"
                referencePalettes
                    .child(palette.paletteID)
                    .child("saved")
                    .child(UID).setValue(true)

                referencePalettes
                    .child(palette.paletteID)
                    .child("likes")
                    .setValue(ServerValue.increment(1))

            }
            // Unlike the Palette, decrement the counter and remove user from Saved
            else {
                paletteSavedButton.setImageResource(R.drawable.ic_favorite_border)
                palette.liked = false

                // Log it
                firebaseAnalytics.logEvent("like_button_clicked_decrement", null)

                // Remove user from "saved"
                referencePalettes
                    .child(palette.paletteID)
                    .child("saved")
                    .child(UID)
                    .removeValue()

                // Decrement palette like
                referencePalettes
                    .child(palette.paletteID)
                    .child("likes")
                    .setValue(ServerValue.increment(-1))
            }
        }
    }

    /* Close CreatePalettesDetail */
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