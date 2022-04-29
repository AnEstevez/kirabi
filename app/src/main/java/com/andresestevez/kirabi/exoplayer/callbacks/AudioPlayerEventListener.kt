package com.andresestevez.kirabi.exoplayer.callbacks

import android.widget.Toast
import com.andresestevez.kirabi.exoplayer.AudioService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class AudioPlayerEventListener(
    private val audioService: AudioService,
) : Player.Listener {

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (!playWhenReady) {
            audioService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        // TODO handle error
        Toast.makeText(audioService, "Unknown error", Toast.LENGTH_LONG).show()
    }
}