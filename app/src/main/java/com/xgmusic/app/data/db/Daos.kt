package com.xgmusic.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM local_music ORDER BY lastPlayed DESC")
    fun getAllMusic(): Flow<List<LocalMusicEntity>>

    @Query("SELECT * FROM local_music WHERE id = :id")
    suspend fun getMusicById(id: Long): LocalMusicEntity?

    @Query("SELECT * FROM local_music WHERE musicId = :musicId")
    suspend fun getMusicByMusicId(musicId: String): LocalMusicEntity?

    @Query("SELECT * FROM local_music WHERE isDownloaded = 1")
    fun getDownloadedMusic(): Flow<List<LocalMusicEntity>>

    @Query("SELECT * FROM local_music WHERE name LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchMusic(query: String): Flow<List<LocalMusicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(music: LocalMusicEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMusic(musicList: List<LocalMusicEntity>)

    @Update
    suspend fun updateMusic(music: LocalMusicEntity)

    @Query("DELETE FROM local_music WHERE id = :id")
    suspend fun deleteMusic(id: Long)

    @Query("DELETE FROM local_music WHERE musicId = :musicId")
    suspend fun deleteMusicByMusicId(musicId: String)

    @Query("UPDATE local_music SET lastPlayed = :timestamp, playCount = playCount + 1 WHERE musicId = :musicId")
    suspend fun updatePlayInfo(musicId: String, timestamp: Long)

    @Query("UPDATE local_music SET isDownloaded = :downloaded, downloadPath = :path WHERE musicId = :musicId")
    suspend fun updateDownloadStatus(musicId: String, downloaded: Boolean, path: String)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_tasks WHERE status != 'completed'")
    fun getActiveDownloads(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(task: DownloadTaskEntity): Long

    @Update
    suspend fun updateDownload(task: DownloadTaskEntity)

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun deleteDownload(id: Long)
}

@Dao
interface PluginConfigDao {
    @Query("SELECT * FROM plugin_config WHERE isActive = 1")
    fun getActivePlugins(): Flow<List<PluginConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPluginConfig(config: PluginConfigEntity): Long

    @Query("UPDATE plugin_config SET isActive = :active WHERE pluginId = :pluginId")
    suspend fun updatePluginActive(pluginId: String, active: Boolean)

    @Query("DELETE FROM plugin_config WHERE pluginId = :pluginId")
    suspend fun deletePluginConfig(pluginId: String)
}
