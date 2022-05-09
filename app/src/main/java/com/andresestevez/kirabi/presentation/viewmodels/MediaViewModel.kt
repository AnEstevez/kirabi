package com.andresestevez.kirabi.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andresestevez.kirabi.data.common.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.andresestevez.kirabi.exoplayer.AudioService
import com.andresestevez.kirabi.exoplayer.AudioServiceConnection
import com.andresestevez.kirabi.exoplayer.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MediaViewModel @Inject constructor(
    audioServiceConnection: AudioServiceConnection
) : ViewModel() {

    private val playbackState = audioServiceConnection.playbackState

    private val _curMediaDuration = MutableLiveData<Long>()
    val curMediaDuration: LiveData<Long> get() = _curMediaDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition: LiveData<Long> get() = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while(true) {
                val pos = playbackState.value?.currentPlaybackPosition
                pos?.let {
                    if (curPlayerPosition.value != it) {
                        _curPlayerPosition.postValue(it)
                        _curMediaDuration.postValue(AudioService.curMediaDuration)
                    }
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
}