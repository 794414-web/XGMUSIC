package com.xgmusic.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferenceRepository(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "xgmusic_settings")

        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val KEY_CACHE_SIZE = intPreferencesKey("cache_size_mb")
        val KEY_AUTO_CLEANUP = booleanPreferencesKey("auto_cleanup")
        val KEY_CLEANUP_THRESHOLD = intPreferencesKey("cleanup_threshold_mb")
        val KEY_SUBSCRIBE_URL = stringPreferencesKey("subscribe_url")
        val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val KEY_PLAYBACK_MODE = stringPreferencesKey("playback_mode")
        val KEY_NOTIFICATION_FULLSCREEN = booleanPreferencesKey("notification_fullscreen")
    }

    val theme: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "dark" }
    val language: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "zh" }
    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFICATION_ENABLED] ?: true }
    val cacheSize: Flow<Int> = context.dataStore.data.map { it[KEY_CACHE_SIZE] ?: 100 }
    val autoCleanup: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_CLEANUP] ?: true }
    val cleanupThreshold: Flow<Int> = context.dataStore.data.map { it[KEY_CLEANUP_THRESHOLD] ?: 300 }
    val subscribeUrl: Flow<String> = context.dataStore.data.map { it[KEY_SUBSCRIBE_URL] ?: DEFAULT_URL }
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_FIRST_LAUNCH] ?: true }
    val playbackMode: Flow<String> = context.dataStore.data.map { it[KEY_PLAYBACK_MODE] ?: "sequence" }
    val notificationFullscreen: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFICATION_FULLSCREEN] ?: true }

    suspend fun setTheme(theme: String) = context.dataStore.edit { it[KEY_THEME] = theme }
    suspend fun setLanguage(language: String) = context.dataStore.edit { it[KEY_LANGUAGE] = language }
    suspend fun setNotificationEnabled(enabled: Boolean) = context.dataStore.edit { it[KEY_NOTIFICATION_ENABLED] = enabled }
    suspend fun setCacheSize(size: Int) = context.dataStore.edit { it[KEY_CACHE_SIZE] = size }
    suspend fun setAutoCleanup(enabled: Boolean) = context.dataStore.edit { it[KEY_AUTO_CLEANUP] = enabled }
    suspend fun setCleanupThreshold(threshold: Int) = context.dataStore.edit { it[KEY_CLEANUP_THRESHOLD] = threshold }
    suspend fun setSubscribeUrl(url: String) = context.dataStore.edit { it[KEY_SUBSCRIBE_URL] = url }
    suspend fun setFirstLaunchCompleted() = context.dataStore.edit { it[KEY_IS_FIRST_LAUNCH] = false }
    suspend fun setPlaybackMode(mode: String) = context.dataStore.edit { it[KEY_PLAYBACK_MODE] = mode }
    suspend fun setNotificationFullscreen(enabled: Boolean) = context.dataStore.edit { it[KEY_NOTIFICATION_FULLSCREEN] = enabled }

    companion object {
        private const val DEFAULT_URL = "https://www.imwzh.com/musicfree.json"
    }
}
