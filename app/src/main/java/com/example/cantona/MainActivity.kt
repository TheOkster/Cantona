package com.example.cantona

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint.Align
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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    Songs,
    CurrentlyPlaying
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exoPlayer: ExoPlayer = ExoPlayer.Builder(this).build()
        val viewModel: CantonaViewModel = CantonaViewModel(exoPlayer)

        enableEdgeToEdge()
        setContent {
            CantonaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    CantonaApp(viewModel)
                }
            }
        }
    }
}
@Composable
fun PlayButton(viewModel: CantonaViewModel, navController: NavController, modifier: Modifier = Modifier){
    val metadata by viewModel.metadataState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val iconColor = MaterialTheme.colorScheme.onSurface
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth().clickable(onClick = {
        navController.navigate(Screen.CurrentlyPlaying.name)
    })){
        Text("${metadata.title ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(0.15f))

        Text("${metadata.artist ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))

        // TODO: change to use resource strings
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxHeight().padding(end=12.dp)) {
            Icon(
                painterResource(R.drawable.baseline_fast_rewind_24),
                "Rewind",
                tint = iconColor
            ) // Not Functional
            IconButton(
                onClick = { viewModel.player.playWhenReady = !isPlaying }
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
@Composable
fun CantonaApp(viewModel: CantonaViewModel) {
    val context = LocalContext.current
    val songs = getAudioFiles(context)
    val albums = songs.groupBy { it.albumID }
    val albumMap: MutableMap<Long, Album> = mutableMapOf()
//    val mediaSession = MediaSession.Builder(context, exoplayer).build()

    for(entry in albums){
        if(entry.key != null) albumMap[entry.key!!] = getAlbum(context, entry.key!!, entry.value)
    }
    RequestPermissions()
    Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)){ it ->
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Screen.Albums.name) {
            composable(Screen.Albums.name) {
                AlbumsScreen(albumMap, navController, viewModel)
            }
            composable("${Screen.Songs.name}/{albumId}") { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId")?.toLongOrNull() ?: return@composable
                albumMap[albumId]?.let { it1 -> SongsScreen(it1, viewModel) }
            }
            composable(Screen.CurrentlyPlaying.name) { backStackEntry ->
               CurrentlyPlayingScreen(viewModel)
                // TODO: switch to make vertical alignment dependent on the component itself
            }
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
    val selectionArgs = arrayOf("%/Music/%") // Plan to utilize later

    val sortOrder = "${Media.DISPLAY_NAME} ASC" // Will likely change

    val uri = Media.EXTERNAL_CONTENT_URI

    context.contentResolver.query(
       Media.EXTERNAL_CONTENT_URI,
        attributes,
        selection,
        null,
        sortOrder
    )?.use { cursor ->
        // Plan to switch this into a more malleable implementaiton
        // Currently manually gets the column index for each metadata category
        // and gets it from the cursor if it can
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
fun getAlbumIdFromUri(context: Context, uri: Uri): Long? {
    context.contentResolver.query(uri, arrayOf(Media.ALBUM_ID),
        null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) return cursor.getLong(cursor.getColumnIndexOrThrow(Media.ALBUM_ID))
    }
    return null
}

@Composable
fun RequestPermissions() {
    val context = LocalContext.current

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
    // Can only be used with Tiramisu apparently, need to change
//    permissionToRequest = Manifest.permission.POST_NOTIFICATIONS
//     launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean ->
//        hasPermission = isGranted
//    }
//
//    LaunchedEffect(hasPermission) {
//        if (!hasPermission) {
//            launcher.launch(permissionToRequest)
//        }
//    }
}
