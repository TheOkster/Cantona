package com.example.cantona

import android.net.Uri

data class Song(val uri: Uri = Uri.EMPTY, val title: String? = null, val artist: String? = null, val album: String? = null, val albumID: Long? = null,
                val albumArtist: String? = null){

}
