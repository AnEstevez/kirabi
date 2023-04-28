package com.andresestevez.kirabi.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.data.resolveUriAsBitmap
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val NOTIFICATION_CHANNEL_ID = "audio"
private const val NOTIFICATION_ID = 1

class AudioPlayerNotificationManager(
    private val context: Context,
    mediaSession: MediaSessionCompat,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val glide: RequestManager,
) {

    private val notificationManager: PlayerNotificationManager

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(job + Dispatchers.Main)

    init {
        notificationManager = PlayerNotificationManager.Builder(context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID).apply {
            setNotificationListener(notificationListener)
            setChannelNameResourceId(R.string.audio_notification_channel_name)
            setChannelDescriptionResourceId(R.string.audio_notification_channel_description)
            setMediaDescriptionAdapter(DescriptionAdapter(mediaSession))
        }.build().apply {
            setUseRewindAction(false)
            setUseFastForwardAction(false)
            setUseChronometer(false)
            setUsePreviousActionInCompactView(true)
            setUseNextActionInCompactView(true)
        }

        notificationManager.apply {
            setMediaSessionToken(mediaSession.sessionToken)
            setSmallIcon(R.drawable.ic_audiotrack)
        }
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    private inner class DescriptionAdapter(private val mediaSession: MediaSessionCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaSession.controller.metadata.description.title.toString()
        }

        override fun getCurrentContentText(player: Player): CharSequence? =
            mediaSession.controller.metadata.description.subtitle

        override fun getCurrentSubText(player: Player): CharSequence? =
            mediaSession.controller.metadata.description.description

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            mediaSession.controller.sessionActivity

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback,
        ): Bitmap? {

            val iconUri = mediaSession.controller.metadata.description.iconUri
            return if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = iconUri?.let {
                        it.resolveUriAsBitmap(glide)
                    }
                }
                return currentBitmap

            } else {
                currentBitmap
            }

        }

    }
}