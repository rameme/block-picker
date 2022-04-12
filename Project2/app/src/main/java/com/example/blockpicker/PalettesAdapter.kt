package com.example.blockpicker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PalettesAdapter(val palettes: List<Palettes>) : RecyclerView.Adapter<PalettesAdapter.ViewHolder>(){

    // Block Palettes
    class ViewHolder (rootLayout: View) : RecyclerView.ViewHolder(rootLayout){

        // Palettes consists of 6 images
        val blockBitmap1: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap1)
        val blockBitmap2: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap2)
        val blockBitmap3: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap3)
        val blockBitmap4: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap4)
        val blockBitmap5: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap5)
        val blockBitmap6: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap6)

        // Additional Palette information: name, avatar, author, and likes
        val paletteName: TextView = rootLayout.findViewById(R.id.ProfilePaletteName)
        val paletteAvatar: ImageView = rootLayout.findViewById(R.id.PaletteAuthorAvatar)
        val paletteAuthor: TextView = rootLayout.findViewById(R.id.PaletteAuthor)
        val paletteSaved: TextView = rootLayout.findViewById(R.id.PaletteProfileSaved)
    }

    //  Create new rows
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val rootLayout: View = layoutInflater.inflate(R.layout.row_palettes, parent, false)
        val viewHolder = ViewHolder(rootLayout)
        return viewHolder
    }

    // Display row on the screen
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPalettes = palettes[position]

        val context = holder.blockBitmap1.context

        // Show block palette
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

        // Show Avatar
        Picasso
            .get()
            .load("https://crafatar.com/avatars/${currentPalettes.minecraftUUID}")
            .into(holder.paletteAvatar)

        holder.paletteName.text = currentPalettes.name
        holder.paletteAuthor.text = currentPalettes.author
        holder.paletteSaved.text = currentPalettes.likes.toString()
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

/* Fake date for our adapter */
fun getFakePalettes(): List<Palettes> {
    return listOf(
        Palettes(
            name = "Blocks",
            author = "rameme",
            authorUID = "0",
            minecraftUUID = "0",
            likes = 10,
            block1 = "Green Wool",
            block2 = "Green Terracotta",
            block3 = "Dark Oak Log",
            block4 = "Brain Coral Block",
            block5 = "Mossy Stone Bricks",
            block6 = "Moss_block",
        ),
        Palettes(
            name = "Blocks",
            author = "rameme",
            authorUID = "0",
            minecraftUUID = "0",
            likes = 10,
            block1 = "Stripped_birch Log",
            block2 = "Prismarine",
            block3 = "Sea Lantern",
            block4 = "Birch Planks",
            block5 = "Prismarine Bricks",
            block6 = "Deepslate Tiles",
        ),
        Palettes(
            name = "Blocks",
            author = "rameme",
            authorUID = "0",
            minecraftUUID = "0",
            likes = 10,
            block1 = "Blackstone",
            block2 = "Gray Concrete",
            block3 = "Gray Concrete Powder",
            block4 = "Stone",
            block5 = "Stone_bricks",
            block6 = "Andesite",
        ),
    )
}