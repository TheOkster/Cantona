package com.example.cantona

import android.content.Context
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
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider

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
        PlayButton(viewModel, modifier = Modifier
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