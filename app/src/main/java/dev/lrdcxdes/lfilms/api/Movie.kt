package dev.lrdcxdes.lfilms.api

import androidx.compose.ui.graphics.Color

// extend int class
class Votes(private val votes: Int) {
    override fun toString(): String {
        return when {
            votes < 1000 -> votes.toString()
            votes % 1000 < 500 -> "${votes / 1000}k"
            else -> "${votes / 1000 + 1}k"
        }
    }
}

class Rating(private val rating: Double) {
    override fun toString(): String {
        return rating.toString()
    }

    fun getColor(): Color {
        // gradient from red to green by rating
        val red = (255 * (1 - rating / 10)).toInt()
        val green = (255 * (rating / 10)).toInt()
        return Color(red, green, 0)
    }
}

data class Movie(
    val id: Int,
    val path: String,
    val title: String,
    val originalTitle: String,
    val imageUrl: String,
    val previewImageUrl: String,
    val ratingIMDB: Rating,
    val votesIMDB: Votes,
    val kinopoiskRating: Rating,
    val kinopoiskVotes: Votes,
    val slogan: String,
    val releaseDate: String,
    val countries: List<String>,
    val directors: List<String>,
    val genres: List<String>,
    val ageRating: String,
    val duration: String,
    val series: List<String>,
    val actors: List<String>,
    val description: String,
    val translations: List<Translation>,
    val isSerial: Boolean,
    val isComingSoon: Boolean,
    val isRestricted: Boolean,
) {
    private fun getYear(): Int {
        return releaseDate.split(" ")[2].toInt()
    }

    fun getGenresString(): String {
        return genres.joinToString(", ")
    }

    fun preview(): MoviePreview {
        // description = 2019, США, Боевики
        val description =
            "${getYear()}, ${if (countries.isNotEmpty()) countries[0] else "..."}, ${if (genres.isNotEmpty()) genres[0] else "..."}"
        return MoviePreview(
            imageUrl = imageUrl,
            name = title,
            description = description,
            path = path,
        )
    }
}
