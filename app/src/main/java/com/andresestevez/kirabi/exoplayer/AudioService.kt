package com.andresestevez.kirabi.exoplayer

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.data.common.Constants.MEDIA_ROOT_ID
import com.andresestevez.kirabi.data.common.Constants.NETWORK_ERROR
import com.andresestevez.kirabi.data.common.Constants.SERVICE_TAG
import com.andresestevez.kirabi.exoplayer.callbacks.AudioPlaybackPreparer
import com.andresestevez.kirabi.exoplayer.extensions.toExoplayer2MediaItem
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AudioService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var firebaseAudioSource: FirebaseAudioSource

    @Inject
    lateinit var glide: RequestManager

    private lateinit var audioPlayerNotificationManager: AudioPlayerNotificationManager

    private val serviceJob = Job()
    private val audioServiceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var audioPlayerEventListener: AudioPlayerEventListener

    var isForegroundService = false

    private var curPlayingAudio: MediaMetadataCompat? = null
    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private var currentMediaItemIndex: Int = 0

    private var isPlayerInitialized = false

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
            AudioPlayerNotificationListener(),
            glide
        )

        audioPlayerNotificationManager.showNotification(exoPlayer)

        val audioPlaybackPreparer = AudioPlaybackPreparer(firebaseAudioSource) {
            list, media, playWhenReady ->

            curPlayingAudio = media
            preparePlayer(
                list,
                media,
                playWhenReady,
                exoPlayer.currentPosition
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
            setPlaybackPreparer(audioPlaybackPreparer)
            setQueueNavigator(MediaQueueNavigator())
        }

        audioPlayerEventListener = AudioPlayerEventListener()
        exoPlayer.addListener(audioPlayerEventListener)

    }

    /**
     * Load the supplied list of songs and the song to play into the current player.
     */
    private fun preparePlayer(
        metadataList: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean,
        playbackStartPositionMs: Long
    ) {
        // Since the playlist was probably based on some ordering (such as tracks
        // on an album), find which window index to play first so that the song the
        // user actually wants to hear plays first.
        val initialWindowIndex = if (itemToPlay == null) 0 else metadataList.indexOf(itemToPlay)
        currentPlaylistItems = metadataList

        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.stop()
        // Set playlist and prepare.
        exoPlayer.setMediaItems(metadataList.map { it.toExoplayer2MediaItem() }, initialWindowIndex, playbackStartPositionMs)
        exoPlayer.prepare()
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
                                false,
                            0L)
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

    private inner class AudioPlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            this@AudioService.apply {
                stopForeground(true)
                isForegroundService = false
                stopSelf()
            }

        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean,
        ) {
            this@AudioService.apply {
                if (ongoing && !isForegroundService) {
                    ContextCompat.startForegroundService(
                        applicationContext,
                        Intent(applicationContext, this::class.java)
                    )
                    startForeground(notificationId, notification)
                    isForegroundService = true
                }
            }
        }
    }

    private inner class AudioPlayerEventListener : Player.Listener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    audioPlayerNotificationManager.showNotification(exoPlayer)
                    if (playbackState == Player.STATE_READY && !playWhenReady) {
                        // If playback is paused we remove the foreground state which allows the
                        // notification to be dismissed. An alternative would be to provide a
                        // "close" button in the notification which stops playback and clears
                        // the notification.
                        stopForeground(false)
                        isForegroundService = false
                    }
                }
                else -> {
                    audioPlayerNotificationManager.hideNotification()
                }
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)
                || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
                || events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED)
            ) {
                currentMediaItemIndex = if (currentPlaylistItems.isNotEmpty()) {
                    Util.constrainValue(
                        player.currentMediaItemIndex,
                        /* min= */ 0,
                        /* max= */ currentPlaylistItems.size - 1
                    )
                } else 0
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            var message = R.string.Unexpected_error
            Log.e(SERVICE_TAG,
                "Player error: " + error.errorCodeName + " (" + error.errorCode + ")")
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
                || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            ) {
                message = R.string.media_not_found
            }
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
