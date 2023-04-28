package com.andresestevez.kirabi.exoplayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.andresestevez.kirabi.exoplayer.FirebaseAudioSource
import com.andresestevez.kirabi.exoplayer.extensions.id
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class AudioPlaybackPreparer(
    private val firebaseAudioSource: FirebaseAudioSource,
    private val playerPrepared: (List<MediaMetadataCompat>, MediaMetadataCompat?, Boolean) -> Unit,

) : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepareFromMediaId(
        mediaId: String,
        playWhenReady: Boolean,
        extras: Bundle?
    ) {
        firebaseAudioSource.whenReady {
            val itemToPlay: MediaMetadataCompat? = firebaseAudioSource.medias.find { item ->
                item.id == mediaId
            }
            if (itemToPlay == null) {
                Log.w("AudioPlaybackPreparer", "Content not found: MediaID=$mediaId")
                // TODO: Notify caller of the error.
            } else {
                playerPrepared(
                    firebaseAudioSource.medias,
                    itemToPlay,
                    playWhenReady
                )
            }
        }
    }

    override fun onCommand(
        player: Player,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?,
    ): Boolean = false

    override fun onPrepare(playWhenReady: Boolean) = Unit

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
}
