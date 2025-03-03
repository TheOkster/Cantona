package com.example.cantona

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.CantonaTheme

enum class Screen(){
    Albums,
    Songs
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CantonaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    CantonaApp()
                }
            }
        }
    }
}
@Composable
fun CantonaApp() {
    val context = LocalContext.current
    val songs = getAudioFiles(context)
    val albums = songs.groupBy { it.albumID }
    val albumMap: MutableMap<Long, Album> = mutableMapOf()
    val exoplayer = ExoPlayer.Builder(context).build()
    val mediaSession = MediaSession.Builder(context, exoplayer).build()

    for(entry in albums){
        if(entry.key != null) albumMap[entry.key!!] = getAlbum(context, entry.key!!, entry.value)
    }
    RequestPermissions()
    Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)){ it ->
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Screen.Albums.name) {
            composable(Screen.Albums.name) {
                AlbumsScreen(albumMap, navController)
            }
            composable("${Screen.Songs.name}/{albumId}") { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId")?.toLongOrNull() ?: return@composable
                albumMap[albumId]?.let { it1 -> SongsScreen(it1, exoplayer) }
            }
        }
    }
}
@Composable
fun AlbumsScreen(albumMap: Map<Long, Album>, navController: NavController, modifier: Modifier = Modifier){
    LazyVerticalGrid(columns= GridCells.Adaptive(minSize=200.dp), modifier = modifier) {
        items(albumMap.values.toList()) {
            AlbumBlock(
                album = it, onClick = {navController.navigate("${Screen.Songs.name}/${it.albumId}")
                }
            )
        }
    }
}
@Composable
fun SongsScreen(album: Album, exoPlayer: ExoPlayer, modifier: Modifier = Modifier){
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
                        song = it, exoPlayer = exoPlayer
                    )
                }
            }
        }
    }
}
@Preview
@Composable
fun SongsScreenPreview(album: Album = Album(0, listOf(Song(title="Naked in Manhattan", artist="Chappell Roan")))){
    SongsScreen(album, ExoPlayer.Builder(LocalContext.current).build())
}
@Composable
fun SongDisplay(
    song: Song, exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            modifier = modifier
                .border(2.dp, Color.hsv(0f, 0f, 1f, .25f), RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clickable {
                    if (song.uri != Uri.EMPTY) {
                        exoPlayer.apply {
                            val mediaItem = MediaItem.fromUri(song.uri)
                            setMediaItem(mediaItem)
                            prepare()
                            play()
                        }
                        Log.d("Player", "Should be playing...")
                    }
                    Log.d("Player", "Empty URI")
                },
        ) {
            Text(text = song.title ?: "N/A", fontSize = 20.sp)
            Text(song.artist ?: "N/A", fontSize = 20.sp)
        }
    }
}




fun getAudioFiles(context: Context): List<Song> {
    val audioList = mutableListOf<Song>()

    val attributes = arrayOf(
        Media._ID,
        Media.TITLE,
        Media.ALBUM,
        Media.ARTIST,
        Media.ALBUM_ARTIST,
        Media.ALBUM_ID
    )

    val selection = "${Media.IS_MUSIC} != 0"
    val selectionArgs = arrayOf("%/Music/%")

    val sortOrder = "${Media.DISPLAY_NAME} ASC"

    val uri = Media.EXTERNAL_CONTENT_URI

    context.contentResolver.query(
       Media.EXTERNAL_CONTENT_URI,
        attributes,
        selection,
        null,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndex(Media._ID)
        val songNameColumn = cursor.getColumnIndex(Media.TITLE)
        val albumColumn = cursor.getColumnIndex(Media.ALBUM)
        val artistColumn = cursor.getColumnIndex(Media.ARTIST)
        val albumArtistColumn = cursor.getColumnIndex(Media.ALBUM_ARTIST)
        val albumIdColumn = cursor.getColumnIndex(Media.ALBUM_ID)

        while (cursor.moveToNext()) {
            val id = if (idColumn != -1) cursor.getLong(idColumn) else null
            val songName =
                if (songNameColumn != -1) cursor.getString(songNameColumn) else null
            val albumName = if (albumColumn != -1) cursor.getString(albumColumn) else null
            val artistName = if (artistColumn != -1) cursor.getString(artistColumn) else null
            val albumArtistName =
                if (albumArtistColumn != -1) cursor.getString(albumArtistColumn) else null
            val albumId = if (albumIdColumn != -1) cursor.getLong(albumIdColumn) else null
            val uriWithId = if (id != null) ContentUris.withAppendedId(uri, id) else null
            val song = Song(
                uri =  uriWithId ?: Uri.EMPTY, songName,
                artistName,
                albumName,
                albumId,
                albumArtistName
            )
            audioList.add(song)
        }
    }
    return audioList
}
@Composable
fun RequestPermissions() {
    val context = LocalContext.current
    var permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permissionToRequest
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            launcher.launch(permissionToRequest)
        }
    }
    permissionToRequest = Manifest.permission.POST_NOTIFICATIONS
     launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            launcher.launch(permissionToRequest)
        }
    }

}
