package com.muneeb.sonara

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumCover: Bitmap?


) : Parcelable