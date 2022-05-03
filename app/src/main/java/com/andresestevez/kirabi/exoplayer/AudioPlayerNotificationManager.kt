package com.andresestevez.kirabi.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaSessionCompat
import com.andresestevez.kirabi.R
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

private const val NOTIFICATION_CHANNEL_ID = "audio"
private const val NOTIFICATION_ID = 1

class AudioPlayerNotificationManager(
    private val context: Context,
    mediaSession: MediaSessionCompat,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val glide: RequestManager,
    private val newAudioCallback: () -> Unit,
) {

    private val notificationManager: PlayerNotificationManager

    init {
        notificationManager = PlayerNotificationManager.Builder(context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID).apply {
            setNotificationListener(notificationListener)
            setChannelNameResourceId(R.string.audio_notification_channel_name)
            setChannelDescriptionResourceId(R.string.audio_notification_channel_description)
            setMediaDescriptionAdapter(DescriptionAdapter(mediaSession))
        }.build()

        notificationManager.apply {
            setMediaSessionToken(mediaSession.sessionToken)
            setSmallIcon(R.drawable.ic_audiotrack)
        }
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(private val mediaSession: MediaSessionCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence =
            mediaSession.controller.metadata.description.title.toString()

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

            glide.asBitmap()
                .load(mediaSession.controller.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        callback.onBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit

                })
            return null
        }
    }
}