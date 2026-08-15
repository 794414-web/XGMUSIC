package com.xgmusic.app.plugin

import android.content.Context
import android.util.Log
import com.xgmusic.app.data.model.MusicItem
import com.xgmusic.app.data.model.MusicResult
import com.xgmusic.app.data.model.PlaylistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Plugin Manager
 *
 * Manages the lifecycle of music source plugins:
 * - Loading/unloading plugins
 * - Routing search/play requests to active plugins
 * - Managing plugin subscriptions
 */
class PluginManager(
    private val quickJsEngine: QuickJsEngine,
    private val pluginRepository: com.xgmusic.app.data.repository.PluginRepository
) {

    companion object {
        private const val TAG = "PluginManager"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _activePlugins = MutableStateFlow<List<PluginContext>>(emptyList())
    val activePlugins: StateFlow<List<PluginContext>> = _activePlugins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Initialize plugins - load from local storage and subscriptions
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val localPlugins = pluginRepository.getLocalPlugins()
            for (plugin in localPlugins) {
                try {
                    val content = pluginRepository.loadPluginContent(plugin.fileName)
                    if (content != null) {
                        val context = quickJsEngine.createContext(plugin.id, content)
                        if (context.initialize()) {
                            _activePlugins.value = _activePlugins.value + context
                            Log.d(TAG, "Loaded plugin: ${plugin.name}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load plugin ${plugin.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Plugin initialization failed: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Search music across all active plugins
     */
    suspend fun searchMusic(keyword: String, page: Int = 1): List<MusicItem> {
        val results = mutableListOf<MusicItem>()

        for (plugin in _activePlugins.value) {
            try {
                val rawResult = plugin.searchMusic(keyword, page)
                val items = parseMusicItems(rawResult, plugin.pluginId)
                results.addAll(items)
            } catch (e: Exception) {
                Log.w(TAG, "Search failed for plugin ${plugin.pluginId}: ${e.message}")
            }
        }

        // If no plugins loaded, return demo results
        if (results.isEmpty()) {
            results.addAll(getDemoResults(keyword))
        }

        return results
    }

    /**
     * Get music playback URL
     */
    suspend fun getMusicUrl(musicId: String, pluginId: String): String? {
        val plugin = _activePlugins.value.find { it.pluginId == pluginId }
            ?: _activePlugins.value.firstOrNull()
        return try {
            plugin?.getMusicUrl(musicId)
        } catch (e: Exception) {
            Log.e(TAG, "getMusicUrl failed: ${e.message}")
            null
        }
    }

    /**
     * Get lyrics for a track
     */
    suspend fun getLyric(musicId: String, pluginId: String): String {
        val plugin = _activePlugins.value.find { it.pluginId == pluginId }
            ?: _activePlugins.value.firstOrNull()
        return try {
            plugin?.getLyric(musicId) ?: "[]"
        } catch (e: Exception) {
            "[]"
        }
    }

    /**
     * Import playlist from URL
     */
    suspend fun importPlaylist(url: String, pluginId: String? = null): List<MusicItem> {
        val plugins = if (pluginId != null) {
            _activePlugins.value.filter { it.pluginId == pluginId }
        } else {
            _activePlugins.value
        }

        for (plugin in plugins) {
            try {
                val rawResult = plugin.importMusicSheet(url)
                val items = parseMusicItems(rawResult, plugin.pluginId)
                if (items.isNotEmpty()) {
                    return items
                }
            } catch (e: Exception) {
                Log.w(TAG, "Import failed for plugin ${plugin.pluginId}: ${e.message}")
            }
        }
        return emptyList()
    }

    /**
     * Install plugin from URL
     */
    suspend fun installFromUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val content = pluginRepository.downloadPlugin(url)
            if (content != null) {
                val pluginId = url.substringAfterLast("/").substringBefore(".")
                pluginRepository.savePlugin(pluginId, content)
                val context = quickJsEngine.createContext(pluginId, content)
                if (context.initialize()) {
                    _activePlugins.value = _activePlugins.value + context
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Install from URL failed: ${e.message}")
            false
        }
    }

    /**
     * Unload a plugin
     */
    fun unloadPlugin(pluginId: String) {
        _activePlugins.value = _activePlugins.value.filter { it.pluginId != pluginId }
        quickJsEngine.removeContext(pluginId)
    }

    /**
     * Parse JSON music items from plugin response
     */
    private fun parseMusicItems(rawJson: String, pluginId: String): List<MusicItem> {
        return try {
            val jsonStr = rawJson.trim()
            if (jsonStr.isEmpty() || jsonStr == "[]" || jsonStr == "{}") {
                return emptyList()
            }
            // Try to parse as array first
            val items = try {
                json.decodeFromString<List<Map<String, Any>>>(jsonStr)
            } catch (e: Exception) {
                // Try to parse as object with list field
                try {
                    val obj = json.decodeFromString<Map<String, Any>>(jsonStr)
                    @Suppress("UNCHECKED_CAST")
                    (obj["data"] as? List<Map<String, Any>>)
                        ?: (obj["items"] as? List<Map<String, Any>>)
                        ?: (obj["list"] as? List<Map<String, Any>>)
                        ?: emptyList()
                } catch (e2: Exception) {
                    emptyList()
                }
            }

            items.mapNotNull { item ->
                try {
                    MusicItem(
                        id = (item["id"] as? String) ?: (item["musicId"] as? String) ?: "",
                        name = (item["name"] as? String) ?: (item["title"] as? String) ?: "Unknown",
                        artist = (item["artist"] as? String) ?: (item["singer"] as? String) ?: "",
                        album = (item["album"] as? String) ?: "",
                        duration = (item["duration"] as? Number)?.toLong()
                            ?: (item["duration"] as? String)?.toLongOrNull()?.times(1000)
                            ?: 0,
                        cover = (item["cover"] as? String)
                            ?: (item["picUrl"] as? String)
                            ?: (item["img"] as? String)
                            ?: "",
                        url = (item["url"] as? String) ?: "",
                        lyricId = (item["lyricId"] as? String) ?: "",
                        pluginId = pluginId,
                        source = pluginId
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse music items failed: ${e.message}")
            emptyList()
        }
    }

    private fun getDemoResults(keyword: String): List<MusicItem> {
        return listOf(
            MusicItem(
                id = "demo_1",
                name = "$keyword - Track 1",
                artist = "Demo Artist",
                album = "Demo Album",
                duration = 240000,
                pluginId = "demo",
                source = "demo"
            ),
            MusicItem(
                id = "demo_2",
                name = "$keyword - Track 2",
                artist = "Demo Artist 2",
                album = "Demo Album 2",
                duration = 195000,
                pluginId = "demo",
                source = "demo"
            )
        )
    }

    fun destroy() {
        _activePlugins.value.forEach { it.close() }
        _activePlugins.value = emptyList()
        quickJsEngine.destroy()
    }
}
