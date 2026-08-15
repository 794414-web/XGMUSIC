package com.xgmusic.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.xgmusic.app.MainActivity
import com.xgmusic.app.R
import com.xgmusic.app.player.PlayerManager

/**
 * Music Playback Service
 *
 * Foreground service that handles music playback.
 * Manages MediaSession for system integration (Bluetooth, car, etc.)
 */
class MusicPlaybackService : MediaSessionService() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "xgmusic_playback"
    }

    private val binder = LocalBinder()
    private var playerManager: PlayerManager? = null

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return getCurrentMediaSession()
    }

    private fun getCurrentMediaSession(): MediaSession? {
        return (application as? com.xgmusic.app.XGMusicApp)
            ?.container
            ?.playerManager
            ?.let { playerManager ->
                // MediaSession is managed by PlayerManager
                null
            }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("XGMUSIC")
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
