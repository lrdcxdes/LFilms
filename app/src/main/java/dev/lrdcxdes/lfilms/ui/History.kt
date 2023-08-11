package dev.lrdcxdes.lfilms.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.lrdcxdes.lfilms.api.ApiError
import dev.lrdcxdes.lfilms.api.MoviePreview
import dev.lrdcxdes.lfilms.helper.MovieCard
import dev.lrdcxdes.lfilms.helper.NetworkError
import dev.lrdcxdes.lfilms.helper.encodePath
import dev.lrdcxdes.lfilms.helper.getHistoryMoviesPreview
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    defaultHistoryList: List<MoviePreview>,
    navController: NavHostController,
    navBar: @Composable () -> Unit
) {
    var moviesList by remember { mutableStateOf(defaultHistoryList) }
    var networkError by remember { mutableStateOf(false) }
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
                // Filtered list of movies based on search query
                val filteredMovies =
                    moviesList.ifEmpty {
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