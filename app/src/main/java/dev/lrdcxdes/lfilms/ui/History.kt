package dev.lrdcxdes.lfilms.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import dev.lrdcxdes.lfilms.api.MoviePreview
import dev.lrdcxdes.lfilms.helper.MovieCard
import dev.lrdcxdes.lfilms.helper.NetworkError
import dev.lrdcxdes.lfilms.helper.encodePath
import dev.lrdcxdes.lfilms.helper.getHistoryMoviesPreview
import dev.lrdcxdes.lfilms.helper.performSearchFavorites
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistoryScreen(
    defaultHistoryList: List<MoviePreview>,
    navController: NavHostController,
    navBar: @Composable () -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var hintsState by remember { mutableStateOf(emptyList<String>()) }
    var moviesList by remember { mutableStateOf(defaultHistoryList) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var networkError by remember { mutableStateOf(false) }

    val resources = LocalContext.current.resources
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

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
                                try {
                                    moviesList = performSearchFavorites(textState.text, moviesList)
                                } catch (e: ApiError) {
                                    Log.e(
                                        "performSearchHistory(${textState.text})",
                                        "Network error: ${e.message}"
                                    )
                                    networkError = true
                                }
                            }
                        }),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filtered list of movies based on search query
                val filteredMovies =
                    if (textState.text.isNotEmpty() && moviesList.isNotEmpty()) {
                        moviesList
                    } else if (textState.text.isEmpty() && hintsState.isEmpty() && moviesList.isEmpty()) {
                        Text(
                            text = LocalContext.current.getString(R.string.no_movies_found),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                        emptyList()
                    } else if (textState.text.isEmpty() && hintsState.isEmpty() && moviesList.isNotEmpty()) {
                        moviesList
                    } else {
                        LaunchedEffect(Unit) {
                            try {
                                moviesList = getHistoryMoviesPreview(context)
                            } catch (e: ApiError) {
                                Log.e(
                                    "getHistoryMovies(context)",
                                    "Network error: ${e.message}"
                                )
                                networkError = true
                            }
                        }

                        emptyList()
                    }

                val gridState = rememberLazyGridState()

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(128.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(filteredMovies) { movie ->
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
                    moviesList = getHistoryMoviesPreview(context)
                } catch (e: ApiError) {
                    Log.e("getHistoryMovies(context)", "Network error: ${e.message}")
                    networkError = true
                }
            }
        }
    }
}