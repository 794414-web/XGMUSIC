package com.xgmusic.app.plugin

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * QuickJS Engine Bridge
 *
 * Since we cannot bundle native QuickJS .so in this initial version,
 * this implementation uses a lightweight in-process JavaScript evaluation
 * approach. In production, this should use the actual QuickJS native library
 * via JNI. For now, we use a Kotlin-based JS evaluator as a placeholder
 * that can parse and execute the music plugin protocol.
 */
class QuickJsEngine {

    companion object {
        private const val TAG = "QuickJsEngine"
        private const val MAX_MEMORY_BYTES = 15 * 1024 * 1024 // 15MB per context
    }

    private val contexts = mutableMapOf<String, PluginContext>()

    /**
     * Create a new JS context for a plugin
     */
    fun createContext(pluginId: String, scriptContent: String): PluginContext {
        val context = PluginContext(pluginId, scriptContent)
        contexts[pluginId] = context
        Log.d(TAG, "Created context for plugin: $pluginId")
        return context
    }

    /**
     * Remove a plugin context
     */
    fun removeContext(pluginId: String) {
        contexts.remove(pluginId)
        Log.d(TAG, "Removed context for plugin: $pluginId")
    }

    /**
     * Check if a context exists
     */
    fun hasContext(pluginId: String): Boolean = contexts.containsKey(pluginId)

    /**
     * Get or create context
     */
    fun getContext(pluginId: String): PluginContext? = contexts[pluginId]

    /**
     * Destroy all contexts
     */
    fun destroy() {
        contexts.values.forEach { it.close() }
        contexts.clear()
    }
}

/**
 * Represents a JavaScript execution context for a single plugin.
 *
 * In the full implementation, this wraps a QuickJS JSRuntime + JSContext.
 * The plugin script is loaded and evaluated, exposing the standard plugin
 * interface functions (searchMusic, getMusicUrl, getLyric, importMusicSheet).
 */
class PluginContext(
    val pluginId: String,
    private val scriptContent: String
) {
    companion object {
        private const val TAG = "PluginContext"
    }

    private var isInitialized = false
    private var exposedFunctions = mutableMapOf<String, JsFunction>()

    // Native callbacks that JS can call into Kotlin
    private val nativeCallbacks = NativeCallbacks()

    suspend fun initialize(): Boolean = withContext(Dispatchers.Default) {
        try {
            // In a full implementation, this would:
            // 1. Create QuickJS JSRuntime
            // 2. Create JSContext
            // 3. Eval the plugin script
            // 4. Register native callbacks (fetch, localStorage, etc.)
            // 5. Expose plugin functions (searchMusic, getMusicUrl, etc.)

            exposedFunctions = parsePluginFunctions(scriptContent)
            isInitialized = true
            Log.d(TAG, "Plugin $pluginId initialized with ${exposedFunctions.size} functions")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize plugin $pluginId: ${e.message}", e)
            false
        }
    }

    /**
     * Execute a searchMusic call
     */
    suspend fun searchMusic(keyword: String, page: Int): String {
        return callFunction("searchMusic", keyword, page.toString())
    }

    /**
     * Execute getMusicUrl call
     */
    suspend fun getMusicUrl(id: String): String {
        return callFunction("getMusicUrl", id)
    }

    /**
     * Execute getLyric call
     */
    suspend fun getLyric(id: String): String {
        return callFunction("getLyric", id)
    }

    /**
     * Execute importMusicSheet call
     */
    suspend fun importMusicSheet(url: String): String {
        return callFunction("importMusicSheet", url)
    }

    /**
     * Execute getMusicDetail call (optional)
     */
    suspend fun getMusicDetail(id: String): String {
        return callFunction("getMusicDetail", id)
    }

    private suspend fun callFunction(name: String, vararg args: String): String {
        if (!isInitialized) {
            throw IllegalStateException("Plugin not initialized")
        }
        val function = exposedFunctions[name]
            ?: throw IllegalArgumentException("Function $name not found in plugin $pluginId")

        return withContext(Dispatchers.Default) {
            try {
                function.execute(args.toList())
            } catch (e: Exception) {
                Log.e(TAG, "Error calling $name in plugin $pluginId: ${e.message}", e)
                "[]"
            }
        }
    }

    private fun parsePluginFunctions(script: String): Map<String, JsFunction> {
        val functions = mutableMapOf<String, JsFunction>()

        // Parse the plugin script to find exported functions
        // In a real QuickJS implementation, we'd eval the script
        // and extract the function references from the JS context

        val exportedFunctions = listOf(
            "searchMusic",
            "getMusicUrl",
            "getLyric",
            "importMusicSheet",
            "getMusicDetail"
        )

        for (funcName in exportedFunctions) {
            if (script.contains(funcName)) {
                functions[funcName] = object : JsFunction {
                    override suspend fun execute(args: List<String>): String {
                        // In production: use QuickJS JNI to call the JS function
                        return executeJsFunction(funcName, args)
                    }
                }
            }
        }

        return functions
    }

    /**
     * Execute a JavaScript function via QuickJS (native implementation)
     *
     * This is where the actual QuickJS JNI call happens.
     * In the full implementation:
     * 1. Get the JS function reference from the context
     * 2. Convert Kotlin args to JS values
     * 3. Call the function
     * 4. Convert JS result back to Kotlin String
     */
    private suspend fun executeJsFunction(name: String, args: List<String>): String {
        // TODO: Implement actual QuickJS JNI call
        // For now, return a placeholder that simulates the response
        // The actual implementation would be:
        //   val result = quickJs.callFunction(name, args.map { JsValue(it) })
        //   return result.asString()

        return when (name) {
            "searchMusic" -> simulateSearchResponse(args[0])
            "getMusicUrl" -> simulateUrlResponse(args[0])
            "getLyric" -> simulateLyricResponse(args[0])
            "importMusicSheet" -> simulatePlaylistResponse(args[0])
            else -> "[]"
        }
    }

    private fun simulateSearchResponse(keyword: String): String {
        return """
        [
            {"id":"qm_001","name":"$keyword - Demo","artist":"Demo Artist","album":"Demo Album","duration":240000,"cover":"","url":"","pluginId":"$pluginId","source":"demo"}
        ]
        """.trimIndent()
    }

    private fun simulateUrlResponse(id: String): String {
        return """{"url":"","id":"$id"}"""
    }

    private fun simulateLyricResponse(id: String): String {
        return """[00:00.00]歌词加载中..."""
    }

    private fun simulatePlaylistResponse(url: String): String {
        return "[]"
    }

    fun isActive(): Boolean = isInitialized

    fun close() {
        isInitialized = false
        exposedFunctions.clear()
        Log.d(TAG, "Context closed for plugin: $pluginId")
    }
}

/**
 * Interface for JS functions
 */
interface JsFunction {
    suspend fun execute(args: List<String>): String
}

/**
 * Native callbacks exposed to JavaScript plugins
 * These replace browser APIs like fetch, localStorage, etc.
 */
class NativeCallbacks {

    /**
     * Polyfill for fetch() - uses OkHttp under the hood
     */
    suspend fun fetch(url: String, options: Map<String, Any>? = null): String {
        // In full implementation: delegate to OkHttp client
        return "{}"
    }

    /**
     * Polyfill for localStorage
     */
    private val localStorage = mutableMapOf<String, String>()

    fun localStorageGet(key: String): String? = localStorage[key]
    fun localStorageSet(key: String, value: String) { localStorage[key] = value }
    fun localStorageRemove(key: String) { localStorage.remove(key) }
}
