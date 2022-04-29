package com.example.blockpicker

import java.io.Serializable

data class Palettes(
    val paletteID: String,
    val name: String,
    val author: String,
    val authorUID: String,
    val minecraftUUID: String,
    var likes: Int,
    var liked: Boolean,
    val paletteUrl: String,
    val block1: String,
    val block2: String,
    val block3: String,
    val block4: String,
    val block5: String,
    val block6: String,
    val blocks: HashMap<String, Boolean>?,
    ) : Serializable {
    constructor() : this("","","","","",0, false,"","","","","","","",null)
}