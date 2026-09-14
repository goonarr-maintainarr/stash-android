package goonarr.stash.features.player

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import goonarr.stash.R

/**
 * A reusable video player component based on Media3 ExoPlayer.
 *
 * @param modifier The modifier to be applied to the player view.
 * @param url The video URL to play. If null, [externalPlayer] must be provided.
 * @param isMuted Whether the audio should be muted.
 * @param useController Whether to show the standard ExoPlayer playback controls.
 * @param resizeMode The resize mode for the player (e.g., FIT, ZOOM).
 * @param autoPlay Whether playback should start automatically.
 * @param externalPlayer An optional existing [ExoPlayer] instance to use.
 * @param onPlayerCreated Callback invoked when a new internal [ExoPlayer] is created.
 * @param onPlayerViewCreated Callback invoked when the [PlayerView] is created, useful for accessing the underlying surface.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    url: String?,
    isMuted: Boolean = false,
    useController: Boolean = true,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    autoPlay: Boolean = true,
    externalPlayer: ExoPlayer? = null,
    onPlayerCreated: ((ExoPlayer) -> Unit)? = null,
    onPlayerViewCreated: ((PlayerView) -> Unit)? = null
) {
    if (url == null && externalPlayer == null) return

    val context = LocalContext.current

    // Manage player lifecycle
    var internalPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    val player = externalPlayer ?: internalPlayer

    DisposableEffect(url, context, externalPlayer) {
        if (externalPlayer == null && url != null) {
            val exoPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(url.toUri()))
                prepare()
                playWhenReady = autoPlay
                if (isMuted) {
                    volume = 0f
                }
            }
            internalPlayer = exoPlayer
            onPlayerCreated?.invoke(exoPlayer)

            onDispose {
                exoPlayer.release()
            }
        } else {
            onDispose { }
        }
    }

    AndroidView(
        factory = { ctx ->
            (LayoutInflater.from(ctx).inflate(R.layout.view_player_texture, null) as PlayerView).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                this.useController = useController
                this.resizeMode = resizeMode
                onPlayerViewCreated?.invoke(this)
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        modifier = modifier
    )
}
