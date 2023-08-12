package dev.lrdcxdes.lfilms.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.lrdcxdes.lfilms.R
import dev.lrdcxdes.lfilms.api
import dev.lrdcxdes.lfilms.api.ApiError
import dev.lrdcxdes.lfilms.api.Episode
import dev.lrdcxdes.lfilms.api.Movie
import dev.lrdcxdes.lfilms.api.MoviePreview
import dev.lrdcxdes.lfilms.api.Season
import dev.lrdcxdes.lfilms.api.Stream
import dev.lrdcxdes.lfilms.api.Translation
import dev.lrdcxdes.lfilms.helper.EpisodeHistory
import dev.lrdcxdes.lfilms.helper.getHistoryMovie
import dev.lrdcxdes.lfilms.helper.inFavorites
import dev.lrdcxdes.lfilms.helper.setFavorite
import dev.lrdcxdes.lfilms.helper.setHistory
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@Composable
private fun fetchTrailerUrl(movie: Movie?): String {
    val scope = rememberCoroutineScope()
    val state = remember { mutableStateOf("") }

    // Fetch trailer URL asynchronously
    LaunchedEffect(movie) {
        if (movie != null) {
            scope.launch {
                try {
                    val url = api.getTrailer(movie.id) ?: ""
                    state.value = url
                } catch (e: Exception) {
                    state.value = ""
                }
            }
        }
    }

    return state.value
}


