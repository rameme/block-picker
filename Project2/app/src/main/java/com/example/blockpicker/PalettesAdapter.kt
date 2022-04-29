package com.example.blockpicker

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.squareup.picasso.Picasso
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.startActivity

class PalettesAdapter(val palettes: List<Palettes>) : RecyclerView.Adapter<PalettesAdapter.ViewHolder>(){

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseDatabase: FirebaseDatabase

    // Block Palettes
    class ViewHolder (rootLayout: View) : RecyclerView.ViewHolder(rootLayout){

        // Palettes consists of 6 images
        val blockBitmap1: ImageView = rootLayout.findViewById(R.id.BlockBitmap1)
        val blockBitmap2: ImageView = rootLayout.findViewById(R.id.BlockBitmap2)
        val blockBitmap3: ImageView = rootLayout.findViewById(R.id.BlockBitmap3)
        val blockBitmap4: ImageView = rootLayout.findViewById(R.id.BlockBitmap4)
        val blockBitmap5: ImageView = rootLayout.findViewById(R.id.BlockBitmap5)
        val blockBitmap6: ImageView = rootLayout.findViewById(R.id.BlockBitmap6)

        // Additional Palette information: name, avatar, author, and likes
        val paletteName: TextView = rootLayout.findViewById(R.id.PaletteName)
        val paletteAvatar: ImageView = rootLayout.findViewById(R.id.PaletteAuthorAvatar)
        val paletteAuthor: TextView = rootLayout.findViewById(R.id.PaletteAuthor)
        val paletteSaved: TextView = rootLayout.findViewById(R.id.PaletteSaved)
        val paletteSavedButton: ImageButton = rootLayout.findViewById(R.id.PaletteSavedButton)
        val paletteShareButton: ImageButton = rootLayout.findViewById(R.id.PaletteShareButton)

        val paletteCardView : CardView = rootLayout.findViewById(R.id.PaletteCardView)
    }

    //  Create new rows
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val rootLayout: View = layoutInflater.inflate(R.layout.row_palettes, parent, false)
        return ViewHolder(rootLayout)
    }

    // Display row on the screen
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentPalettes = palettes[position]
        val context = holder.blockBitmap1.context

        // On click listener
        holder.paletteCardView.setOnClickListener(){
            val intent = Intent(holder.paletteCardView.context, PalettesDetailActivity::class.java)
            intent.putExtra("PALETTE", currentPalettes)
            startActivity(context, intent, null)
        }

        // Load blocks from local storage
        Picasso
            .get()
            .load(getResId(currentPalettes.block1, context))
            .into(holder.blockBitmap1)

        Picasso
            .get()
            .load(getResId(currentPalettes.block2, context))
            .into(holder.blockBitmap2)

        Picasso
            .get()
            .load(getResId(currentPalettes.block3, context))
            .into(holder.blockBitmap3)

        Picasso
            .get()
            .load(getResId(currentPalettes.block4, context))
            .into(holder.blockBitmap4)

        Picasso
            .get()
            .load(getResId(currentPalettes.block5, context))
            .into(holder.blockBitmap5)

        Picasso
            .get()
            .load(getResId(currentPalettes.block6, context))
            .into(holder.blockBitmap6)

        // Share Palette
        holder.paletteShareButton.setOnClickListener() {
            ShareCompat.IntentBuilder(context)
                .setType("text/plain")
                .setChooserTitle("Share URL")
                .setText(currentPalettes.paletteUrl)
                .startChooser()
        }

        // Show player Avatar
        Picasso
            .get()
            .load("https://crafatar.com/avatars/${currentPalettes.minecraftUUID}")
            .into(holder.paletteAvatar)

        holder.paletteName.text = currentPalettes.name
        holder.paletteAuthor.text = currentPalettes.author
        holder.paletteSaved.text = currentPalettes.likes.toString()

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseDatabase = FirebaseDatabase.getInstance()

        val UID = firebaseAuth.currentUser!!.uid

        /* Update like button icon */
        // Disable like button if we own the Palette
        if(currentPalettes.authorUID == UID){
            holder.paletteSavedButton.isEnabled = false
            holder.paletteSavedButton.setImageResource(R.drawable.ic_favorite)
        }
        // Set like button icon
        else {
            if(currentPalettes.liked){
                holder.paletteSavedButton.setImageResource(R.drawable.ic_favorite_red)
            } else {
                holder.paletteSavedButton.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        /* Update likes Firebase */
        holder.paletteSavedButton.setOnClickListener(){

            val referencePalettes = firebaseDatabase.getReference("palettes")
            // Like the palette, increment the counter and add user to Saved
            if(!currentPalettes.liked){
                holder.paletteSavedButton.setImageResource(R.drawable.ic_favorite_red)
                currentPalettes.liked = true

                // Log it
                firebaseAnalytics.logEvent("like_button_clicked_increment", null)

                // Add user to "saved"
                referencePalettes
                    .child(currentPalettes.paletteID)
                    .child("saved")
                    .child(UID).setValue(true)

                referencePalettes
                    .child(currentPalettes.paletteID)
                    .child("likes")
                    .setValue(ServerValue.increment(1))

            }
            // Unlike the Palette, decrement the counter and remove user from Saved
            else {
                holder.paletteSavedButton.setImageResource(R.drawable.ic_favorite_border)
                currentPalettes.liked = false

                firebaseAnalytics.logEvent("like_button_clicked_decrement", null)

                // Remove user from "saved"
                referencePalettes
                    .child(currentPalettes.paletteID)
                    .child("saved")
                    .child(UID)
                    .removeValue()

                // Decrement palette like
                referencePalettes
                    .child(currentPalettes.paletteID)
                    .child("likes")
                    .setValue(ServerValue.increment(-1))
            }
        }
    }

    // Total rows we want the adapter to render
    override fun getItemCount(): Int {
        return palettes.size
    }

    // Helper function: get the resource ID from the block name
    private fun getResId(block: String, context: Context): Int {
        // Get the drawable ID from block name
        var block1Id = block.lowercase().replace(" ", "_")

        return context.resources.getIdentifier(
            block1Id, "drawable",
            context.packageName
        )
    }
}