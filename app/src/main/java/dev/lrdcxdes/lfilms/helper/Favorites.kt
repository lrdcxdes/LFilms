package dev.lrdcxdes.lfilms.helper

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.gson.Gson
import dev.lrdcxdes.lfilms.api.MoviePreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val KEY_FAVORITE_MOVIES = stringSetPreferencesKey("favorites")

suspend fun setFavorite(movie: MoviePreview, isFavorite: Boolean, context: Context): List<MoviePreview> {
    val favoriteMovies = getFavoriteMoviesSet(context)

    if (isFavorite) {
        favoriteMovies.add(movie)
    } else {
        favoriteMovies.remove(movie)
    }

    context.dataStore.edit { settings ->
        settings[KEY_FAVORITE_MOVIES] = favoriteMovies.toJsonSet()
    }

    return favoriteMovies.toList()
}

suspend fun getFavoriteMoviesSet(context: Context): MutableSet<MoviePreview> {
    val favoriteMoviesJsonSet = context.dataStore.data.map { preferences ->
        preferences[KEY_FAVORITE_MOVIES] ?: mutableSetOf()
    }.first()
    return favoriteMoviesJsonSet.toMoviesSet()
}

suspend fun getFavoriteMovies(context: Context): List<MoviePreview> {
    val favoriteMoviesJsonSet = context.dataStore.data.map { preferences ->
        preferences[KEY_FAVORITE_MOVIES] ?: mutableSetOf()
    }.first()
    return favoriteMoviesJsonSet.toMoviesSet().toList()
}

suspend fun inFavorites(context: Context, path: String): Boolean {
    val favoriteMovies = getFavoriteMoviesSet(context)
    return favoriteMovies.find { it.path == path } != null
}

private fun Set<MoviePreview>.toJsonSet(): Set<String> {
    return this.map { Gson().toJson(it) }.toSet()
}

private fun Set<String>.toMoviesSet(): MutableSet<MoviePreview> {
    return this.map { Gson().fromJson(it, MoviePreview::class.java) }.toMutableSet()
}

fun performSearchFavorites(text: String, moviesList: List<MoviePreview>): List<MoviePreview> {
    return moviesList.filter { movie ->
        movie.name.contains(text, ignoreCase = true)
    }
}