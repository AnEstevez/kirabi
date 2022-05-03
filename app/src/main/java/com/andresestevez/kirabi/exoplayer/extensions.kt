package com.andresestevez.kirabi.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.andresestevez.kirabi.data.models.Media

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

fun MediaMetadataCompat.toMedia(): Media = Media(
    description.mediaId ?: "",
    description.title.toString(),
    description.subtitle.toString(),
    description.description.toString(),
    description.mediaUri.toString(),
    description.iconUri.toString(),
)

