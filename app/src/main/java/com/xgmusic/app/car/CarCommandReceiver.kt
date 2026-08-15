package com.xgmusic.app.car

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xgmusic.app.XGMusicApp
import com.xgmusic.app.data.model.PlaybackMode

/**
 * Car Command Receiver
 *
 * Handles car-specific broadcast intents:
 * - FULLSCREEN_ON/OFF: Toggle fullscreen notification mode
 * - MEDIA_BUTTON: Handle steering wheel media buttons
 */
class CarCommandReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CarCommandReceiver"
        const val ACTION_FULLSCREEN_ON = "io.github.netamade.FULLSCREEN_ON"
        const val ACTION_FULLSCREEN_OFF = "io.github.netamade.FULLSCREEN_OFF"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received action: $action")

        val app = context.applicationContext as? XGMusicApp ?: return

        when (action) {
            ACTION_FULLSCREEN_ON -> {
                Log.d(TAG, "Fullscreen notification ON")
                // Enable fullscreen notification mode
                scope.launch {
                    app.container.userPreferenceRepository.setNotificationFullscreen(true)
                }
            }

            ACTION_FULLSCREEN_OFF -> {
                Log.d(TAG, "Fullscreen notification OFF")
                // Disable fullscreen notification mode
                scope.launch {
                    app.container.userPreferenceRepository.setNotificationFullscreen(false)
                }
            }

            Intent.ACTION_MEDIA_BUTTON -> {
                val keyEvent = intent.getParcelableExtra<android.view.KeyEvent>(Intent.EXTRA_KEY_EVENT)
                keyEvent?.let {
                    when (it.keyCode) {
                        android.view.KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            app.container.playerManager.play()
                        }
                        android.view.KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            app.container.playerManager.pause()
                        }
                        android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            app.container.playerManager.togglePlayPause()
                        }
                        android.view.KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            // play next
                        }
                        android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            // play previous
                        }
                    }
                }
            }
        }
    }

    private val scope get() = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob()
    )
}
