package com.example.blockpicker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class PalettesActivity: AppCompatActivity() {

    // init variables
    private lateinit var recyclerView : RecyclerView
    private lateinit var searchButton : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.palettes_activity)

        // log it
        Log.d("PalettesActivity", "onCreate called!")

        // set title
        title = resources.getText(R.string.palettes_activity_title);

        /* search */
        searchButton = findViewById(R.id.SearchPalettes)
        searchButton.setOnClickListener(){ view ->

        }

        /* palettes recyclerView */
        recyclerView = findViewById(R.id.ResultView)

        val palettes = getFakePalettes()
        val adapter = PalettesAdapter(palettes)
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getFakePalettes(): List<Palettes> {
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
                blockBitmap1 = R.drawable.stripped_birch_log,
                blockBitmap2 = R.drawable.prismarine,
                blockBitmap3 = R.drawable.sea_lantern,
                blockBitmap4 = R.drawable.birch_planks,
                blockBitmap5 = R.drawable.prismarine_bricks,
                blockBitmap6 = R.drawable.deepslate_tiles,
            ),
        )
    }
}