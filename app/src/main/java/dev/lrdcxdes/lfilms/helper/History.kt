package dev.lrdcxdes.lfilms.helper

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.gson.Gson
import dev.lrdcxdes.lfilms.api.MoviePreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val KEY_HISTORY = stringSetPreferencesKey("history")

data class EpisodeHistory(
    val episode: Int,
    val season: Int,
)

data class HistoryPreview(
    val name: String,
    val description: String,
    val imageUrl: String,
    val path: String,
    val translation: Int,
    val season: Int? = null,
    val episode: Int? = null,
    val episodesWatched: MutableSet<EpisodeHistory> = mutableSetOf(),
) {
    fun preview(): MoviePreview {
        return MoviePreview(
            name,
            description,
            imageUrl,
            path,
        )
    }
}

suspend fun setHistory(
    movie: MoviePreview,
    season: Int? = null,
    episode: Int? = null,
    translation: Int,
    context: Context
): List<MoviePreview> {
    val historyMovies = getHistoryMoviesList(context)
    val historyPreview = historyMovies.find { m ->
        m.path == movie.path
    }
    println("historyPreview: $historyPreview")
    val episodes = historyPreview?.episodesWatched ?: mutableSetOf()

    if (season != null && episode != null) {
        episodes.add(EpisodeHistory(episode, season))
    }

    val newHistoryPreview = HistoryPreview(
        movie.name,
        movie.description,
        movie.imageUrl,
        movie.path,
        translation,
        season,
        episode,
        episodes
    )

    historyMovies.removeIf { m ->
        m.path == movie.path
    }
    historyMovies.add(newHistoryPreview)

    context.dataStore.edit { settings ->
        settings[KEY_HISTORY] = historyMovies.toJsonSet()
    }

    return historyMovies.toMoviePreviewList()
}

suspend fun getHistoryMoviesList(context: Context): MutableList<HistoryPreview> {
    val historyMoviesJsonSet = context.dataStore.data.map { preferences ->
        preferences[KEY_HISTORY] ?: mutableSetOf()
    }.first()
    return historyMoviesJsonSet.toHistoryPreviewList()
}

suspend fun getHistoryMoviesPreview(context: Context): List<MoviePreview> {
    val historyMoviesJsonSet = context.dataStore.data.map { preferences ->
        preferences[KEY_HISTORY] ?: mutableSetOf()
    }.first()
    return historyMoviesJsonSet.toHistoryPreviewList().map { movie ->
        movie.preview()
    }
}

suspend fun getHistoryMovie(
    path: String,
    context: Context
): HistoryPreview? {
    val historyMoviesJsonSet = context.dataStore.data.map { preferences ->
        preferences[KEY_HISTORY] ?: mutableSetOf()
    }.first()
    return historyMoviesJsonSet.toHistoryPreviewList().find { movie ->
        movie.path == path
    }
}

fun List<HistoryPreview>.toJsonSet(): Set<String> {
    return this.map { Gson().toJson(it) }.toSet()
}

private fun Set<String>.toHistoryPreviewList(): MutableList<HistoryPreview> {
    return this.map { Gson().fromJson(it, HistoryPreview::class.java) }.toMutableList()
}

fun MutableList<HistoryPreview>.toMoviePreviewList(): List<MoviePreview> {
    return this.map { it.preview() }
}
