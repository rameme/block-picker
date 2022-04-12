package com.example.blockpicker

import java.io.Serializable

data class Palettes(
    val name: String,
    val author: String,
    val authorUID: String,
    val minecraftUUID: String,
    val likes: Int,
    val block1: String,
    val block2: String,
    val block3: String,
    val block4: String,
    val block5: String,
    val block6: String
) : Serializable {
    constructor() : this("","","","",0,"","","","","","")
}