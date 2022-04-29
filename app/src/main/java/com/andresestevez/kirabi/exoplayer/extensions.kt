package com.andresestevez.kirabi.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat

fun MediaMetadataCompat.toMediaItem(flag: Int = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE): MediaBrowserCompat.MediaItem {
    val mediaDescription = MediaDescriptionCompat.Builder()
        .setMediaId(description.mediaId)
        .setTitle(description.title)
        .setSubtitle(description.subtitle)
        .setDescription(description.description)
        .setIconUri(description.iconUri)
        .setMediaUri(description.mediaUri)
        .build()
    return MediaBrowserCompat.MediaItem(mediaDescription, flag)
}