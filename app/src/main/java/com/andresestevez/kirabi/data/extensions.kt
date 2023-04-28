package com.andresestevez.kirabi.data

import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.andresestevez.kirabi.data.models.Media
import com.andresestevez.kirabi.data.server.MediaDto
import com.andresestevez.kirabi.exoplayer.extensions.firstCharOfEachWordToUppercase
import com.bumptech.glide.RequestManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun MediaDto.toMediaMetadataCompat(): MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title.firstCharOfEachWordToUppercase())
    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist.firstCharOfEachWordToUppercase())
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, imageUrl)
    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, imageUrl)
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, url)
    .build()

fun MediaBrowserCompat.MediaItem.toMedia(): Media = Media(
    mediaId ?: "",
    description.title.toString().firstCharOfEachWordToUppercase(),
    description.subtitle.toString().firstCharOfEachWordToUppercase(),
    description.description.toString(),
    description.mediaUri.toString(),
    description.iconUri.toString(),
)

suspend fun Uri.resolveUriAsBitmap(glide: RequestManager, width: Int? = null, height: Int? = null): Bitmap? {
    return withContext(Dispatchers.IO) {
        if (width != null && height != null) {
            glide.asBitmap()
                .load(this@resolveUriAsBitmap)
                .submit(width, height)
                .get()
        } else {
            glide.asBitmap()
                .load(this@resolveUriAsBitmap)
                .submit()
                .get()
        }
    }
}

