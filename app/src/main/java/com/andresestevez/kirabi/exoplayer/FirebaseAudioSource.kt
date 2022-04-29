package com.andresestevez.kirabi.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import androidx.core.net.toUri
import com.andresestevez.kirabi.data.server.MediaDatabase
import com.andresestevez.kirabi.exoplayer.State.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import javax.inject.Inject

class FirebaseAudioSource @Inject constructor(
    private val mediaDatabase: MediaDatabase,
) {
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    var medias = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() {
        state = STATE_INITIALIZING
        medias = mediaDatabase.getAllMediaMetadataCompat()
        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        medias.forEach { media ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(media.getString(METADATA_KEY_MEDIA_URI)
                    .toUri()))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems(): MutableList<MediaBrowserCompat.MediaItem> =
        medias.map { it.toMediaItem() }.toMutableList()

    fun whenReady(action: (Boolean) -> Unit): Boolean =
        if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == STATE_INITIALIZED)
            true
        }

}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}