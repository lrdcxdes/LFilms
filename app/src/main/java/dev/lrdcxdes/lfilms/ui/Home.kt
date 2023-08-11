package dev.lrdcxdes.lfilms.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.lrdcxdes.lfilms.R
import dev.lrdcxdes.lfilms.api.ApiError
import dev.lrdcxdes.lfilms.api.MoviesList
import dev.lrdcxdes.lfilms.helper.MovieCard
import dev.lrdcxdes.lfilms.helper.NetworkError
import dev.lrdcxdes.lfilms.helper.defaultList
import dev.lrdcxdes.lfilms.helper.encodePath
import dev.lrdcxdes.lfilms.helper.getHints
import dev.lrdcxdes.lfilms.helper.loadNextPage
import dev.lrdcxdes.lfilms.helper.performSearch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    defaultMoviesList: MoviesList,
    onSearchResult: (MoviesList) -> Unit,
    navBar: @Composable () -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var hintsState by remember { mutableStateOf(emptyList<String>()) }
    var moviesList by remember { mutableStateOf(defaultMoviesList) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var networkError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Внешняя функция для обновления списка фильмов по текстовому запросу
    suspend fun updateMoviesList(query: String) {
        try {
            val newMoviesList = performSearch(query)
            moviesList = newMoviesList
            onSearchResult(newMoviesList)
        } catch (e: ApiError) {
            Log.e("performSearch($query)", "Network error: ${e.message}")
            networkError = true
        }
    }

    // Обновление подсказок при изменении текста в поисковом поле
    LaunchedEffect(textState) {
        scope.launch {
            try {
                hintsState = getHints(textState.text)
            } catch (e: ApiError) {
                Log.e("getHints()", "Network error: ${e.message}")
                networkError = true
            }
        }
    }

    val resources = LocalContext.current.resources

    Scaffold(
        bottomBar = { navBar() }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
                        label = {
                            Text(
                                resources.getString(R.string.search_label),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            hintsState = emptyList()
                            scope.launch {
                                updateMoviesList(textState.text)
                            }
                        }),
                    )
                }

                // Show hints below the search bar
                if (hintsState.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hintsState) { hint ->
                            Text(
                                hint,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        textState = TextFieldValue(hint)
                                        hintsState = emptyList()
                                        scope.launch {
                                            try {
                                                moviesList = performSearch(hint)
                                                onSearchResult(moviesList)
                                            } catch (e: ApiError) {
                                                Log.e(
                                                    "performSearch($hint)",
                                                    "Network error: ${e.message}"
                                                )
                                                networkError = true
                                            }
                                        }
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Filtered list of movies based on search query
                val filteredMovies =
                    if (textState.text.isNotEmpty() && moviesList.movies.isNotEmpty()) {
                        moviesList
                    } else if (textState.text.isEmpty() && hintsState.isEmpty() && moviesList.movies.isEmpty()) {
                        Text(
                            text = LocalContext.current.getString(R.string.no_movies_found),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                        MoviesList(1, 1, emptyList())
                    } else if (textState.text.isEmpty() && hintsState.isEmpty() && moviesList.movies.isNotEmpty()) {
                        moviesList
                    } else {
                        LaunchedEffect(Unit) {
                            scope.launch {
                                try {
                                    moviesList = defaultList()
                                } catch (e: ApiError) {
                                    Log.e("defaultList()", "Network error: ${e.message}")
                                    networkError = true
                                }
                            }
                        }

                        MoviesList(1, 1, emptyList())
                    }

                val gridState = rememberLazyGridState()

                LaunchedEffect(gridState) {
                    snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
                        .debounce(100) // Throttle collection to every 100 milliseconds
                        .collect { visibleItems ->
                            val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: 0
                            val totalItems = moviesList.movies.size
                            if (lastVisibleItemIndex >= totalItems - 1) {
                                // Load the next page if the last visible item is at the end of the list
                                if (moviesList.page < moviesList.maxPage) {
                                    // Load the next page of movies
                                    val nextPage = moviesList.page + 1
                                    try {
                                        val nextPageMovies =
                                            loadNextPage(nextPage, textState.text)
                                        moviesList = moviesList.copy(
                                            page = nextPage,
                                            movies = moviesList.movies + nextPageMovies.movies
                                        )
                                    } catch (e: ApiError) {
                                        Log.e(
                                            "loadNextPage($nextPage, ${textState.text})",
                                            "Network error: ${e.message}"
                                        )
                                        networkError = true
                                    }
                                }
                            }
                        }
                }

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(128.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(filteredMovies.movies) { movie ->
                        MovieCard(movie = movie) {
                            val encodedPath = encodePath(movie.path)
                            navController.navigate("movie/$encodedPath")
                        }
                    }
                }
            }

        }
    }

    if (networkError) {
        NetworkError {
            networkError = false
            scope.launch {
                try {
                    moviesList = defaultList()
                } catch (e: ApiError) {
                    Log.e("defaultList()", "Network error: ${e.message}")
                    networkError = true
                }
            }
        }
    }
}
