package com.xgmusic.app.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.media.MediaPlaybackService
import com.xgmusic.app.XGMusicApp

/**
 * Car Music Service
 *
 * Android Automotive Media Playback Service.
 * Provides car-optimized UI for music control.
 */
class CarMusicService : MediaPlaybackService() {

    override fun onCreateSession(sessionType: Int): Session {
        return CarPlaybackSession(this)
    }

    override fun onGetLibraryRoot(
        host: androidx.car.app.media.MediaPlaybackService.Host,
        libraries: MutableList<androidx.car.app.LibraryItem>
    ) {
        // Root library items for car
        libraries.apply {
            // Add "推荐" section
            // Add "我的音乐" section
            // Add "最近播放" section
        }
    }

    override fun onGetMediaItems(
        host: androidx.car.app.media.MediaPlaybackService.Host,
        parentId: String,
        limit: Int,
        offset: Int
    ): List<androidx.car.app.MediaItem> {
        // Return media items for the given parent
        return emptyList()
    }
}
