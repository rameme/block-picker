package com.example.blockpicker

import java.io.Serializable

data class Accounts(
    val username: String,
    val minecraft_username: String,
    val minecraft_UUID: String,
) : Serializable {
    constructor() : this("","","")
}