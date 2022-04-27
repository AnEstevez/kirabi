package com.andresestevez.kirabi.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.andresestevez.kirabi.data.common.Constants.MEDIA_ROOT_ID
import com.andresestevez.kirabi.data.common.Constants.NETWORK_ERROR
import com.andresestevez.kirabi.data.common.Constants.SERVICE_TAG
import com.andresestevez.kirabi.exoplayer.callbacks.AudioPlaybackPreparer
import com.andresestevez.kirabi.exoplayer.callbacks.AudioPlayerEventListener
import com.andresestevez.kirabi.exoplayer.callbacks.AudioPlayerNotificationListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AudioService @Inject constructor(
    private val dataSourceFactory: DefaultDataSource.Factory,
    private val exoPlayer: ExoPlayer,
    coroutineDispatcher: CoroutineDispatcher,
    private val firebaseAudioSource: FirebaseAudioSource,
) : MediaBrowserServiceCompat() {

    private lateinit var audioPlayerNotificationManager: AudioPlayerNotificationManager

    private val serviceJob = Job()
    private val audioServiceScope = CoroutineScope(coroutineDispatcher + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var curPlayingAudio: MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private lateinit var audioPlayerEventListener: AudioPlayerEventListener

    companion object {
        var curMediaDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        audioServiceScope.launch {
            firebaseAudioSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        audioPlayerNotificationManager = AudioPlayerNotificationManager(
            this,
            mediaSession,
            AudioPlayerNotificationListener(this),
        ) {
            curMediaDuration = exoPlayer.duration
        }

        audioPlayerNotificationManager.showNotification(exoPlayer)

        val audioPlaybackPreparer = AudioPlaybackPreparer(firebaseAudioSource) {
            curPlayingAudio = it
            preparePlayer(
                firebaseAudioSource.medias,
                it,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
            setPlaybackPreparer(audioPlaybackPreparer)
            setQueueNavigator(MediaQueueNavigator())
        }

        audioPlayerEventListener = AudioPlayerEventListener(this)
        exoPlayer.addListener(audioPlayerEventListener)
    }

    private fun preparePlayer(
        medias: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean,
    ) {
        val curSongIndex = if (curPlayingAudio == null) 0 else medias.indexOf(itemToPlay)
        //TODO replace with setMediaSource + prepare
        exoPlayer.prepare(firebaseAudioSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioServiceScope.cancel()

        exoPlayer.removeListener(audioPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = firebaseAudioSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(firebaseAudioSource.asMediaItems())
                        if (!isPlayerInitialized && firebaseAudioSource.medias.isNotEmpty()) {
                            preparePlayer(firebaseAudioSource.medias,
                                firebaseAudioSource.medias[0],
                                false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }


    private inner class MediaQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseAudioSource.medias[windowIndex].description
        }
    }

}