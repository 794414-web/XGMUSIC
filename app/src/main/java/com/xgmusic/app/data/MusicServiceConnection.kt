package com.xgmusic.app.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.xgmusic.app.service.MusicPlaybackService
import com.xgmusic.app.player.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicServiceConnection(
    private val context: Context,
    private val playerManager: PlayerManager
) {

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            _isServiceConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _isServiceConnected.value = false
        }
    }

    fun startService() {
        val intent = Intent(context, MusicPlaybackService::class.java)
        context.startService(intent)
    }

    fun startForegroundService() {
        val intent = Intent(context, MusicPlaybackService::class.java)
        context.startForegroundService(intent)
    }

    fun bindService() {
        val intent = Intent(context, MusicPlaybackService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        if (_isServiceConnected.value) {
            context.unbindService(serviceConnection)
            _isServiceConnected.value = false
        }
    }
}
