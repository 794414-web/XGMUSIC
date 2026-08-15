package com.xgmusic.app.data

import android.content.Context
import com.xgmusic.app.data.db.MusicDatabase
import com.xgmusic.app.data.repository.MusicRepository
import com.xgmusic.app.data.repository.PluginRepository
import com.xgmusic.app.data.repository.UserPreferenceRepository
import com.xgmusic.app.player.PlayerManager
import com.xgmusic.app.plugin.PluginManager
import com.xgmusic.app.plugin.QuickJsEngine
import com.xgmusic.app.service.MusicServiceConnection

interface AppContainer {
    val musicRepository: MusicRepository
    val pluginRepository: PluginRepository
    val userPreferenceRepository: UserPreferenceRepository
    val pluginManager: PluginManager
    val playerManager: PlayerManager
    val musicServiceConnection: MusicServiceConnection
}

class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    private val database: MusicDatabase by lazy {
        MusicDatabase.getInstance(applicationContext)
    }

    override val musicRepository: MusicRepository by lazy {
        MusicRepository(
            musicDao = database.musicDao(),
            playlistDao = database.playlistDao(),
            remoteDataSource = pluginManager
        )
    }

    override val pluginRepository: PluginRepository by lazy {
        PluginRepository(applicationContext)
    }

    override val userPreferenceRepository: UserPreferenceRepository by lazy {
        UserPreferenceRepository(applicationContext)
    }

    private val quickJsEngine: QuickJsEngine by lazy {
        QuickJsEngine()
    }

    override val pluginManager: PluginManager by lazy {
        PluginManager(quickJsEngine, pluginRepository)
    }

    override val playerManager: PlayerManager by lazy {
        PlayerManager(applicationContext, musicRepository, pluginManager)
    }

    override val musicServiceConnection: MusicServiceConnection by lazy {
        MusicServiceConnection(applicationContext, playerManager)
    }
}
