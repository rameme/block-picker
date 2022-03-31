package com.example.blockpicker

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import android.widget.TextView

import android.widget.ArrayAdapter
import java.util.*
import android.widget.EditText
import android.widget.AdapterView.OnItemClickListener
import com.google.android.material.textfield.TextInputLayout

class CreatePalettesActivity : AppCompatActivity() {

    // init variables
    private lateinit var createBlock : ArrayList<ImageView>
    private lateinit var currentBlock : ImageView
    private lateinit var paletteList : TextView
    private lateinit var paletteName : TextInputLayout
    private lateinit var createButton : Button

    private var currentBlockIndex = 0
    private var currentBlockSize = 0;
    private lateinit var blockName : Array<String>

    private lateinit var block : TextView
    private lateinit var blockList : ArrayList<String>
    private lateinit var dialog : Dialog

    private lateinit var searchBlock : EditText
    private lateinit var listBlocks : ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_palettes_activity)

        // log it
        Log.d("CreatePalettesActivity", "onCreate called!")

        // set title
        title = resources.getText(R.string.CreatePalettes);


        // set value in array list
        blockList = ArrayList<String>()
        blockList = arrayListOf<String>(*resources.getStringArray(R.array.blocks))

        // Text View
        block = findViewById(R.id.SearchBlocks)

        /* image view */
        createBlock = ArrayList<ImageView>(6)
        blockName = arrayOf("","","","","","")

        var imageViewId = arrayOf(R.id.CreateBlock1,R.id.CreateBlock2,R.id.CreateBlock3,R.id.CreateBlock4,R.id.CreateBlock5,R.id.CreateBlock6);

        // get ImageView by and set on click listeners
        for(i in imageViewId.indices){
            var createBlockView : ImageView = findViewById(imageViewId[i])
            createBlockView.setOnClickListener() { view ->
                currentBlock = createBlockView
                currentBlockIndex = i
                selectBlock()
            }
            createBlock.add(createBlockView)
        }

        paletteList = findViewById(R.id.PaletteBlockList)

        paletteName = findViewById(R.id.NamePalette)

        createButton = findViewById(R.id.PaletteCreate)
        createButton.isEnabled = false
    }

    private fun selectBlock (){
        // Initialize dialog
        val builder = AlertDialog.Builder(this@CreatePalettesActivity)
        builder.setView(R.layout.searchable_spinner)

        dialog = builder.create()
        dialog.show()

        searchBlock = dialog.findViewById(R.id.SearchBlock)
        listBlocks = dialog.findViewById(R.id.ListBlocks)

        val adapter: ArrayAdapter<*> = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, blockList)

        listBlocks.adapter = adapter

        searchBlock.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.getFilter().filter(p0);
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        listBlocks.onItemClickListener = OnItemClickListener { parent, view, position, id -> // when item selected from list
            // set selected item on textView
            var blockSelected = parent.getItemAtPosition(position) as String

            if(blockName[currentBlockIndex].isEmpty()){
                currentBlockSize += 1
            }

            blockName[currentBlockIndex] = "$blockSelected\n"

            // if first block is changed change palette hint
            if(currentBlockIndex == 0){
                paletteName.hint = "$blockSelected Palette"
            }

            paletteList.text = blockName.joinToString().replace(", ","")

            var blockId = blockSelected.lowercase().replace(" ","_")
            Log.d("CreatePalettesActivity", "Block Filename: $blockId")

            val resId = resources.getIdentifier(
                blockId, "drawable",
                packageName
            )

            Picasso
                .get()
                .load(resId)
                .into(currentBlock)

            // Dismiss dialog
            dialog.dismiss()

            if(currentBlockSize == 6){
                createButton.isEnabled = true
            }
        }
    }
}