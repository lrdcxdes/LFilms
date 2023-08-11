package dev.lrdcxdes.lfilms

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.lrdcxdes.lfilms.api.ApiError
import dev.lrdcxdes.lfilms.api.MoviePreview
import dev.lrdcxdes.lfilms.api.MoviesList
import dev.lrdcxdes.lfilms.helper.AutoUpdate
import dev.lrdcxdes.lfilms.helper.decodePath
import dev.lrdcxdes.lfilms.helper.defaultList
import dev.lrdcxdes.lfilms.helper.getFavoriteMovies
import dev.lrdcxdes.lfilms.helper.getHistoryMoviesPreview
import dev.lrdcxdes.lfilms.helper.getMirror
import dev.lrdcxdes.lfilms.helper.getTheme
import dev.lrdcxdes.lfilms.helper.setMirror
import dev.lrdcxdes.lfilms.helper.setTheme
import dev.lrdcxdes.lfilms.ui.BottomNavigationBar
import dev.lrdcxdes.lfilms.ui.FavoritesScreen
import dev.lrdcxdes.lfilms.ui.HistoryScreen
import dev.lrdcxdes.lfilms.ui.HomeScreen
import dev.lrdcxdes.lfilms.ui.MovieScreen
import dev.lrdcxdes.lfilms.ui.SettingsScreen
import dev.lrdcxdes.lfilms.ui.theme.LFilmsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MovieApp()
                }
            }
        }
    }
}


@Composable
fun MovieApp() {
    val favoriteMoviesListState = remember { mutableStateOf<List<MoviePreview>>(emptyList()) }
    val historyMoviesListState = remember { mutableStateOf<List<MoviePreview>>(emptyList()) }

    val currentTheme = remember { mutableStateOf(Theme.LIGHT) }
    val navController = rememberNavController()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val defaultMoviesListState = remember { mutableStateOf<MoviesList?>(null) }
    val searchMoviesListState = remember { mutableStateOf<MoviesList?>(null) }

    var currentMirror = api.getBaseUrl()
    var mirror by remember { mutableStateOf("") }

    var actualMirror = ""

    LaunchedEffect(Unit) {
        val tempTheme = withContext(Dispatchers.IO) { getTheme(context) }
        currentTheme.value = tempTheme

        val tempMirror = withContext(Dispatchers.IO) { getMirror(context) }
        try {
            withContext(Dispatchers.IO) {
                api.setActualMirror()
                actualMirror = api.getBaseUrl()
            }
        } catch (e: ApiError) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            defaultMoviesListState.value = MoviesList(1, 1, emptyList())
        }

        if (tempMirror.isEmpty()) {
            currentMirror = actualMirror
        } else {
            api.setMirror(tempMirror)
        }

        defaultMoviesListState.value = try {
            withContext(Dispatchers.IO) { defaultList(category = "watching") }
        } catch (e: ApiError) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            MoviesList(1, 1, emptyList())
        }

        favoriteMoviesListState.value = withContext(Dispatchers.IO) { getFavoriteMovies(context) }
        historyMoviesListState.value =
            withContext(Dispatchers.IO) { getHistoryMoviesPreview(context) }
    }

    LFilmsTheme(theme = currentTheme.value, dynamicColor = false) {
        if (defaultMoviesListState.value == null) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.secondary,
            )
        } else {
            AutoUpdate()

            NavHost(navController, startDestination = "home") {
                composable("home") {
                    val moviesList =
                        searchMoviesListState.value ?: defaultMoviesListState.value ?: MoviesList(
                            1,
                            1,
                            emptyList()
                        )
                    HomeScreen(navController, moviesList, onSearchResult = { searchResults ->
                        searchMoviesListState.value = searchResults
                    }) {
                        BottomNavigationBar(navController, onHomeClicked = {
                            searchMoviesListState.value = null
                            // reload home screen
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        })
                    }
                }
                composable("favorites") {
                    FavoritesScreen(navController, favoriteMoviesListState.value) {
                        BottomNavigationBar(
                            navController
                        )
                    }
                }
                composable("history") {
                    HistoryScreen(historyMoviesListState.value, navController) {
                        BottomNavigationBar(
                            navController
                        )
                    }
                }
                composable("settings") {
                    SettingsScreen(
                        navBar = { BottomNavigationBar(navController) },
                        currentTheme = currentTheme.value,
                        currentMirror = currentMirror,
                        context = context,
                        onThemeChanged = { newTheme ->
                            currentTheme.value = newTheme
                            scope.launch {
                                setTheme(newTheme, context)
                            }
                        },
                        onMirrorChanged = { newMirror ->
                            if (newMirror == currentMirror) {
                                Toast.makeText(
                                    context,
                                    "Mirror is already $newMirror",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@SettingsScreen
                            }
                            val success = api.setMirror(newMirror)
                            if (success) {
                                defaultMoviesListState.value = null
                                searchMoviesListState.value = null
                                mirror = newMirror
                                currentMirror = newMirror

                                scope.launch {
                                    setMirror(newMirror, context)
                                }

                                Toast.makeText(
                                    context,
                                    "Mirror changed to $newMirror",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            } else {
                                Log.e("API", "Mirror change failed")

                                Toast.makeText(
                                    context,
                                    "Mirror change failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onResetToActualMirror = {
                            if (actualMirror == currentMirror) {
                                Toast.makeText(
                                    context,
                                    "Mirror is already $currentMirror",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@SettingsScreen
                            }

                            defaultMoviesListState.value = null
                            searchMoviesListState.value = null
                            mirror = actualMirror
                            currentMirror = actualMirror

                            scope.launch {
                                setMirror(mirror, context)
                            }

                            Toast.makeText(
                                context,
                                "Mirror changed to $currentMirror",
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    "movie/{encodedPath}",
                    arguments = listOf(navArgument("encodedPath") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encodedPath = backStackEntry.arguments?.getString("encodedPath")!!
                    val path = decodePath(encodedPath)
                    MovieScreen(navController, path, onHistorySet = { historySet ->
                        historyMoviesListState.value = historySet
                    }, onFavoriteSet = { favoriteSet ->
                        favoriteMoviesListState.value = favoriteSet
                    })
                }
            }
        }
    }
}
