package com.andresestevez.kirabi.data

import android.support.v4.media.MediaMetadataCompat
import com.andresestevez.kirabi.data.server.MediaDto

fun MediaDto.toMediaMetadataCompat(): MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, artist)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, imageUrl)
    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, imageUrl)
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, url)
    .build()