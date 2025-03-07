package com.example.cantona

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage


data class Album(val albumId: Long, val songs: List<Song> = listOf(), val title: String = "", val artist: String = "",
    val year: Int? = null){

}
@Composable
fun AlbumBlock(album: Album, onClick: () -> Unit = {},
               modifier: Modifier = Modifier
){
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(.8f).fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(.8f),
            ) {
                AlbumArt(album.albumId, modifier = Modifier.aspectRatio(1f))
                Spacer(modifier = Modifier.weight(.05f))
                Text(
                    text = album.title, color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    modifier = Modifier,
                    style = LocalTextStyle.current.copy(
                        lineHeight = 16.sp
                    )
                )
                Text(
                    text = album.artist,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.2f*2/3).fillMaxWidth(),
                    style = LocalTextStyle.current.copy(
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}
@Preview
@Composable
fun AlbumBlockPreview(album: Album = Album(0, listOf(), "The Rise and Fall of a Midwest Princess","Chappell Roan"), onClick: () -> Unit = {},
               modifier: Modifier = Modifier
){
    AlbumBlock(album = album, onClick = onClick, modifier = modifier)
}
@Composable
fun AlbumArt(albumId: Long, modifier: Modifier = Modifier) {
    AsyncImage(
        model = getAlbumArtUri(albumId),
        contentDescription = "Album Art",
        modifier = modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.default_album), // Fallback image
        placeholder = painterResource(id = R.drawable.default_album) // Placeholder
    )
}
@Composable
fun AlbumArt(uri: Uri, modifier: Modifier = Modifier) {
    AsyncImage(
        model = uri,
        contentDescription = "Album Art",
        modifier = modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.default_album), // Fallback image
        placeholder = painterResource(id = R.drawable.default_album) // Placeholder
    )
}
fun getAlbumArtUri(albumId: Long): Uri {
    return Uri.parse("content://media/external/audio/albumart/$albumId")
}
fun getAlbum(context: Context, albumId: Long, songList: List<Song>): Album {
    val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    val attributes = arrayOf(
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Albums.ARTIST,
        MediaStore.Audio.Albums.LAST_YEAR
    )
    val selection = "${MediaStore.Audio.Albums._ID} = ?"
    val selectionArgs = arrayOf(albumId.toString())

    context.contentResolver.query(uri, attributes, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
            val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST))
            var lastYear: Int? = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.LAST_YEAR))
            if(lastYear == 0){
                lastYear = null;
            }
            return Album(albumId, songList, title, artist, lastYear)
        }
    }
    return Album(albumId)
}