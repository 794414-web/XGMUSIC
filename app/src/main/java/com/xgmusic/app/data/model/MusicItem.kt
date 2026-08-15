package com.xgmusic.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MusicItem(
    val id: String = "",
    val name: String = "",
    val artist: String = "",
    val album: String = "",
    val duration: Long = 0,
    val cover: String = "",
    val url: String = "",
    val lyricId: String = "",
    val pluginId: String = "",
    val source: String = "",
    val size: Long = 0,
    val type: String = "audio",
    val extra: Map<String, String> = emptyMap()
)

@Serializable
data class MusicResult(
    val items: List<MusicItem> = emptyList(),
    val hasMore: Boolean = false,
    val total: Int = 0
)

@Serializable
data class LyricItem(
    val time: Long = 0,
    val text: String = ""
)

@Serializable
data class LyricResult(
    val items: List<LyricItem> = emptyList(),
    val rawText: String = ""
)

@Serializable
data class PlaylistItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val cover: String = "",
    val count: Int = 0,
    val url: String = "",
    val pluginId: String = ""
)

@Serializable
data class PluginInfo(
    val id: String = "",
    val name: String = "",
    val author: String = "",
    val version: String = "",
    val description: String = "",
    val url: String = "",
    val fileName: String = "",
    val status: PluginStatus = PluginStatus.DISABLED
)

@Serializable
enum class PluginStatus {
    ACTIVE,
    DISABLED,
    ERROR
}

@Serializable
data class SubscribeSource(
    val name: String = "",
    val url: String = "",
    val plugins: List<PluginInfo> = emptyList()
)

@Serializable
data class MusicProgress(
    val current: Long = 0,
    val total: Long = 0,
    val isPlaying: Boolean = false,
    val buffered: Long = 0
)

@Serializable
enum class PlaybackMode {
    SEQUENCE,
    SINGLE,
    SHUFFLE,
    LOOP_LIST
}
