package com.example.cantona

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Song(val uri: Uri = Uri.EMPTY, val title: String? = null, val artist: String? = null, val album: String? = null, val albumID: Long? = null,
                val albumArtist: String? = null)
@Composable
fun SongDisplay(
    song: Song, viewModel: CantonaViewModel,
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
                        viewModel.play(song.uri)
                    } else Log.d("Player", "Empty URI")
                },
        ) {
            Text(text = song.title ?: "N/A", fontSize = 20.sp)
            Text(song.artist ?: "N/A", fontSize = 20.sp)
        }
    }
}