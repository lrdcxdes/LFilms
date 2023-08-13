package dev.lrdcxdes.lfilms.api

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import dev.lrdcxdes.lfilms.api.interceptors.DefaultInterceptor
import dev.lrdcxdes.lfilms.api.interceptors.ErrorInterceptor
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URI
import java.security.cert.X509Certificate
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class ApiError(message: String) : Exception(message)

data class Subtitle(val url: String, val lang: String, val name: String)
data class Stream(val url: String, val quality: String, val subtitles: List<Subtitle>)
data class Episode(val id: Int)
data class Season(val id: Int, var episodes: List<Episode>)
data class Translation(val id: Int, val name: String)

class Api {
    private var scheme = "https"
    private var host = "rezka.ag"

    private var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
            " AppleWebKit/537.36 (KHTML, like Gecko)" +
            " Chrome/117.0.0.0 Safari/537.36"

    @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )

    private var sslContext: SSLContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, java.security.SecureRandom())
    }


    var client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(DefaultInterceptor(userAgent))
        .addInterceptor(ErrorInterceptor())
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .build()

    fun setScheme(newProtocol: String) {
        scheme = newProtocol
    }

    fun setHost(newHost: String) {
        host = newHost
    }

    private fun getScheme(): String {
        return scheme
    }

    private fun getHost(): String {
        return host
    }

    suspend fun searchAjax(query: String): List<SearchResultItem> =
        suspendCoroutine { continuation ->
            val url = HttpUrl.Builder()
                .scheme(getScheme())
                .host(getHost())
                .addPathSegment("engine")
                .addPathSegment("ajax")
                .addPathSegment("search.php")
                .build()

            val request = okhttp3.Request.Builder()
                .url(url)
                .post(FormBody.Builder().add("q", query).build())
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val body = response.body?.string()

                        if (body.isNullOrBlank()) {
                            continuation.resume(emptyList())
                            return
                        }

                        val regex =
                            Regex("<li>\\s*<a href=\"([^\"]+)\">\\s*<span class=\"enty\">([^<]+)</span>\\s*\\(([^,]+),[^,]+, ([^)]+)\\)\\s*<span class=\"rating\">\\s*<i class=\"hd-tooltip rating-green-string\" title=\"[^\"]+\">([^<]+)</i>\\s*</span>\\s*</a>\\s*</li>")
                        val matches = regex.findAll(body)
                        val result = matches.map {
                            SearchResultItem(
                                it.groupValues[2],
                                it.groupValues[1],
                                it.groupValues[5].toDoubleOrNull() ?: 0.0
                            )
                        }.toList()
                        continuation.resume(result)
                    }
                }
            })
        }

    suspend fun search(query: String? = null, filter: String? = null, page: Int = 1): MoviesList =
        suspendCoroutine { continuation ->
            var url = HttpUrl.Builder()
                .scheme(getScheme())
                .host(getHost())

            if (filter != null) {
                url = url.addPathSegment("page")
                    .addPathSegment(page.toString())
            } else if (query != null) {
                url = url.addPathSegment("search")
            }

            if (query != null) {
                url = url.addQueryParameter("do", "search")
                    .addQueryParameter("subaction", "search")
                    .addQueryParameter("q", query)
            } else if (filter != null) {
                url = url.addQueryParameter("filter", filter)
            }

            val request = okhttp3.Request.Builder()
                .url(url.build())
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val body = response.body?.string()

                        if (body.isNullOrBlank()) {
                            continuation.resume(MoviesList(1, 1, emptyList()))
                            return
                        }

                        val soup = Jsoup.parse(body)
                        val elements = soup.select("div.b-content__inline_item")

                        val items = elements.map {
                            val title = it.select("div.b-content__inline_item-link > a").text()
                            val description =
                                it.select("div.b-content__inline_item-link > div").text()
                            val link = it.select("div.b-content__inline_item-link > a").attr("href")
                            val path = link.substringAfter("://").substringAfter("/")
                            val imageUrl =
                                it.select("div.b-content__inline_item-cover > a > img").attr("src")
                            MoviePreview(title, description, imageUrl, path)
                        }.toList()

                        val pagination = soup.select("div.b-navigation").first()
                        if (pagination != null) {
                            if (pagination.select("span").size < 3) {
                                continuation.resume(
                                    MoviesList(
                                        1,
                                        1,
                                        items
                                    )
                                )
                                return
                            }
                            val pages = pagination.select("a")
                            if (pages.size < 2) {
                                continuation.resume(
                                    MoviesList(
                                        1,
                                        1,
                                        items
                                    )
                                )
                                return
                            }
                            val maxPages = pages[pages.size - 2].text().toInt()
                            var currentPage = 1
                            for (span in pagination.select("span")) {
                                if (span.attr("class").isEmpty()) {
                                    currentPage = span.text().toInt()
                                    break
                                }
                            }
                            continuation.resume(MoviesList(currentPage, maxPages, items))
                        } else {
                            continuation.resume(MoviesList(1, 1, items))
                        }
                    }
                }
            })
        }

    suspend fun watching(page: Int = 1): MoviesList {
        return search(filter = "watching", page = page)
    }

    suspend fun getMovie(path: String): Movie? = suspendCoroutine { continuation ->
        val url = HttpUrl.Builder()
            .scheme(getScheme())
            .host(getHost())
            .addPathSegments(path)
            .build()

        val request = okhttp3.Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = response.body?.string()
                    if (body == null) {
                        continuation.resume(null)
                        return
                    }

                    val soup = Jsoup.parse(body)
                    val id = path.substringAfterLast("/").substringBefore("-").toIntOrNull()
                    if (id == null) {
                        continuation.resume(null)
                        return
                    }
                    val title = soup.select("div.b-post__title > h1").text()
                    val originalTitle = soup.select("div.b-post__origtitle").text()
                    val imageUrl =
                        soup.select("div.b-post__infotable_left > div.b-sidecover > a").attr("href")
                    val previewImageUrl =
                        soup.select("div.b-post__infotable_left > div.b-sidecover > a > img")
                            .attr("src")
                    val rating =
                        soup.select("span.b-post__info_rates.imdb > span").text().toDoubleOrNull()
                            ?: 0.0
                    val votes =
                        soup.select("span.b-post__info_rates.imdb > i").text().replace("(", "")
                            .replace(")", "").replace(" ", "")
                            .toIntOrNull() ?: 0
                    val kinopoiskRating =
                        soup.select("span.b-post__info_rates.kp > span").text().toDoubleOrNull()
                            ?: 0.0
                    val kinopoiskVotes =
                        soup.select("span.b-post__info_rates.kp > i").text().replace("(", "")
                            .replace(")", "").replace(" ", "")
                            .toIntOrNull() ?: 0

                    // TODO: Parse all fields
                    var slogan = ""
                    var releaseDate = ""
                    var countries = emptyList<String>()
                    var directors = emptyList<String>()
                    var genres = emptyList<String>()
                    var ageRating = ""
                    var duration = ""
                    var series = emptyList<String>()
                    var actors = emptyList<String>()

                    // Define a mapping of field names to their corresponding CSS selector queries
                    val fieldSelectors = mapOf(
                        "Слоган" to "table.b-post__info td.l:contains(Слоган) + td",
                        "Дата выхода" to "table.b-post__info td.l:contains(Дата выхода) + td",
                        "Страна" to "table.b-post__info td.l:contains(Страна) + td a",
                        "Режиссер" to "table.b-post__info td.l:contains(Режиссер) + td a",
                        "Жанр" to "table.b-post__info td.l:contains(Жанр) + td a span[itemprop=genre]",
                        "Возраст" to "table.b-post__info td.l:contains(Возраст) + td span.bold",
                        "Время" to "table.b-post__info td.l:contains(Время) + td[itemprop=duration]",
                        "Из серии" to "table.b-post__info td.l:contains(Из серии) + td a",
                        "В ролях" to "table.b-post__info span[itemprop=actor] span[itemprop=name]"
                    )

                    // Loop through the field selectors and extract the corresponding information
                    fieldSelectors.forEach { (fieldName, selector) ->
                        val elements = soup.select(selector)
                        when (fieldName) {
                            "Слоган" -> slogan = elements.text()
                            "Дата выхода" -> releaseDate = elements.text()
                            "Страна" -> countries = elements.map { it.text() }
                            "Режиссер" -> directors = elements.map { it.text() }
                            "Жанр" -> genres = elements.map { it.text() }
                            "Возраст" -> ageRating = elements.text()
                            "Время" -> duration = elements.text()
                            "Из серии" -> series = elements.map { it.text() }
                            "В ролях" -> actors = elements.map { it.text() }
                        }
                    }

                    val description =
                        soup.select("div.b-post__description > div.b-post__description_text").text()

                    var translations = soup.select("ul#translators-list > li.b-translator__item")
                        .map {
                            Translation(
                                it.attr("data-translator_id").toInt(),
                                it.text() + if (it.select("img").attr("src")
                                        .contains("ua.png")
                                ) " \uD83C\uDDFA\uD83C\uDDE6" else ""
                            )
                        }
                    if (translations.isEmpty()) {
                        translations = listOf(Translation(110, "Оригинал"))
                    }

                    val isSerial = soup.select("meta[property=og:type]").attr("content")
                        .contains("tv_series")

                    val isComing = soup.select("div.b-post__lastepisodeout > h2").text()
                        .contains("трейлер на русском языке")

                    val isRestricted =
                        soup.select("span.b-player__restricted__block_message").isNotEmpty()

                    continuation.resume(
                        Movie(
                            id,
                            path,
                            title,
                            originalTitle,
                            imageUrl,
                            previewImageUrl,
                            Rating(rating),
                            Votes(votes),
                            Rating(kinopoiskRating),
                            Votes(kinopoiskVotes),
                            slogan,
                            ReleaseDate(releaseDate),
                            countries,
                            directors,
                            genres,
                            ageRating,
                            Duration(duration),
                            series,
                            actors,
                            description,
                            translations,
                            isSerial,
                            isComing,
                            isRestricted
                        )
                    )
                }
            }
        })
    }

    suspend fun getTrailer(movieId: Int): String? = suspendCoroutine { continuation ->
        val url = HttpUrl.Builder()
            .scheme(getScheme())
            .host(getHost())
            .addPathSegment("engine")
            .addPathSegment("ajax")
            .addPathSegment("gettrailervideo.php")
            .build()

        val request = okhttp3.Request.Builder()
            .url(url)
            .post(
                FormBody.Builder()
                    .add("id", movieId.toString())
                    .build()
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = response.body?.string()
                    if (body == null) {
                        continuation.resume(null)
                        return
                    }
                    val json = JSONObject(body)
                    val result = if (json.getBoolean("success")) {
                        val code = json.getString("code")
                        val document = Jsoup.parse(code)
                        val iframe = document.select("iframe").first()
                        val src = iframe?.attr("src")
                        if (src != null) {
                            val videoId = src.substringAfterLast("/").substringBefore("?")
                            "https://youtu.be/$videoId"
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                    continuation.resume(result)
                }
            }
        })
    }


    suspend fun loadSeasonsForTranslation(movieId: Int, translationId: Int): List<Season> =
        suspendCoroutine { continuation ->
            val url = HttpUrl.Builder()
                .scheme(getScheme())
                .host(getHost())
                .addPathSegment("ajax")
                .addPathSegment("get_cdn_series")
                .addPathSegment("")
                .build()

            val request = okhttp3.Request.Builder()
                .url(url)
                .post(
                    FormBody.Builder()
                        .add("id", movieId.toString())
                        .add("translator_id", translationId.toString())
                        .add("favs", "0")
                        .add("action", "get_episodes")
                        .build()
                )
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val body = response.body?.string()
                        if (body == null) {
                            Log.e("API", "Empty response")
                            continuation.resume(emptyList())
                            return
                        }
                        val json = JSONObject(body)

                        if (!json.getBoolean("success")) {
                            Log.e("API", json.getString("message"))
                            continuation.resume(emptyList())
                            return
                        }

                        val soupSeasons = Jsoup.parse(json.getString("seasons"))
                        val soupEpisodes = Jsoup.parse(json.getString("episodes"))

                        val seasonsClasses = soupSeasons.select("li").map {
                            Season(it.attr("data-tab_id").toInt(), emptyList())
                        }

                        val episodesClasses = soupEpisodes.select("ul").map { ul ->
                            ul.select("li").map {
                                Episode(it.attr("data-episode_id").toInt())
                            }
                        }

                        seasonsClasses.forEachIndexed { index, season ->
                            season.episodes = episodesClasses[index]
                        }

                        continuation.resume(seasonsClasses)
                    }
                }
            })
        }

    private fun <T> product(a: Collection<T>?, r: Int): List<Collection<T>> {
        var result = Collections.nCopies<Collection<T>>(1, emptyList())
        for (pool in Collections.nCopies(r, LinkedHashSet<T>(a))) {
            val temp: MutableList<Collection<T>> = ArrayList()
            for (x in result) {
                for (y in pool) {
                    val z: MutableCollection<T> = ArrayList(x)
                    z.add(y)
                    temp.add(z)
                }
            }
            result = temp
        }
        return result
    }

    private fun clearTrash(data: String): String {
        val trashList = listOf("@", "#", "!", "^", "$")
        val trashCodesSet = mutableListOf<String>()

        for (i in 2..4) {
            val startchar = ""
            for (chars in product(trashList, i)) {
                val dataBytes = chars.joinToString(startchar).encodeToByteArray()
                val trashcombo = Base64.encodeToString(dataBytes, Base64.NO_WRAP)
                trashCodesSet.add(trashcombo)
            }
        }

        var trashString = data.replace("#h", "").split("//_//").joinToString("")
        for (i in trashCodesSet) {
            val temp = i.replace("\n", "") // Remove any newline characters from the Base64 string
            trashString = trashString.replace(temp, "")
        }

        val finalString = Base64.decode(trashString, Base64.NO_WRAP)
        return finalString.decodeToString()
    }


    suspend fun loadResolutions(
        movieId: Int,
        translationId: Int,
        season: Int? = null,
        episode: Int? = null
    ): List<Stream> = suspendCoroutine { continuation ->
        val form = FormBody.Builder()
            .add("id", movieId.toString())
            .add("translator_id", translationId.toString())

        if (season != null) {
            form.add("season", season.toString())
                .add("episode", episode.toString())
                .add("action", "get_stream")
        } else {
            form.add("is_camrip", "0")
                .add("is_ads", "0")
                .add("is_director", "0")
                .add("action", "get_movie")
        }

        val url = HttpUrl.Builder()
            .scheme(getScheme())
            .host(getHost())
            .addPathSegment("ajax")
            .addPathSegment("get_cdn_series")
            .addPathSegment("")
            .addQueryParameter("t", System.currentTimeMillis().toString())
            .build()

        val request = okhttp3.Request.Builder()
            .url(url)
            .post(
                form.build()
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API", "Empty response")
                        continuation.resume(emptyList())
                        return
                    }
                    val json = JSONObject(body)

                    if (!json.getBoolean("success")) {
                        Log.e("API", json.getString("message"))
                        continuation.resume(emptyList())
                        return
                    }

                    println("loadResolutions: $json")

                    val link = clearTrash(json.getString("url"))
                    val arr = link.split(",")

                    val subtitle = json.optString("subtitle", "")
                    if (subtitle.isEmpty() || subtitle.equals("false")) {
                        val result = arr.map {
                            val res = it.split("[")[1].split("]")[0]
                            val video = it.split("]")[1].split(" or ")[1]
                            Stream(video, res, emptyList())
                        }
                        continuation.resume(result)
                        return
                    }

                    val subtitleCodes = json.optJSONObject("subtitle_lns") ?: JSONObject()

                    val subtitles = subtitle.split(",").map {
                        val lang = it.substringAfter("[").substringBefore("]")
                        val subtitleUrl = it.substringAfter("]").substringAfter(" ")
                        val code = subtitleCodes.getString(lang)
                        Subtitle(subtitleUrl, code, lang)
                    }

                    val result = arr.map {
                        val res = it.substringAfter("[").substringBefore("]")
                        val video = it.substringAfter("]").substringAfter(" or ")
                        Stream(video, res, subtitles)
                    }
                    continuation.resume(result)
                }
            }
        })
    }

    suspend fun setActualMirror(): String = suspendCoroutine { continuation ->
        val request = okhttp3.Request.Builder()
            .get()
            .url("https://raw.githubusercontent.com/lrdcxdes/LFilms/master/mirror.txt")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    var body = response.body?.string()
                    if (body == null) {
                        Log.e("API", "Empty response")
                        continuation.resume("")
                        return
                    }
                    body = body.replace("\n", "")

                    val scheme = body.split("://")[0]
                    val host = body.split("://")[1]

                    setScheme(scheme)
                    setHost(host)
                    continuation.resume(body)
                }
            }
        })
    }

    fun setMirror(mirrorUrl: String): Boolean {
        return try {
            val scheme = mirrorUrl.split("://")[0]
            val host = mirrorUrl.split("://")[1].split("/")[0]

            val uri = URI.create(mirrorUrl)
            if (uri.scheme == null || uri.host == null) {
                return false
            }

            setScheme(scheme)
            setHost(host)

            true
        } catch (e: Exception) {
            false
        }
    }
}
