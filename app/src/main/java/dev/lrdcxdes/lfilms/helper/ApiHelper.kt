package dev.lrdcxdes.lfilms.helper

import dev.lrdcxdes.lfilms.api
import dev.lrdcxdes.lfilms.api.MoviesList

suspend fun getHints(query: String): List<String> {
    if (query.isEmpty()) return emptyList()
    val result = try {
        api.searchAjax(query).map { it.title }
    } catch (e: Exception) {
        emptyList()
    }
    return if (query.lowercase() in result.map { it.lowercase() }) emptyList() else result
}


suspend fun performSearch(query: String, page: Int = 1): MoviesList {
    val searchResult = try {
        api.search(query = query, page = page)
    } catch (e: Exception) {
        MoviesList(1, 1, emptyList())
    }

    return searchResult
}


suspend fun defaultList(page: Int = 1): MoviesList {
    return api.watching(page)
}

suspend fun loadNextPage(page: Int, query: String): MoviesList {
    return if (query.isEmpty()) {
        api.watching(page)
    } else {
        api.search(query = query, page = page)
    }
}
