package goonarr.stash.features.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.features.scenes.utils.SpriteManager
import goonarr.stash.repositories.scenes.SceneRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

val LocalGlobalPlayerState = staticCompositionLocalOf<GlobalPlayerState> {
    error("No GlobalPlayerState provided")
}

@Singleton
class GlobalPlayerState @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sceneRepository: SceneRepository
) {
    // Scope for playback tracking
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var player: ExoPlayer? by mutableStateOf(null)
        private set

    var currentVideoUrl: String? by mutableStateOf(null)
        private set

    var isMinimized: Boolean by mutableStateOf(false)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    // Metadata for the Mini Player
    var currentSceneId: String? by mutableStateOf(null)
    var currentSceneTitle: String? by mutableStateOf(null)
    var currentSceneImage: String? by mutableStateOf(null)

    var currentMarkers: List<SceneMarker> by mutableStateOf(emptyList())
    var currentSpriteManager: SpriteManager? by mutableStateOf(null)
    var currentTintColor: Color? by mutableStateOf(null)

    // Floating Window Geometry
    var floatingPosition: Offset? by mutableStateOf(null)
    var floatingWidth: Dp by mutableStateOf(300.dp)

    // Transition State
    var sourceBounds: androidx.compose.ui.geometry.Rect? by mutableStateOf(null)

    // Watch History Tracker
    private var playbackTracker: PlaybackTracker? = null

    fun initializePlayerIfNeeded() {
        if (player == null) {
            Timber.d("🎥 Initializing ExoPlayer")
            player = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Timber.d("▶️ onIsPlayingChanged: $isPlaying")
                        this@GlobalPlayerState.isPlaying = isPlaying
                        playbackTracker?.onIsPlayingChanged(isPlaying)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val stateName = when (playbackState) {
                            Player.STATE_IDLE -> "IDLE"
                            Player.STATE_BUFFERING -> "BUFFERING"
                            Player.STATE_READY -> "READY"
                            Player.STATE_ENDED -> "ENDED"
                            else -> "UNKNOWN"
                        }
                        Timber.d("📼 onPlaybackStateChanged: $stateName")
                        if (playbackState == Player.STATE_ENDED) {
                            playbackTracker?.onPlaybackEnded()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Timber.e(error, "❌ ExoPlayer Error")
                    }
                })
            }
        }
    }

    fun play(
        url: String,
        sceneId: String,
        title: String?,
        image: String?,
        autoPlay: Boolean = true,
        markers: List<SceneMarker> = emptyList(),
        spriteManager: SpriteManager? = null,
        tintColor: Color? = null,
        startTimeSeconds: Double? = null
    ) {
        initializePlayerIfNeeded()

        // Always update metadata
        currentSceneId = sceneId
        currentSceneTitle = title
        currentSceneImage = image
        currentMarkers = markers
        currentSpriteManager = spriteManager
        currentTintColor = tintColor

        if (currentVideoUrl != url) {
            Timber.d("🎬 Loading new video: $title ($url) [autoPlay=$autoPlay]")
            currentVideoUrl = url
            isMinimized = false

            // Stop existing tracker before starting a new one
            playbackTracker?.stop()
            playbackTracker = null

            player?.setMediaItem(MediaItem.fromUri(url))
            player?.prepare()
            if (startTimeSeconds != null) {
                player?.seekTo((startTimeSeconds * 1000).toLong())
            }
            if (autoPlay) {
                player?.play()
            } else {
                player?.pause() // Ensure it starts paused
            }

            // Initialize new tracker
            playbackTracker = PlaybackTracker(sceneId).apply { start() }
        } else {
            Timber.d("🔄 Resuming/Replaying current video: $title")
            if (startTimeSeconds != null) {
                player?.seekTo((startTimeSeconds * 1000).toLong())
            }
            if (autoPlay) {
                player?.play()
            }
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            Timber.d("⏸️ Pausing playback")
            player?.pause()
        } else {
            Timber.d("▶️ Resuming playback")
            player?.play()
        }
    }

    fun minimize() {
        if (player != null && currentVideoUrl != null) {
            Timber.d("🔽 Minimizing player")
            isMinimized = true
        }
    }

    fun restore() {
        Timber.d("🔼 Restoring player")
        isMinimized = false
    }

    fun dismiss() {
        Timber.d("🛑 Dismissing player")
        playbackTracker?.stop()
        playbackTracker = null
        player?.stop()
        player?.clearMediaItems()
        currentVideoUrl = null
        currentSceneId = null
        isMinimized = false
    }

    fun release() {
        Timber.d("♻️ Releasing player")
        playbackTracker?.stop()
        playbackTracker = null
        player?.release()
        player = null
    }

    /**
     * Tracks playback duration and syncs with the server.
     * Implements the Stash Playback Tracking Behavior Specification:
     * - Sends updates every 10 seconds of accumulated playback time
     * - Increments play count once per session at the first 10-second threshold
     * - Resets resume time to 0 at 98% completion
     */
    private inner class PlaybackTracker(private val sceneId: String) {
        private var trackerJob: Job? = null

        // Seconds since last sync (resets after each sync)
        private var currentPlayDuration: Double = 0.0

        // Total seconds played this session (never resets until new session)
        private var totalPlayDuration: Double = 0.0

        // Only increment play count once per session
        private var playCountIncremented: Boolean = false

        // Constants
        private val syncIntervalSeconds = 10.0
        private val completionThreshold = 0.98

        init {
            Timber.tag("PlaybackTracker").d("🚀 Initialized for scene $sceneId")
        }

        fun start() {
            if (trackerJob?.isActive == true) return
            Timber.tag("PlaybackTracker").d("⏰ Starting tracking loop")
            trackerJob = scope.launch {
                while (isActive) {
                    delay(1000)
                    if (isPlaying) {
                        currentPlayDuration += 1.0
                        totalPlayDuration += 1.0
                        Timber.tag("PlaybackTracker").v(
                            "⏱️ Tick: current=${currentPlayDuration}s, total=${totalPlayDuration}s"
                        )

                        // Check if we should send periodic update (every 10 seconds)
                        if (currentPlayDuration >= syncIntervalSeconds) {
                            sendActivity(isPeriodic = true)
                        }
                    }
                }
            }
        }

        fun stop() {
            Timber.tag("PlaybackTracker").d("🛑 Stopping tracker (accumulated=${currentPlayDuration}s)")
            trackerJob?.cancel()
            trackerJob = null
            // Send final update with any remaining accumulated time
            if (currentPlayDuration > 0) {
                sendActivity(isPeriodic = false)
            }
        }

        fun onIsPlayingChanged(isPlaying: Boolean) {
            Timber.tag("PlaybackTracker").d("⏯️ Playing changed: $isPlaying (accumulated=${currentPlayDuration}s)")
            if (!isPlaying && currentPlayDuration > 0) {
                // Send final update on pause
                sendActivity(isPeriodic = false)
            }
        }

        fun onPlaybackEnded() {
            Timber.tag("PlaybackTracker").d("🏁 Playback ended (accumulated=${currentPlayDuration}s)")
            sendActivity(isPeriodic = false, forceReset = true)
        }

        private fun sendActivity(isPeriodic: Boolean, forceReset: Boolean = false) {
            val durationToSend = currentPlayDuration
            currentPlayDuration = 0.0 // Reset accumulator for next interval

            // Per spec: don't send if no accumulated time (unless forcing reset)
            if (durationToSend <= 0 && !forceReset) {
                Timber.tag("PlaybackTracker").d("⏭️ Skipping sync: no accumulated duration")
                return
            }

            val currentPlayer = player ?: run {
                Timber.tag("PlaybackTracker").w("⚠️ Player is null, cannot sync")
                return
            }
            val currentPos = currentPlayer.currentPosition
            val videoDuration = currentPlayer.duration

            if (videoDuration <= 0) {
                Timber.tag("PlaybackTracker").w("⚠️ Invalid video duration: $videoDuration")
                return
            }

            // Calculate completion percentage
            val percentCompleted = (currentPos.toDouble() / videoDuration.toDouble()) * 100
            val percentPlayed = (totalPlayDuration / (videoDuration / 1000.0)) * 100

            Timber.tag("PlaybackTracker").d(
                "📊 Progress: ${percentCompleted.toInt()}% watched, ${percentPlayed.toInt()}% played"
            )

            // 98% rule: reset resume time if near completion
            val resumeTime = if (percentCompleted >= completionThreshold * 100 || forceReset) {
                Timber.tag("PlaybackTracker").d(
                    "🔄 Near completion (${percentCompleted.toInt()}%), resetting resume time to 0"
                )
                0.0
            } else {
                currentPos / 1000.0
            }

            Timber.tag("PlaybackTracker").d(
                "📡 Syncing activity: resume=${String.format("%.1f", resumeTime)}s, " +
                    "duration=${String.format("%.1f", durationToSend)}s [periodic=$isPeriodic]"
            )

            scope.launch {
                // Increment play count once per session at first threshold
                if (!playCountIncremented && totalPlayDuration >= syncIntervalSeconds) {
                    Timber.tag("PlaybackTracker").d("🎯 First 10s reached, incrementing play count")
                    val newCount = sceneRepository.incrementPlayCount(sceneId)
                    if (newCount >= 0) {
                        playCountIncremented = true
                        Timber.tag("PlaybackTracker").d("✅ Play count incremented to $newCount")
                    } else {
                        Timber.tag("PlaybackTracker").e("❌ Failed to increment play count")
                    }
                }

                // Save activity (resume time + play duration)
                val success = sceneRepository.saveActivity(sceneId, resumeTime, durationToSend)
                if (success) {
                    Timber.tag("PlaybackTracker").d("✅ Activity saved successfully")
                } else {
                    Timber.tag("PlaybackTracker").e("❌ Failed to save activity")
                }
            }
        }
    }
}
