package dev.lrdcxdes.lfilms.helper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.lrdcxdes.lfilms.R
import dev.lrdcxdes.lfilms.api.MoviePreview

private const val IMAGE_ASPECT_RATIO = 498f / 739f


@Composable
fun MovieCard(movie: MoviePreview, onClick: () -> Unit) {
    val isClicked = remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = {
                    isClicked.value = true
                    onClick()
                }
            ),
        elevation = CardDefaults.cardElevation()
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .size(498, 739)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(IMAGE_ASPECT_RATIO)
                    .width(200.dp) // Set a fixed width for the image
                    .height(200.dp) // Set a fixed height for the image
                    .clip(shape = RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Movie Title
            Text(
                text = movie.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    lineHeight = 14.sp
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
            )

            // Movie Description
            Text(
                text = movie.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}