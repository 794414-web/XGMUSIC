package com.xgmusic.app.data.repository

import android.content.Context
import android.util.Log
import com.xgmusic.app.data.db.PluginConfigDao
import com.xgmusic.app.data.db.PluginConfigEntity
import com.xgmusic.app.data.model.PluginInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class PluginRepository(
    private val context: Context
) {

    companion object {
        private const val TAG = "PluginRepository"
        private const val PLUGIN_DIR = "plugins"
        private const val DEFAULT_SUBSCRIBE_URL = "https://www.imwzh.com/musicfree.json"
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val pluginDir: File by lazy {
        File(context.filesDir, PLUGIN_DIR).apply { mkdirs() }
    }

    /**
     * Get all locally installed plugins
     */
    suspend fun getLocalPlugins(): List<PluginInfo> = withContext(Dispatchers.IO) {
        val plugins = mutableListOf<PluginInfo>()
        try {
            val files = pluginDir.listFiles { _, name -> name.endsWith(".js") } ?: emptyArray()
            for (file in files) {
                val name = file.nameWithoutExtension
                plugins.add(
                    PluginInfo(
                        id = name,
                        name = name,
                        fileName = file.name,
                        url = file.absolutePath
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list plugins: ${e.message}")
        }
        plugins
    }

    /**
     * Load plugin content from file
     */
    suspend fun loadPluginContent(fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(pluginDir, fileName)
            if (file.exists()) {
                file.readText()
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load plugin $fileName: ${e.message}")
            null
        }
    }

    /**
     * Download plugin from URL
     */
    suspend fun downloadPlugin(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download plugin from $url: ${e.message}")
            null
        }
    }

    /**
     * Save plugin content to local file
     */
    suspend fun savePlugin(pluginId: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(pluginDir, "$pluginId.js")
            file.writeText(content)
            Log.d(TAG, "Plugin saved: $pluginId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save plugin $pluginId: ${e.message}")
            false
        }
    }

    /**
     * Delete a plugin
     */
    suspend fun deletePlugin(pluginId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(pluginDir, "$pluginId.js")
            if (file.exists()) {
                file.delete()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fetch and parse subscribe source
     */
    suspend fun fetchSubscribeSource(url: String): List<PluginInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    parseSubscribeJson(body)
                } else emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch subscribe source: ${e.message}")
            emptyList()
        }
    }

    private fun parseSubscribeJson(json: String): List<PluginInfo> {
        return try {
            val obj = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString<Map<String, Any>>(json)

            @Suppress("UNCHECKED_CAST")
            val plugins = when {
                obj.containsKey("plugins") -> obj["plugins"] as? List<Map<String, Any>>
                obj.containsKey("data") -> obj["data"] as? List<Map<String, Any>>
                else -> null
            } ?: emptyList()

            plugins.map { p ->
                PluginInfo(
                    id = (p["id"] as? String) ?: "",
                    name = (p["name"] as? String) ?: "",
                    url = (p["url"] as? String) ?: "",
                    description = (p["description"] as? String) ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse subscribe JSON: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get the plugin directory path
     */
    fun getPluginDir(): String = pluginDir.absolutePath
}
