package com.andresestevez.kirabi.exoplayer

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.andresestevez.kirabi.exoplayer.callbacks.AudioPlayerNotificationListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "AudioService"

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AudioService @Inject constructor(
    private val dataSourceFactory: DefaultDataSource.Factory,
    private val exoPlayer: ExoPlayer,
    coroutineDispatcher: CoroutineDispatcher,
) : MediaBrowserServiceCompat() {

    private lateinit var audioNotificationManager: AudioNotificationManager

    private val serviceJob = Job()
    private val audioServiceScope = CoroutineScope(coroutineDispatcher + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    override fun onCreate() {
        super.onCreate()
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        audioNotificationManager = AudioNotificationManager(
            this,
            mediaSession,
            AudioPlayerNotificationListener(this),
            ) { }

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        audioServiceScope.cancel()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        TODO("Not yet implemented")
    }

}