@Composable
fun MovieScreen(
    navController: NavHostController,
    path: String,
    onFavoriteSet: (List<MoviePreview>) -> Unit,
    onHistorySet: (List<MoviePreview>) -> Unit
) {
    // Favorite
    val isFavorite = remember { mutableStateOf(false) }

    val context = LocalContext.current

    val showSheet = remember { mutableStateOf(false) }

    fun showSeasonEpisodeSheet() {
        showSheet.value = true
    }

    val movieS = remember { mutableStateOf<Movie?>(null) }

    val trailerUrl = fetchTrailerUrl(movieS.value)

    val scope = rememberCoroutineScope()

    // Fetch movie details asynchronously
    LaunchedEffect(Unit) {
        scope.launch {
            val loadedMovie = try {
                api.getMovie(path)
            } catch (e: ApiError) {
                null
            }
            movieS.value = loadedMovie
            if (loadedMovie != null) {
                isFavorite.value = inFavorites(context, loadedMovie.path)
            }
        }
    }

    if (movieS.value == null) {
        // Show a circular progress bar while loading
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        }
    } else {
        val movie = movieS.value!!

        // Fetch movie image URL from the 'movie' object
        val imageUrl = movie.imageUrl

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // button arrow left (back to home) in the left top corner
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .padding(16.dp)
                                .padding(top = 24.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Unspecified)
                                .align(Alignment.TopStart)
                                .zIndex(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        val model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .placeholder(R.drawable.ic_placeholder)
                            .size(498, 739)
                            .crossfade(true)
                            .build()

                        SubcomposeAsyncImage(
                            model = model,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .blur(24.dp)
                                // плавный градиент с картинки в чёрный цвет внизу
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black
                                            ),
                                            startY = size.height * 0.3f,
                                            endY = size.height * 0.85f
                                        ),
                                    )
                                },
                            filterQuality = FilterQuality.Medium
                        )

                        SubcomposeAsyncImage(
                            model = model,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth(0.45f)
                                .aspectRatio(498f / 739f)
                                .align(Alignment.CenterStart)
                                .padding(start = 24.dp, bottom = 24.dp)
                                .clip(shape = RoundedCornerShape(16.dp)),
                            filterQuality = FilterQuality.Medium
                        )

                        // after image at the right, make movie.title, and under movie.title make originalTitle
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .padding(start = 18.dp, top = 12.dp, end = 24.dp, bottom = 12.dp)
                                .align(Alignment.CenterEnd)
                        ) {
                            Text(
                                text = if (LocalContext.current.resources.configuration.locales[0].language == "ru") {
                                    movie.title
                                } else {
                                    movie.originalTitle
                                },
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                            )
                            Text(
                                text = if (LocalContext.current.resources.configuration.locales[0].language == "ru") {
                                    movie.originalTitle
                                } else {
                                    movie.title
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.Gray,
                            )
                        }
                        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // rating
                                        Text(
                                            text = movie.ratingIMDB.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            textAlign = TextAlign.Center,
                                            color = movie.ratingIMDB.getColor(),
                                            modifier = Modifier
                                                .padding(start = 4.dp)
                                                .clip(shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp)
                                        )

                                        // votes
                                        Text(
                                            text = "(${movie.votesIMDB})",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 12.sp,
                                            ),
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }

                                    // imdb text
                                    Text(
                                        text = LocalContext.current.getString(R.string.imdb),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .padding(start = 4.dp, top = 8.dp)
                                            .align(
                                                Alignment.CenterHorizontally
                                            ),
                                        textAlign = TextAlign.Center,
                                    )
                                }

                                Column {
                                    // Kinopoisk rating
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // rating
                                        Text(
                                            text = movie.kinopoiskRating.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            textAlign = TextAlign.Center,
                                            color = movie.kinopoiskRating.getColor(),
                                            modifier = Modifier
                                                .padding(start = 4.dp)
                                                .clip(shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp)
                                        )

                                        // votes
                                        Text(
                                            text = "(${movie.kinopoiskVotes})",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 12.sp,
                                            ),
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }

                                    // kinopoisk text
                                    Text(
                                        text = LocalContext.current.getString(R.string.kinopoisk),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .padding(start = 4.dp, top = 8.dp)
                                            .align(
                                                Alignment.CenterHorizontally
                                            ),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Duration
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = movie.duration.toString(LocalContext.current.resources.configuration.locales[0].language),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }

                                    // Duration text
                                    Text(
                                        text = LocalContext.current.getString(R.string.duration),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .padding(start = 4.dp, top = 8.dp)
                                            .align(
                                                Alignment.CenterHorizontally
                                            ),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Age
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = movie.ageRating,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            textAlign = TextAlign.Center,
                                            color = Color.White,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }

                                    // Age text
                                    Text(
                                        text = LocalContext.current.getString(R.string.age),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .padding(start = 4.dp, top = 8.dp)
                                            .align(
                                                Alignment.CenterHorizontally
                                            ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            ) {
                                // Add Trailer button with icon play when trailer url loaded
                                Button(
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(trailerUrl)
                                        )
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f) // Occupy equal space
                                        .padding(end = 8.dp), // Add spacing between buttons
                                    contentPadding = PaddingValues(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (trailerUrl != "") {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = LocalContext.current.getString(R.string.trailer),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        } else {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }

                                // Add Watch button with icon play when translations loaded
                                Button(
                                    onClick = {
                                        showSeasonEpisodeSheet()
                                    },
                                    modifier = Modifier
                                        .weight(1f) // Occupy equal space
                                        .padding(end = 8.dp), // Add spacing between buttons
                                    enabled = !movie.isComingSoon && !movie.isRestricted,
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = Color.Gray,
                                        disabledContentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            if (movie.isRestricted) {
                                                Icon(
                                                    imageVector = Icons.Filled.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else if (movie.isComingSoon) {
                                                Icon(
                                                    imageVector = Icons.Filled.DateRange,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = LocalContext.current.getString(R.string.watch),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }

                                // Add Favorite button with icon
                                Button(
                                    onClick = {
                                        isFavorite.value = !isFavorite.value
                                        scope.launch {
                                            onFavoriteSet(
                                                setFavorite(
                                                    movie.preview(),
                                                    isFavorite.value,
                                                    context
                                                )
                                            )
                                        }
                                    },

                                    ) {
                                    Icon(
                                        imageVector = if (isFavorite.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Movie INFO

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = 4.dp)
                    ) {
                        if (movie.slogan.isNotEmpty()) {
                            // Movie Slogan
                            Row {
                                Text(
                                    text = "${LocalContext.current.getString(R.string.slogan)}: ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary
                                    ),
                                )
                                Text(
                                    text = movie.slogan,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }

                        // Movie Release Data
                        Row {
                            Text(
                                text = "${LocalContext.current.getString(R.string.release_date)}: ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimary
                                ),
                            )
                            Text(
                                text = movie.releaseDate.toString(LocalContext.current.resources.configuration.locales[0].language),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray
                                )
                            )
                        }

                        // Movie Countries
                        if (movie.countries.isNotEmpty()) {
                            Row {
                                Text(
                                    text = "${LocalContext.current.getString(R.string.countries)}: ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary
                                    ),
                                )
                                Text(
                                    text = movie.countries.joinToString(separator = ", "),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }

                        // Movie Directors
                        if (movie.directors.isNotEmpty()) {
                            Row {
                                Text(
                                    text = "${LocalContext.current.getString(R.string.directors)}: ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary
                                    ),
                                )
                                Text(
                                    text = movie.directors.joinToString(separator = ", "),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }

                        // Movie Genres
                        if (movie.genres.isNotEmpty()) {
                            Row {
                                Text(
                                    text = "${LocalContext.current.getString(R.string.genres)}: ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary
                                    ),
                                )
                                Text(
                                    text = movie.getGenresString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }

                        // Movie from series
                        if (movie.series.isNotEmpty()) {
                            Row {
                                Text(
                                    text = "${LocalContext.current.getString(R.string.from_series)}: ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary
                                    ),
                                )
                                Text(
                                    text = movie.series.joinToString(separator = ", "),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }

                        // Movie actors
                        if (movie.actors.isNotEmpty()) {
                            Row {
                                Text(
                                    text = "${LocalContext.current.getString(R.string.actors)}: ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary
                                    ),
                                )
                                Text(
                                    text = movie.actors.joinToString(separator = ", "),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        // Show the bottom sheet
        WatchBottomSheet(
            showSheet = showSheet.value,
            onDismissSheet = { showSheet.value = false },
            movie = movie,
            onHistorySet = onHistorySet,
        )
    }
}


@Composable
fun WatchBottomSheetContentSerial(
    movie: Movie,
    selectedTranslation: Translation,
    onTranslationSelected: (Translation) -> Unit,
    selectedSeason: Season?,
    onSeasonSelected: (Season) -> Unit,
    selectedEpisode: Episode?,
    onEpisodeSelected: (Episode) -> Unit,
    watchedEpisodes: List<EpisodeHistory>,
    onCloseSheet: () -> Unit,
    onWatchClicked: () -> Unit,
    selectedResolution: Stream?,
    onResolutionSelected: (Stream) -> Unit,
) {
    var showDialog by remember { mutableStateOf<String?>(null) }
    var seasons by remember { mutableStateOf(listOf<Season>()) }
    var resolutions by remember { mutableStateOf(listOf<Stream>()) }

    val seasonLocale = LocalContext.current.getString(R.string.season)
    val episodeLocale = LocalContext.current.getString(R.string.episode)

    val scope = rememberCoroutineScope()

    // Function to load resolutions for the current translation, season, and episode
    suspend fun loadResolutions() {
        // Implement the API call to load resolutions for the selected translation, season, and episode
        // For example:
        scope.launch {
            resolutions = api.loadResolutions(
                movie.id,
                selectedTranslation.id,
                selectedSeason?.id ?: 1,
                selectedEpisode?.id ?: 1
            )

            // Once the resolutions are loaded, update the selectedResolution state with the first resolution (if any)
            if (resolutions.isNotEmpty()) {
                onResolutionSelected(resolutions.first())
            }
        }
    }

    // Function to load seasons for the selected translation
    LaunchedEffect(selectedTranslation) {
        selectedSeason?.let {
            onSeasonSelected(it)
        }
        scope.launch {
            seasons = api.loadSeasonsForTranslation(movie.id, selectedTranslation.id)
        }
    }

    // Function to load episodes for the selected season
    LaunchedEffect(selectedSeason, selectedTranslation) {
        selectedEpisode?.let {
            onEpisodeSelected(it)
        }
    }

    // Function to load resolutions for the selected translation, season, and episode
    LaunchedEffect(selectedSeason, selectedEpisode, selectedTranslation) {
        loadResolutions()
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Translation dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Toggle the menu visibility
                    showDialog = "translation"
                }
        ) {
            OutlinedTextField(
                value = selectedTranslation.name,
                onValueChange = { /* Implement if needed */ },
                label = { Text(LocalContext.current.getString(R.string.translation)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                    disabledLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        // Season dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Show the dialog for selecting season
                    if (selectedTranslation.id != -1) {
                        showDialog = "season"
                    }
                }
        ) {
            OutlinedTextField(
                value = "$seasonLocale ${selectedSeason?.id ?: "?"}",
                onValueChange = { /* Implement if needed */ },
                label = { Text(LocalContext.current.getString(R.string.season)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                    disabledLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        // Episode dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Show the dialog for selecting episode
                    if (selectedSeason != null) {
                        showDialog = "episode"
                    }
                }
        ) {
            OutlinedTextField(
                value = "$episodeLocale ${selectedEpisode?.id ?: "?"}",
                onValueChange = { /* Implement if needed */ },
                label = { Text(LocalContext.current.getString(R.string.episode)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                    disabledLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        // Resolution buttons in row with scrollable at horizontal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Show the resolution buttons
            for (resolution in resolutions) {
                Button(
                    onClick = { onResolutionSelected(resolution) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (resolution == selectedResolution) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (resolution == selectedResolution) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                ) {
                    Text(resolution.quality)
                }
            }
        }

        // Watch button
        Button(
            onClick = { onWatchClicked() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = LocalContext.current.getString(R.string.watch),
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(LocalContext.current.getString(R.string.watch))
        }

        // Close button
        Button(
            onClick = onCloseSheet,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(LocalContext.current.getString(R.string.close))
        }

        if (showDialog != null) {
            // Show the corresponding dialog based on the showDialog value
            when (showDialog) {
                "season" -> {
                    // Show the season dialog
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = null
                        },
                        confirmButton = {
                            // Implement the confirm button for the season dialog
                            Button(
                                onClick = {
                                    // Close the dialog and load episodes for the selected season
                                    showDialog = null
                                    // Load episodes for the selected season using the API
                                    onSeasonSelected(seasons.first())
                                }
                            ) {
                                Text(LocalContext.current.getString(R.string.confirm))
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        title = {
                            Text(LocalContext.current.getString(R.string.select_season))
                        },
                        text = {
                            // Implement the list of seasons in the dialog
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                if (selectedSeason != null) {
                                    items(seasons) { season ->
                                        Text(
                                            text = "$seasonLocale ${season.id}",
                                            modifier = Modifier
                                                .clickable {
                                                    // Select the season and update the selectedSeason
                                                    onSeasonSelected(season)
                                                    showDialog = null
                                                }
                                                .padding(16.dp)
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        },
                        dismissButton = {
                            // Implement the dismiss button for the season dialog
                            Button(
                                onClick = {
                                    // Close the dialog
                                    showDialog = null
                                }
                            ) {
                                Text(LocalContext.current.getString(R.string.dismiss))
                            }
                        }
                    )
                }

                "episode" -> {
                    // Show the episode dialog
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = null
                        },
                        confirmButton = {
                            // Implement the confirm button for the episode dialog
                            Button(
                                onClick = {
                                    // Close the dialog and update the selectedEpisode
                                    showDialog = null
                                    if (selectedEpisode != null) {
                                        onEpisodeSelected(selectedEpisode)
                                    }
                                }
                            ) {
                                Text(LocalContext.current.getString(R.string.confirm))
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        title = {
                            Text(LocalContext.current.getString(R.string.select_episode))
                        },
                        text = {
                            // Implement the list of episodes in the dialog
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                if (selectedSeason != null) {
                                    items(selectedSeason.episodes) { episode ->
                                        val isEpisodeWatched =
                                            watchedEpisodes.indexOfFirst { it.episode == episode.id && it.season == selectedSeason.id } != -1
                                        val color = if (episode == selectedEpisode) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onPrimary
                                        }

                                        Row(
                                            modifier = Modifier
                                                .clickable {
                                                    // Select the episode and update the selectedEpisode
                                                    onEpisodeSelected(episode)
                                                    showDialog = null
                                                }
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Show the episode name
                                            Text(
                                                text = "$episodeLocale ${episode.id}",
                                                modifier = Modifier.weight(1f),
                                                color = color
                                            )

                                            // Show the watched icon
                                            if (isEpisodeWatched) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Watched",
                                                    tint = color
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        dismissButton = {
                            // Implement the dismiss button for the episode dialog
                            Button(
                                onClick = {
                                    // Close the dialog
                                    showDialog = null
                                }
                            ) {
                                Text(LocalContext.current.getString(R.string.dismiss))
                            }
                        }
                    )
                }

                "translation" -> {
                    // Show the translation dialog
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = null
                        },
                        confirmButton = {
                            // Implement the confirm button for the translation dialog
                            Button(
                                onClick = {
                                    // Close the dialog and update the selectedTranslation
                                    showDialog = null
                                    onTranslationSelected(selectedTranslation)
                                }
                            ) {
                                Text(LocalContext.current.getString(R.string.confirm))
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        title = {
                            Text(LocalContext.current.getString(R.string.select_translation))
                        },
                        text = {
                            // Implement the list of translations in the dialog
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(movie.translations) { translation ->
                                    Text(
                                        text = translation.name,
                                        modifier = Modifier
                                            .clickable {
                                                // Select the translation and update the selectedTranslation
                                                onTranslationSelected(translation)
                                                showDialog = null
                                            }
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        },
                        dismissButton = {
                            // Implement the dismiss button for the translation dialog
                            Button(
                                onClick = {
                                    // Close the dialog
                                    showDialog = null
                                }
                            ) {
                                Text(LocalContext.current.getString(R.string.dismiss))
                            }
                        }
                    )
                }
            }
        }

    }
}


@Composable
fun WatchBottomSheetContent(
    movie: Movie,
    selectedTranslation: Translation,
    onTranslationSelected: (Translation) -> Unit,
    onCloseSheet: () -> Unit,
    onWatchClicked: () -> Unit,
    selectedResolution: Stream?,
    onResolutionSelected: (Stream) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    var resolutions by remember { mutableStateOf(listOf<Stream>()) }

    // Function to load resolutions for the current translation, season, and episode
    suspend fun loadResolutions() {
        // Implement the API call to load resolutions for the selected translation, season, and episode
        // For example:
        resolutions = api.loadResolutions(
            movie.id,
            selectedTranslation.id,
            null,
            null
        )

        // Once the resolutions are loaded, update the selectedResolution state with the first resolution (if any)
        if (resolutions.isNotEmpty()) {
            onResolutionSelected(resolutions.first())
        }
    }

    LaunchedEffect(selectedTranslation) {
        // Load resolutions when the selectedTranslation changes
        loadResolutions()
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Translation dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Toggle the menu visibility
                    showDialog = true
                }
        ) {
            OutlinedTextField(
                value = selectedTranslation.name,
                onValueChange = { /* Implement if needed */ },
                label = { Text(LocalContext.current.getString(R.string.translation)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                    disabledLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        // Resolution buttons in row with scrollable at horizontal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Show the resolution buttons
            for (resolution in resolutions) {
                Button(
                    onClick = { onResolutionSelected(resolution) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (resolution == selectedResolution) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (resolution == selectedResolution) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                ) {
                    Text(resolution.quality)
                }
            }
        }

        // Watch button
        Button(
            onClick = { onWatchClicked() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = LocalContext.current.getString(R.string.watch),
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(LocalContext.current.getString(R.string.watch))
        }

        // Close button
        Button(
            onClick = onCloseSheet,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(LocalContext.current.getString(R.string.close))
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                confirmButton = {
                    // Implement the confirm button for the translation dialog
                    Button(
                        onClick = {
                            // Close the dialog and update the selectedTranslation
                            showDialog = false
                            onTranslationSelected(selectedTranslation)
                        }
                    ) {
                        Text(LocalContext.current.getString(R.string.confirm))
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                title = {
                    Text(LocalContext.current.getString(R.string.select_translation))
                },
                text = {
                    // Implement the list of translations in the dialog
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(movie.translations) { translation ->
                            Text(
                                text = translation.name,
                                modifier = Modifier
                                    .clickable {
                                        // Select the translation and update the selectedTranslation
                                        onTranslationSelected(translation)
                                        showDialog = false
                                    }
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                },
                dismissButton = {
                    // Implement the dismiss button for the translation dialog
                    Button(
                        onClick = {
                            // Close the dialog
                            showDialog = false
                        }
                    ) {
                        Text(LocalContext.current.getString(R.string.dismiss))
                    }
                }
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchBottomSheet(
    showSheet: Boolean,
    onDismissSheet: () -> Unit,
    movie: Movie,
    onHistorySet: (List<MoviePreview>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val selectedResolution = remember { mutableStateOf<Stream?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Remember the selected translation, season, and episode states
    val selectedTranslation = remember { mutableStateOf(movie.translations.firstOrNull()) }
    val watchedEpisodes = remember { mutableStateOf(listOf<EpisodeHistory>()) }

    if (movie.isSerial) {
        val selectedSeason = remember { mutableStateOf<Season?>(null) }
        val selectedEpisode = remember { mutableStateOf<Episode?>(null) }
        val historyEpisode = remember { mutableStateOf<Episode?>(null) }
        val seasons = remember { mutableStateOf(listOf<Season>()) }

        // Function to load seasons for the selected translation
        suspend fun loadSeasonsForSelectedTranslation(translation: Translation): Job {
            selectedSeason.value = null // Reset selected season when translation changes
            // Load episodes for the first season of the selected translation
            return scope.launch {
                seasons.value = api.loadSeasonsForTranslation(movie.id, translation.id)
                if (seasons.value.isNotEmpty()) {
                    selectedSeason.value = seasons.value.first()
                    if (selectedSeason.value!!.episodes.isNotEmpty()) {
                        selectedEpisode.value = selectedSeason.value!!.episodes.first()
                        println("[loadSeasonsForSelectedTranslation] selectedEpisode.value: $selectedEpisode")
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            // Load seasons for the selected translation
            loadSeasonsForSelectedTranslation(
                selectedTranslation.value ?: movie.translations.first()
            ).join()

            scope.launch {
                getHistoryMovie(movie.path, context)?.let { historyMovie ->
                    val season = seasons.value.firstOrNull { season ->
                        season.id == historyMovie.season
                    }
                    val episodes = season?.episodes ?: emptyList()

                    selectedSeason.value = season

                    selectedEpisode.value = episodes.firstOrNull { episode ->
                        episode.id == historyMovie.episode
                    }

                    println("[getHistoryMovie] selectedEpisode.value: $selectedEpisode")

                    historyEpisode.value = selectedEpisode.value

                    watchedEpisodes.value = historyMovie.episodesWatched.toList()

                    selectedTranslation.value = movie.translations.firstOrNull { translation ->
                        translation.id == historyMovie.translation
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                sheetState = sheetState,
                content = {
                    // Pass the states and update functions to the WatchBottomSheetContent composable
                    WatchBottomSheetContentSerial(
                        movie = movie,
                        selectedTranslation = selectedTranslation.value
                            ?: movie.translations.first(),
                        onTranslationSelected = { translation ->
                            scope.launch {
                                selectedTranslation.value = translation
                            }
                        },
                        selectedSeason = selectedSeason.value,
                        onSeasonSelected = { season ->
                            scope.launch {
                                selectedSeason.value = season
                                selectedEpisode.value =
                                    if (season.episodes.contains(historyEpisode.value)) {
                                        historyEpisode.value
                                    } else {
                                        season.episodes.firstOrNull()
                                    }
                                println("[onSeasonSelected] selectedEpisode.value: $selectedEpisode, and historyEpisode.value: $historyEpisode")
                            }
                        },
                        selectedEpisode = selectedEpisode.value,
                        onEpisodeSelected = { episode ->
                            selectedEpisode.value = episode
                            // Handle episode selection if needed
                        },
                        watchedEpisodes = watchedEpisodes.value,
                        onCloseSheet = onDismissSheet,
                        onWatchClicked = {
                            if (selectedResolution.value == null) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.select_resolution),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@WatchBottomSheetContentSerial
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(
                                    Uri.parse(selectedResolution.value!!.url),
                                    "video/mp4"
                                )
                                if (selectedResolution.value!!.subtitles.isNotEmpty()) {
                                    // Pass the subtitles to the activity (Google Images, XPlore MediaView, MI Video Player, ExoPlayer)

                                    // ExoPlayer
                                    putExtra(
                                        "subtitle_uri",
                                        selectedResolution.value!!.subtitles.first().url
                                    )
                                    putExtra(
                                        "subtitle",
                                        selectedResolution.value!!.subtitles.first().url
                                    )
                                    putExtra(
                                        "subtitle_url",
                                        selectedResolution.value!!.subtitles.first().url
                                    )
                                    putExtra(
                                        "subtitle_language",
                                        selectedResolution.value!!.subtitles.first().lang
                                    )
                                    putExtra(
                                        "subtitle_label",
                                        selectedResolution.value!!.subtitles.first().name
                                    )
                                    putExtra(
                                        "subtitle_lang",
                                        selectedResolution.value!!.subtitles.first().lang
                                    )

                                    // MI Video Player
                                    val subtitleList =
                                        selectedResolution.value!!.subtitles.map { subtitle ->
                                            subtitle.url
                                        }
                                    putStringArrayListExtra(
                                        "subtitles",
                                        ArrayList(subtitleList)
                                    )
                                }
                            }
                            scope.launch {
                                onHistorySet(
                                    setHistory(
                                        movie.preview(),
                                        selectedSeason.value?.id,
                                        selectedEpisode.value?.id,
                                        selectedTranslation.value!!.id, context
                                    )
                                )
                            }
                            context.startActivity(intent)
                        },
                        selectedResolution = selectedResolution.value,
                        onResolutionSelected = { resolution ->
                            selectedResolution.value = resolution
                        },
                    )
                },
                scrimColor = Color.Black.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onDismissRequest = onDismissSheet
            )
        }
    } else {
        if (showSheet) {
            LaunchedEffect(Unit) {
                scope.launch {
                    getHistoryMovie(movie.path, context)?.let { historyMovie ->
                        // selectedTranslation from movie.translations by id
                        selectedTranslation.value = movie.translations.firstOrNull { translation ->
                            translation.id == historyMovie.translation
                        }
                    }
                }
            }

            // Only translation
            ModalBottomSheet(
                sheetState = sheetState,
                content = {
                    // Pass the states and update functions to the WatchBottomSheetContent composable
                    WatchBottomSheetContent(
                        movie = movie,
                        selectedTranslation = selectedTranslation.value
                            ?: movie.translations.first(),
                        onTranslationSelected = { translation ->
                            scope.launch {
                                selectedTranslation.value = translation
                            }
                        },
                        onCloseSheet = onDismissSheet,
                        onWatchClicked = {
                            if (selectedResolution.value == null) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.select_resolution),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@WatchBottomSheetContent
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(
                                    Uri.parse(selectedResolution.value!!.url),
                                    "video/mp4"
                                )
                                if (selectedResolution.value!!.subtitles.isNotEmpty()) {
                                    // Pass the subtitles to the activity (Google Images, XPlore MediaView, MI Video Player, ExoPlayer)

                                    // ExoPlayer
                                    putExtra(
                                        "subtitle_uri",
                                        selectedResolution.value!!.subtitles.first().url
                                    )
                                    putExtra(
                                        "subtitle",
                                        selectedResolution.value!!.subtitles.first().url
                                    )
                                    putExtra(
                                        "subtitle_url",
                                        selectedResolution.value!!.subtitles.first().url
                                    )
                                    putExtra(
                                        "subtitle_language",
                                        selectedResolution.value!!.subtitles.first().lang
                                    )
                                    putExtra(
                                        "subtitle_label",
                                        selectedResolution.value!!.subtitles.first().name
                                    )
                                    putExtra(
                                        "subtitle_lang",
                                        selectedResolution.value!!.subtitles.first().lang
                                    )

                                    // MI Video Player
                                    val subtitleList =
                                        selectedResolution.value!!.subtitles.map { subtitle ->
                                            subtitle.url
                                        }
                                    putStringArrayListExtra(
                                        "subtitles",
                                        ArrayList(subtitleList)
                                    )
                                }
                            }
                            context.startActivity(intent)
                        },
                        selectedResolution = selectedResolution.value,
                        onResolutionSelected = { resolution ->
                            selectedResolution.value = resolution
                        },
                    )
                },
                scrimColor = Color.Black.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onDismissRequest = onDismissSheet
            )
        }
    }
}