package com.example.cantona

import android.content.Context
import android.icu.text.DecimalFormat
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.floor

enum class TimeUnit{
    ms
}
@Composable
fun AlbumsScreen(albumMap: Map<Long, Album>, navController: NavController, viewModel: CantonaViewModel, modifier: Modifier = Modifier){
    Box() {
        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 200.dp), modifier = modifier) {
            items(albumMap.values.toList()) {
                AlbumBlock(
                    album = it, onClick = {
                        navController.navigate("${Screen.Songs.name}/${it.albumId}")
                    }
                )
            }
        }
        PlayButton(viewModel, navController, modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding().fillMaxWidth().background(MaterialTheme.colorScheme.surface).fillMaxHeight(.05f))
    }
}
@Composable
fun SongsScreen(album: Album, viewModel: CantonaViewModel, modifier: Modifier = Modifier){
    Box(modifier=modifier.fillMaxWidth()) {
        Column(modifier = Modifier
            .fillMaxWidth(0.8f)
            .align(Alignment.Center)) {
            AlbumArt(album.albumId, modifier = Modifier.aspectRatio(1f))
            Text(
                text = album.title, color = MaterialTheme.colorScheme.onSurface,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier,
            )
            Text(
                text = album.artist,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
            )
            if (album.year != null) {
                Text(
                    text = album.year.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            LazyColumn(modifier = modifier) {
                items(album.songs) { it ->
                    SongDisplay(
                        song = it, viewModel = viewModel
                    )
                }
            }
        }
    }
}
@Preview
@Composable
fun SongsScreenPreview(album: Album = Album(0, listOf(Song(title="Naked in Manhattan", artist="Chappell Roan")))){
    val context: Context = ApplicationProvider.getApplicationContext()
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    SongsScreen(album,  CantonaViewModel(exoPlayer))
}
fun durationString(time: Double, unit: TimeUnit = TimeUnit.ms): String {
    // Right now this only works for minutes
    if(unit == TimeUnit.ms) {
        val minutes: Double = floor(time / 60000)
        val seconds: Double = floor((time % 60000) / 60000 * 60)
        val formatFloat: (Double) -> String = {num -> String.format(Locale.US, "%02d", num.toInt())}
        return "${formatFloat(minutes)}:${formatFloat(seconds)}"
    }
    throw Error("Only accepts ms for now!") // make more specific later
}
@Composable
fun CurrentlyPlayingScreen(viewModel: CantonaViewModel, modifier: Modifier = Modifier){
    val metadata by viewModel.metadataState.collectAsState()
    val currentItem by viewModel.currentMedia.collectAsState()
    val context = LocalContext.current
    val isPlaying by viewModel.isPlaying.collectAsState()
    val iconColor = MaterialTheme.colorScheme.onSurface
    var duration by remember { mutableStateOf(0L) }
    var currentPosition by remember { mutableStateOf(0L) }

    LaunchedEffect(viewModel.player) {
        while (true) {
            currentPosition = viewModel.player.currentPosition
            duration = viewModel.player.duration
            delay(500)
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier
        .fillMaxWidth()) {
        Log.d("CurrentlyPlayingScreen", "${currentItem?.localConfiguration?.uri} is the current URI")
        Log.d("CurrentlyPlayingScreen", "${metadata.trackNumber} is the current track num")
        AlbumArt(currentItem?.localConfiguration?.uri?.let { getAlbumIdFromUri(context, it) } ?: -1,
            modifier = Modifier.aspectRatio(1f).fillMaxWidth(.8f))
        Text(
            text = metadata.title.toString(), color = MaterialTheme.colorScheme.onSurface,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier,
        )
        Text(
            text = metadata.artist.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.weight(1f))
        LinearProgressIndicator(
            progress = {  if(duration != 0L) (currentPosition.toFloat()/duration.toFloat()).coerceIn(0f, 1f)
                       else 0f},
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = durationString(currentPosition.toDouble()),  modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start)
            Text(text = durationString(duration.toDouble()), modifier = Modifier.weight(1f),
                textAlign = TextAlign.End)
        }
        Spacer(modifier = Modifier.weight(0.25f))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(end=12.dp)) {
            Icon(
                painterResource(R.drawable.baseline_fast_rewind_24),
                "Rewind",
                tint = iconColor
            ) // Not Functional
            IconButton(
                onClick = { viewModel.player.playWhenReady = !isPlaying },
                modifier = Modifier.size(200.dp)
            ) {
                Icon(
                    painterResource(
                        if (isPlaying) R.drawable.baseline_pause_24
                        else R.drawable.baseline_play_arrow_24
                    ), "Play/Pause", tint = iconColor
                )
            }
            Icon(
                painterResource(R.drawable.baseline_fast_forward_24),
                "Fast Forward",
                tint = iconColor
            ) // Not Functional
        }
    }
}