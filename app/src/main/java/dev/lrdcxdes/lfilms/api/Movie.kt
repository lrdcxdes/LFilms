package dev.lrdcxdes.lfilms.api

import androidx.compose.ui.graphics.Color

// extend int class
class Votes(private val votes: Int) {
    override fun toString(): String {
        val votesString = votes.toString()
        return when (votesString.length) {
            7 -> "${votesString[0]},${votesString[1]}M"
            6 -> "${votesString[0]}${votesString[1]}${votesString[2]},${votesString[3]}K"
            5 -> "${votesString[0]}${votesString[1]},${votesString[2]}K"
            4 -> "${votesString[0]},${votesString[1]}K"
            else -> votesString
        }
    }

    fun isNotNull(): Boolean {
        return votes != 0
    }
}

class Rating(private val rating: Double) {
    override fun toString(): String {
        return rating.toString()
    }

    fun isNotNull(): Boolean {
        return rating != 0.0
    }

    private fun interpolateColor(startColor: Color, endColor: Color, ratio: Float): Color {
        val r = startColor.red + ratio * (endColor.red - startColor.red)
        val g = startColor.green + ratio * (endColor.green - startColor.green)
        val b = startColor.blue + ratio * (endColor.blue - startColor.blue)
        return Color(r, g, b, 1.0f)
    }

    fun getColor(): Color {
        val minRating = 0.0
        val maxRating = 10.0

        val clampedRating = maxOf(minRating, minOf(rating, maxRating))
        val ratio = clampedRating.toFloat() / maxRating.toFloat()

        val red = Color(0.8f, 0.4f, 0.4f, 1.0f) // Slightly brighter red
        val sandGold = Color(0.8f, 0.655f, 0.2f, 1.0f) // Brighter sand gold color
        val green = Color(0.4f, 0.8f, 0.4f, 1.0f) // Slightly brighter green

        return when {
            ratio <= 0.5 -> interpolateColor(red, sandGold, ratio * 2)
            else -> interpolateColor(sandGold, green, (ratio - 0.5f) * 2)
        }
    }
}

class ReleaseDate(private val releaseDate: String) {
    private val enMonths = mapOf(
        "января" to "January",
        "февраля" to "February",
        "марта" to "March",
        "апреля" to "April",
        "мая" to "May",
        "июня" to "June",
        "июля" to "July",
        "августа" to "August",
        "сентября" to "September",
        "октября" to "October",
        "ноября" to "November",
        "декабря" to "December",
    )

    private val uaMonths = mapOf(
        "января" to "січня",
        "февраля" to "лютого",
        "марта" to "березня",
        "апреля" to "квітня",
        "мая" to "травня",
        "июня" to "червня",
        "июля" to "липня",
        "августа" to "серпня",
        "сентября" to "вересня",
        "октября" to "жовтня",
        "ноября" to "листопада",
        "декабря" to "грудня",
    )

    private fun getUkrainianReleaseDate(): String {
        val date = releaseDate.split(" ")
        return "${date[0]} ${uaMonths[date[1]]} ${date[2]} року"
    }

    private fun getEnglishReleaseDate(): String {
        val date = releaseDate.split(" ")
        return "${enMonths[date[1]]} ${date[0]}, ${date[2]}"
    }

    fun toString(lang: String): String {
        return when (lang) {
            "uk" -> getUkrainianReleaseDate()
            "en" -> getEnglishReleaseDate()
            else -> releaseDate
        }
    }

    override fun toString(): String {
        return releaseDate
    }
}


class Duration(private val duration: String) {
    fun toString(lang: String): String {
        return when (lang) {
            "uk" -> duration.replace("мин.", "хв.")
            "en" -> duration.replace("мин.", "min.")
            else -> duration
        }
    }

    override fun toString(): String {
        return duration
    }

    fun isNotNull(): Boolean {
        return duration.isNotEmpty() && duration != "0 мин."
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
    val releaseDate: ReleaseDate,
    val countries: List<String>,
    val directors: List<String>,
    val genres: List<String>,
    val ageRating: String,
    val duration: Duration,
    val series: List<String>,
    val actors: List<String>,
    val description: String,
    val translations: List<Translation>,
    val isSerial: Boolean,
    val isComingSoon: Boolean,
    val isRestricted: Boolean,
) {
    private fun getYear(): Int {
        return releaseDate.toString().split(" ")[2].toInt()
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
