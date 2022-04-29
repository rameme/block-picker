package com.example.blockpicker

import java.io.Serializable

// Account data class
data class Accounts(
    val username: String,
    val minecraftUsername: String,
    val minecraftUUID: String,
) : Serializable {
    constructor() : this("","","")
}