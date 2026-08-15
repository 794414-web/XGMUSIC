package com.xgmusic.app.data.repository

import com.xgmusic.app.data.db.LocalMusicEntity
import com.xgmusic.app.data.db.MusicDao
import com.xgmusic.app.data.model.MusicItem
import com.xgmusic.app.plugin.PluginManager
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    suspend fun searchMusic(keyword: String, page: Int): List<MusicItem>
    suspend fun getMusicUrl(id: String, pluginId: String): String?
    suspend fun importPlaylist(url: String): List<MusicItem>
}

class PluginRemoteDataSource(
    private val pluginManager: PluginManager
) : RemoteDataSource {
    override suspend fun searchMusic(keyword: String, page: Int): List<MusicItem> {
        return pluginManager.searchMusic(keyword, page)
    }

    override suspend fun getMusicUrl(id: String, pluginId: String): String? {
        return pluginManager.getMusicUrl(id, pluginId)
    }

    override suspend fun importPlaylist(url: String): List<MusicItem> {
        return pluginManager.importPlaylist(url)
    }
}

class MusicRepository(
    private val musicDao: MusicDao,
    private val remoteDataSource: RemoteDataSource
) {
    fun getAllLocalMusic(): Flow<List<LocalMusicEntity>> = musicDao.getAllMusic()

    suspend fun getMusicById(id: Long): LocalMusicEntity? = musicDao.getMusicById(id)

    suspend fun getMusicByMusicId(musicId: String): LocalMusicEntity? =
        musicDao.getMusicByMusicId(musicId)

    suspend fun insertMusic(music: LocalMusicEntity): Long = musicDao.insertMusic(music)

    suspend fun updateMusic(music: LocalMusicEntity) = musicDao.updateMusic(music)

    suspend fun deleteMusic(id: Long) = musicDao.deleteMusic(id)

    // Remote operations
    suspend fun searchRemote(keyword: String, page: Int = 1): List<MusicItem> =
        remoteDataSource.searchMusic(keyword, page)

    suspend fun getRemoteUrl(id: String, pluginId: String): String? =
        remoteDataSource.getMusicUrl(id, pluginId)

    suspend fun importRemotePlaylist(url: String): List<MusicItem> =
        remoteDataSource.importPlaylist(url)
}
