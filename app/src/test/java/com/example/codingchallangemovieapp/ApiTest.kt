package com.example.codingchallangemovieapp

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

class ApiTest {
    @Test
    fun testApiKey() {
        val apiKey = BuildConfig.API_KEY
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/movie/now_playing?language=en-US&page=1&api_key=$apiKey")
            .get()
            .addHeader("accept", "application/json")
            .build()

        val response = client.newCall(request).execute()
        println(response)

        assert(response.isSuccessful)
    }

}