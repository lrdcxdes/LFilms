package dev.lrdcxdes.lfilms.api

data class Movie(
    val id: Int,
    val path: String,
    val title: String,
    val originalTitle: String,
    val imageUrl: String,
    val previewImageUrl: String,
    val ratingIMDB: Double,
    val votesIMDB: Int,
    val kinopoiskRating: Double,
    val kinopoiskVotes: Int,
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
