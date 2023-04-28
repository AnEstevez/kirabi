package com.andresestevez.kirabi.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.andresestevez.kirabi.data.server.MediaDatabase
import com.andresestevez.kirabi.exoplayer.State.*
import com.andresestevez.kirabi.exoplayer.extensions.toMediaItem
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

    fun whenReady(action: (Boolean) -> Unit): Boolean =
        if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state != STATE_ERROR)
            true
        }

    fun asMediaItems(): MutableList<MediaBrowserCompat.MediaItem> =
        medias.map { it.toMediaItem() }.toMutableList()

}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}