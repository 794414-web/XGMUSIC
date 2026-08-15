package com.xgmusic.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "local_music")
data class LocalMusicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val musicId: String,
    val name: String,
    val artist: String = "",
    val album: String = "",
    val duration: Long = 0,
    val cover: String = "",
    val localPath: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "audio/*",
    val isDownloaded: Boolean = false,
    val downloadPath: String = "",
    val lastPlayed: Long = 0,
    val playCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val cover: String = "",
    val songIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val musicId: String,
    val name: String,
    val url: String,
    val status: String = "pending",
    val progress: Int = 0,
    val localPath: String = "",
    val fileSize: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "plugin_config")
data class PluginConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pluginId: String,
    val pluginName: String,
    val pluginUrl: String,
    val isActive: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
