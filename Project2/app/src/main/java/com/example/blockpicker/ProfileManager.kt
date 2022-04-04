package com.example.blockpicker

import android.graphics.Bitmap
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ProfileManager {
    private val okHttpClient: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()

        // This will cause all network traffic to be logged to the console for easy debugging
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        okHttpClient = builder.build()
    }

    fun retrieveUUID(username: String) : String{

        // get minecraft UUID
        val requestUUID: Request = Request.Builder()
            .url("https://api.mojang.com/users/profiles/minecraft/$username")
            .get()
            .build()

        // Mojang API response
        val responseUUID: Response = okHttpClient.newCall(requestUUID).execute()
        val responseBodyUUID: String? = responseUUID.body?.string()

        // check if API call is successful
        if (responseUUID.isSuccessful && !responseBodyUUID.isNullOrEmpty()) {
            // parse and get UUID
            val json: JSONObject = JSONObject(responseBodyUUID)
            var UUID: String = json.getString("id")

            return UUID
        }

        return ""
    }
}