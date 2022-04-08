package com.example.blockpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PalettesAdapter(val palettes: List<Palettes>) : RecyclerView.Adapter<PalettesAdapter.ViewHolder>(){

    // row display 6 images
    class ViewHolder (rootLayout: View) : RecyclerView.ViewHolder(rootLayout){

        // bitmaps
        val blockBitmap1: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap1)
        val blockBitmap2: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap2)
        val blockBitmap3: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap3)
        val blockBitmap4: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap4)
        val blockBitmap5: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap5)
        val blockBitmap6: ImageView = rootLayout.findViewById(R.id.ProfileBlockBitmap6)

        // palettes info
        val paletteName: TextView = rootLayout.findViewById(R.id.ProfilePaletteName)
        val paletteAvatar: ImageView = rootLayout.findViewById(R.id.PaletteAuthorAvatar)
        val paletteAuthor: TextView = rootLayout.findViewById(R.id.PaletteAuthor)
        val paletteSaved: TextView = rootLayout.findViewById(R.id.PaletteProfileSaved)
    }

    // The RecyclerView needs a new row
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // A LayoutInflater is an object that knows how to read & parse an XML file
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)

        // Read & parse the XML file to create a new row at runtime
        val rootLayout: View = layoutInflater.inflate(R.layout.row_palettes, parent, false)

        // We can now create a ViewHolder from the root view
        val viewHolder = ViewHolder(rootLayout)
        return viewHolder
    }

    // The RecyclerView is ready to display a new (or recycled) row on the screen, represented a our ViewHolder.
    // We're given the row position / index that needs to be rendered.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPalettes = palettes[position]

        Picasso
            .get()
            .load(currentPalettes.blockBitmap1)
            .into(holder.blockBitmap1)

        Picasso
            .get()
            .load(currentPalettes.blockBitmap2)
            .into(holder.blockBitmap2)

        Picasso
            .get()
            .load(currentPalettes.blockBitmap3)
            .into(holder.blockBitmap3)

        Picasso
            .get()
            .load(currentPalettes.blockBitmap4)
            .into(holder.blockBitmap4)

        Picasso
            .get()
            .load(currentPalettes.blockBitmap5)
            .into(holder.blockBitmap5)

        Picasso
            .get()
            .load(currentPalettes.blockBitmap6)
            .into(holder.blockBitmap6)

        holder.paletteName.text = currentPalettes.name
        holder.paletteAuthor.text = currentPalettes.author
        holder.paletteSaved.text = currentPalettes.likes.toString()
    }

    // total rows we want the adapter to render
    override fun getItemCount(): Int {
        return palettes.size
    }
}

/* fake date for our adapter */
fun getFakePalettes(): List<Palettes> {
    return listOf(
        Palettes(
            name = "Blocks",
            author = "rameme",
            likes = 10,
            blockBitmap1 = R.drawable.green_wool,
            blockBitmap2 = R.drawable.green_terracotta,
            blockBitmap3 = R.drawable.dark_oak_log,
            blockBitmap4 = R.drawable.brain_coral_block,
            blockBitmap5 = R.drawable.mossy_stone_bricks,
            blockBitmap6 = R.drawable.moss_block,
        ),
        Palettes(
            name = "Blocks",
            author = "rameme",
            likes = 10,
            blockBitmap1 = R.drawable.stripped_birch_log,
            blockBitmap2 = R.drawable.prismarine,
            blockBitmap3 = R.drawable.sea_lantern,
            blockBitmap4 = R.drawable.birch_planks,
            blockBitmap5 = R.drawable.prismarine_bricks,
            blockBitmap6 = R.drawable.deepslate_tiles,
        ),
        Palettes(
            name = "Blocks",
            author = "rameme",
            likes = 10,
            blockBitmap1 = R.drawable.blackstone,
            blockBitmap2 = R.drawable.gray_concrete,
            blockBitmap3 = R.drawable.gray_concrete_powder,
            blockBitmap4 = R.drawable.stone,
            blockBitmap5 = R.drawable.stone_bricks,
            blockBitmap6 = R.drawable.andesite,
        ),
    )
}