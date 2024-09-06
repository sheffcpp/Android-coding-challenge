package com.example.codingchallangemovieapp.ui

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class FavouriteManager(context: Context) {

    private companion object {
        private const val PREFS_NAME = "favourite_prefs"
        private const val FAVOURITES_KEY = "favourites"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()


    private fun getFavourites(): MutableList<Int> {
        val favouritesJson = sharedPreferences.getString(FAVOURITES_KEY, null)
        return if (favouritesJson != null) {
            val type = object : TypeToken<MutableList<Int>>() {}.type
            gson.fromJson(favouritesJson, type)
        } else {
            mutableListOf()
        }
    }

    val favouriteMovies = MutableStateFlow<List<Int>>(getFavourites())

    private suspend fun saveFavourites(favourites: List<Int>) {
        val favouritesJson = gson.toJson(favourites)
        sharedPreferences.edit()
            .putString(FAVOURITES_KEY, favouritesJson)
            .apply()

        withContext(Dispatchers.IO) {
            favouriteMovies.emit(getFavourites().toList())
        }
    }

    suspend fun addFavourite(item: Int) {
        val favourites = getFavourites()
        if (!favourites.contains(item)) {
            favourites.add(item)
            saveFavourites(favourites)
        }
    }

    suspend fun removeFavourite(item: Int) {
        val favourites = getFavourites()
        if (favourites.contains(item)) {
            favourites.remove(item)
            saveFavourites(favourites)
        }
    }
}
