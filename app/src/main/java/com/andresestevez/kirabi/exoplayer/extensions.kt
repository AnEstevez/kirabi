package com.andresestevez.kirabi.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri

fun MediaMetadataCompat.toMediaItem(flag: Int = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE): MediaBrowserCompat.MediaItem {
    val mediaDescription = MediaDescriptionCompat.Builder()
        .setMediaUri(getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri())
        .build()
    return MediaBrowserCompat.MediaItem(mediaDescription, flag)
}