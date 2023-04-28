package com.andresestevez.kirabi.exoplayer.extensions

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.andresestevez.kirabi.data.models.Media
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.util.MimeTypes
import java.io.File

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

/**
 * Returns a Content Uri for the AlbumArtContentProvider
 */
fun File.asAlbumArtContentUri(): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(AUTHORITY)
        .appendPath(this.path)
        .build()
}

fun String.firstCharOfEachWordToUppercase(): String {
    return if (this.isNotBlank()) {
        var input = this.trim()
        var wordsList = input.split(' ')
        val output =
            wordsList.map { it.replaceFirstChar { firstChar -> firstChar.uppercase() } }.toList()
        output.joinToString(" ")
    } else this
}

private const val AUTHORITY = "com.andresestevez.kirabi"
private const val ORIGINAL_ARTWORK_URI_KEY = "com.andresestevez.kirabi.ARTWORK_URI"

fun MediaMetadataCompat.toMediaItemMetadata(): com.google.android.exoplayer2.MediaMetadata {
    return with(MediaMetadata.Builder()) {
        setTitle(title)
        setDisplayTitle(displayTitle)
        setAlbumArtist(artist)
        setAlbumTitle(album)
        setComposer(composer)
        setTrackNumber(trackNumber.toInt())
        setTotalTrackCount(trackCount.toInt())
        setDiscNumber(discNumber.toInt())
        setWriter(writer)
        setArtworkUri(albumArtUri)
        val extras = Bundle()
        getString(ORIGINAL_ARTWORK_URI_KEY)?.let {
            // album art is a content:// URI. Keep the original for Cast.
            extras.putString(
                ORIGINAL_ARTWORK_URI_KEY,
                it
            )
        }
        setExtras(extras)
    }.build()
}

fun MediaMetadataCompat.toExoplayer2MediaItem(): com.google.android.exoplayer2.MediaItem {
    return with(com.google.android.exoplayer2.MediaItem.Builder()) {
        setMediaId(mediaUri.toString())
        setUri(mediaUri)
        setMimeType(MimeTypes.AUDIO_MPEG)
        setMediaMetadata(toMediaItemMetadata())
    }.build()
}

/**
 * Helper extension to convert a potentially null [String] to a [Uri] falling back to [Uri.EMPTY]
 */
fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY

/**
 * Useful extensions for [MediaMetadataCompat].
 */
inline val MediaMetadataCompat.id: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

inline val MediaMetadataCompat.title: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)

inline val MediaMetadataCompat.subtitle: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

inline val MediaMetadataCompat.artist: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

inline val MediaMetadataCompat.duration
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

inline val MediaMetadataCompat.album: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)

inline val MediaMetadataCompat.author: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_AUTHOR)

inline val MediaMetadataCompat.writer: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_WRITER)

inline val MediaMetadataCompat.composer: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_COMPOSER)

inline val MediaMetadataCompat.compilation: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_COMPILATION)

inline val MediaMetadataCompat.date: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_DATE)

inline val MediaMetadataCompat.year: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_YEAR)

inline val MediaMetadataCompat.genre: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_GENRE)

inline val MediaMetadataCompat.trackNumber
    get() = getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER)

inline val MediaMetadataCompat.trackCount
    get() = getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS)

inline val MediaMetadataCompat.discNumber
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DISC_NUMBER)

inline val MediaMetadataCompat.albumArtist: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST)

inline val MediaMetadataCompat.art: Bitmap
    get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ART)

inline val MediaMetadataCompat.artUri: Uri
    get() = this.getString(MediaMetadataCompat.METADATA_KEY_ART_URI).toUri()

inline val MediaMetadataCompat.albumArt: Bitmap?
    get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)

inline val MediaMetadataCompat.albumArtUri: Uri
    get() = this.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI).toUri()

inline val MediaMetadataCompat.userRating
    get() = getLong(MediaMetadataCompat.METADATA_KEY_USER_RATING)

inline val MediaMetadataCompat.rating
    get() = getLong(MediaMetadataCompat.METADATA_KEY_RATING)

inline val MediaMetadataCompat.displayTitle: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)

inline val MediaMetadataCompat.displaySubtitle: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

inline val MediaMetadataCompat.displayDescription: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)

inline val MediaMetadataCompat.displayIcon: Bitmap
    get() = getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON)

inline val MediaMetadataCompat.displayIconUri: Uri
    get() = this.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI).toUri()

inline val MediaMetadataCompat.mediaUri: Uri
    get() = this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri()

inline val MediaMetadataCompat.downloadStatus
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DOWNLOAD_STATUS)