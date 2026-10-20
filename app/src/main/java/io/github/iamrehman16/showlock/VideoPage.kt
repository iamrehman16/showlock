package io.github.iamrehman16.showlock

import android.net.Uri
import android.view.View
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar

/**
 * One video in the pager, with play/pause and a seek bar.
 *
 * A player exists only while this is the settled page and the app is in the foreground,
 * so there is at most one at a time. Leaving the page, turning the screen off or
 * backgrounding the app releases it. There is no autoplay.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPage(
    uri: Uri,
    isCurrentPage: Boolean,
    isPagerScrolling: Boolean,
    onScrubbingChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val active = isCurrentPage && lifecycleState.isAtLeast(Lifecycle.State.STARTED)
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    val reportScrubbing by rememberUpdatedState(onScrubbingChange)

    DisposableEffect(uri, active) {
        if (!active) return@DisposableEffect onDispose {}
        val newPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
        player = newPlayer
        onDispose {
            player = null
            newPlayer.release()
            reportScrubbing(false)
        }
    }

    // Silence the video as soon as a swipe starts; release follows once the pager settles elsewhere.
    LaunchedEffect(isPagerScrolling) {
        if (isPagerScrolling) player?.pause()
    }

    AndroidView(
        // Keeps the seek bar and play button clear of the navigation bar.
        modifier = Modifier.fillMaxSize().windowInsetsPadding(stableSafeArea()),
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                setShowRewindButton(false)
                setShowFastForwardButton(false)
                setShowPreviousButton(false)
                setShowNextButton(false)
                setShowSubtitleButton(false)
                // Playback speed is out of scope, and the API has no switch for its button.
                findViewById<View>(androidx.media3.ui.R.id.exo_settings)?.visibility = View.GONE
                // Paging is disabled while the seek bar is being dragged, so a scrub never changes the page.
                findViewById<DefaultTimeBar>(androidx.media3.ui.R.id.exo_progress)?.addListener(
                    object : TimeBar.OnScrubListener {
                        override fun onScrubStart(timeBar: TimeBar, position: Long) = reportScrubbing(true)
                        override fun onScrubMove(timeBar: TimeBar, position: Long) = Unit
                        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) =
                            reportScrubbing(false)
                    },
                )
            }
        },
        update = { view -> view.player = player },
        onRelease = { view -> view.player = null },
    )
}
