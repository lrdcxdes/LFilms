package dev.lrdcxdes.lfilms.api

data class MoviesList(val page: Int, val maxPage: Int, val movies: List<MoviePreview>)