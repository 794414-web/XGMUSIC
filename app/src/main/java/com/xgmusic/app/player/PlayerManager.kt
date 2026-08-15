package com.xgmusic.app.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.xgmusic.app.data.model.MusicItem
import com.xgmusic.app.data.model.MusicProgress
import com.xgmusic.app.data.model.PlaybackMode
import com.xgmusic.app.plugin.PluginManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Player Manager
 *
 * Core music playback engine based on ExoPlayer/Media3.
 * Handles: playback, queue management, media session, audio focus.
 */
class PlayerManager(
    private val context: Context,
    private val musicRepository: com.xgmusic.app.data.repository.MusicRepository,
    private val pluginManager: PluginManager
) {

    companion object {
        private const val TAG = "PlayerManager"
        private const val BUFFER_MIN_MS = 30_000L
        private const val BUFFER_MAX_MS = 60_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private val _currentMusic = MutableStateFlow<MusicItem?>(null)
    val currentMusic: StateFlow<MusicItem?> = _currentMusic.asStateFlow()

    private val _progress = MutableStateFlow(MusicProgress())
    val progress: StateFlow<MusicProgress> = _progress.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _queue = MutableStateFlow<List<MusicItem>>(emptyList())
    val queue: StateFlow<List<MusicItem>> = _queue.asStateFlow()

    private val _playbackMode = MutableStateFlow(PlaybackMode.SEQUENCE)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var progressJob: Job? = null
    private var currentIndex: Int = -1

    /**
     * Initialize the player
     */
    fun initialize() {
        if (_isReady.value) return

        player = ExoPlayer.Builder(context)
            .setLoadControl(
                androidx.media3.exoplayer.DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        BUFFER_MIN_MS,
                        BUFFER_MAX_MS,
                        5_000L,
                        10_000L
                    )
                    .build()
            )
            .build()
            .also { exoPlayer ->
                exoPlayer.addListener(playerListener)
            }

        mediaSession = MediaSession.Builder(context, player!!)
            .setCallback(mediaSessionCallback)
            .build()

        _isReady.value = true
        Log.d(TAG, "Player initialized")
    }

    /**
     * Play a single music item
     */
    suspend fun play(music: MusicItem) {
        initialize()
        _currentMusic.value = music

        // Resolve URL if needed
        val url = if (music.url.isEmpty()) {
            pluginManager.getMusicUrl(music.id, music.pluginId) ?: music.id
        } else {
            music.url
        }

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(music.name)
                    .setArtist(music.artist)
                    .setAlbumTitle(music.album)
                    .build()
            )
            .build()

        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        startProgressTracking()
    }

    /**
     * Play a list of music (queue)
     */
    suspend fun playList(musicList: List<MusicItem>, startIndex: Int = 0) {
        initialize()
        _queue.value = musicList
        currentIndex = startIndex

        if (musicList.isNotEmpty()) {
            play(musicList[startIndex])
        }
    }

    /**
     * Play next track
     */
    suspend fun playNext() {
        if (_queue.value.isEmpty()) return
        val nextIndex = when (_playbackMode.value) {
            PlaybackMode.SHUFFLE -> (_queue.value.indices).random()
            PlaybackMode.LOOP_LIST -> (currentIndex + 1) % _queue.value.size
            PlaybackMode.SINGLE -> currentIndex
            PlaybackMode.SEQUENCE, null -> {
                if (currentIndex < _queue.value.size - 1) currentIndex + 1
                else 0
            }
        }
        currentIndex = nextIndex
        play(_queue.value[nextIndex])
    }

    /**
     * Play previous track
     */
    suspend fun playPrevious() {
        if (_queue.value.isEmpty()) return
        val prevIndex = if (currentIndex > 0) currentIndex - 1
        else _queue.value.size - 1
        currentIndex = prevIndex
        play(_queue.value[prevIndex])
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    /**
     * Pause playback
     */
    fun pause() {
        player?.pause()
    }

    /**
     * Resume playback
     */
    fun play() {
        player?.play()
    }

    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    /**
     * Change playback mode
     */
    fun setPlaybackMode(mode: PlaybackMode) {
        _playbackMode.value = mode
    }

    /**
     * Clear queue
     */
    fun clearQueue() {
        _queue.value = emptyList()
        currentIndex = -1
    }

    /**
     * Release resources
     */
    fun release() {
        progressJob?.cancel()
        mediaSession?.run {
            release()
        }
        mediaSession = null
        player?.run {
            stop()
            clearMediaItems()
            release()
        }
        player = null
        _isReady.value = false
        Log.d(TAG, "Player released")
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                player?.let { p ->
                    _progress.value = MusicProgress(
                        current = p.currentPosition,
                        total = p.duration ?: 0L,
                        isPlaying = p.isPlaying,
                        buffered = p.bufferedPosition
                    )
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private val playerListener = object : Player.Listener() {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> Log.d(TAG, "STATE_IDLE")
                Player.STATE_BUFFERING -> Log.d(TAG, "STATE_BUFFERING")
                Player.STATE_READY -> {
                    Log.d(TAG, "STATE_READY")
                    _progress.value = _progress.value.copy(
                        total = player?.duration ?: 0L
                    )
                }
                Player.STATE_ENDED -> {
                    Log.d(TAG, "STATE_ENDED")
                    scope.launch {
                        if (_playbackMode.value == PlaybackMode.SINGLE) {
                            player?.seekTo(0)
                            player?.play()
                        } else {
                            playNext()
                        }
                    }
                }
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            Log.e(TAG, "Player error: ${error.message}", error)
        }
    }

    private val mediaSessionCallback = object : MediaSession.Callback() {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                )
                .setAvailablePlayerCommands(
                    MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                )
                .build()
        }

        override fun onPlay(session: MediaSession, controller: MediaSession.ControllerInfo) {
            play()
        }

        override fun onPause(session: MediaSession, controller: MediaSession.ControllerInfo) {
            pause()
        }

        override fun onNext(session: MediaSession, controller: MediaSession.ControllerInfo) {
            scope.launch { playNext() }
        }

        override fun onPrevious(session: MediaSession, controller: MediaSession.ControllerInfo) {
            scope.launch { playPrevious() }
        }

        override fun onSeekTo(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            position: Long
        ) {
            seekTo(position)
        }
    }
}
