package com.andresestevez.kirabi.presentation.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.andresestevez.kirabi.exoplayer.*
import com.andresestevez.kirabi.exoplayer.extensions.duration
import com.andresestevez.kirabi.exoplayer.extensions.id
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MediaViewModel @Inject constructor(
    audioServiceConnection: AudioServiceConnection
) : ViewModel() {

    private val _mediaMetadata = MutableLiveData<MediaMetadataCompat>()
    val mediaMetadata : LiveData<MediaMetadataCompat> get() = _mediaMetadata

    /**
     * When the session's [PlaybackStateCompat] changes, the [mediaItems] need to be updated
     * so the correct [MediaItemData.playbackRes] is displayed on the active item.
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        val metadata = audioServiceConnection.curPlayingMedia.value ?: NOTHING_PLAYING
        // Only update media item once we have duration available
        if (metadata.duration != 0L && metadata.id != null) {
            _mediaMetadata.postValue(metadata)
        }
    }

    /**
     * When the session's [MediaMetadataCompat] changes, the [mediaItems] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their [MediaItemData.playbackRes]
     * changed. (i.e.: play/pause button or blank)
     */
    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        // Only update media item once we have duration available
        if (it.duration != 0L && it.id != null) {
            _mediaMetadata.postValue(it)
        }
    }

    /**
     * Because there's a complex dance between this [ViewModel] and the [AudioServiceConnection]
     * (which is wrapping a [MediaBrowserCompat] object), the usual guidance of using
     * [Transformations] doesn't quite work.
     *
     * Specifically there's three things that are watched that will cause the single piece of
     * [LiveData] exposed from this class to be updated.
     *
     * [AudioServiceConnection.playbackState] changes state based on the playback state of
     * the player, which can change the [MediaItemData.playbackRes]s in the list.
     *
     * [AudioServiceConnection.nowPlaying] changes based on the item that's being played,
     * which can also change the [MediaItemData.playbackRes]s in the list.
     */
    private val audioServiceConnection = audioServiceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.curPlayingMedia.observeForever(mediaMetadataObserver)
    }

    /**
     * Since we use [LiveData.observeForever] above (in [audioServiceConnection]), we want
     * to call [LiveData.removeObserver] here to prevent leaking resources when the [ViewModel]
     * is not longer in use.
     *
     * For more details, see the kdoc on [audioServiceConnection] above.
     */
    override fun onCleared() {
        super.onCleared()
        // Remove the permanent observers from the AudioServiceConnection.
        audioServiceConnection.playbackState.removeObserver(playbackStateObserver)
        audioServiceConnection.curPlayingMedia.removeObserver(mediaMetadataObserver)
    }

}