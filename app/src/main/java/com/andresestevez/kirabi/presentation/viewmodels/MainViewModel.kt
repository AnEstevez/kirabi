package com.andresestevez.kirabi.presentation.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andresestevez.kirabi.data.Resource
import com.andresestevez.kirabi.data.common.Constants.MEDIA_ROOT_ID
import com.andresestevez.kirabi.data.models.Media
import com.andresestevez.kirabi.data.toMedia
import com.andresestevez.kirabi.exoplayer.AudioServiceConnection
import com.andresestevez.kirabi.exoplayer.isPlayEnabled
import com.andresestevez.kirabi.exoplayer.isPlaying
import com.andresestevez.kirabi.exoplayer.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val audioServiceConnection: AudioServiceConnection,
) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Media>>>()
    val mediaItems: LiveData<Resource<List<Media>>> = _mediaItems

    val isConnected = audioServiceConnection.isConnected
    val networkError = audioServiceConnection.networkError
    val curPlayingMedia = audioServiceConnection.curPlayingMedia
    val playbackState = audioServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        audioServiceConnection.subscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>,
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map { it.toMedia() }
                    _mediaItems.postValue(Resource.success(items))

                }
            })
    }

    fun skipToNextMedia() {
        audioServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousMedia() {
        audioServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        audioServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleMedia(mediaItem: Media, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.id == curPlayingMedia.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) audioServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> audioServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            audioServiceConnection.transportControls.playFromMediaId(mediaItem.id, null)
        }
    }

    fun prepareFromMedia(mediaItem: Media) {
        audioServiceConnection.transportControls.prepareFromMediaId(mediaItem.id, null)
        audioServiceConnection.transportControls.pause()
    }

    override fun onCleared() {
        super.onCleared()
        audioServiceConnection.unsubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

}