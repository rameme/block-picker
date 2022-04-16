package com.example.blockpicker

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class SignupManager {
    private val okHttpClient: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()

        // This will cause all network traffic to be logged to the console for debugging
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        okHttpClient = builder.build()
    }

    fun retrieveUUID(username: String) : String {

        // Get minecraft UUID
        val requestUUID: Request = Request.Builder()
            .url("https://api.mojang.com/users/profiles/minecraft/$username")
            .get()
            .build()

        // Mojang API response
        val responseUUID: Response = okHttpClient.newCall(requestUUID).execute()
        val responseBodyUUID: String? = responseUUID.body?.string()

        // Check if API call is successful
        if (responseUUID.isSuccessful && !responseBodyUUID.isNullOrEmpty()) {
            // parse and get UUID
            val json: JSONObject = JSONObject(responseBodyUUID)
            var UUID: String = json.getString("id")

            return UUID
        }

        // Return null
        return ""
    }
}