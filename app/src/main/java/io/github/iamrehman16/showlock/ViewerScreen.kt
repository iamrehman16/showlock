package io.github.iamrehman16.showlock

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

@Composable
fun ViewerScreen(
    photos: List<Uri>,
    initialIndex: Int,
    locked: Boolean,
    onPageSettled: (Int) -> Unit,
    onLock: () -> Unit,
    onUnlockRequest: () -> Unit,
    onExit: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    // Only the settled page can be zoomed, since paging is off while zoomed.
    var currentPageZoomed by remember { mutableStateOf(false) }
    var scrubbingVideo by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(pagerState.settledPage) {
        currentPageZoomed = false
        onPageSettled(pagerState.settledPage)
    }

    // While locked, Back is swallowed. While browsing it ends pinning and closes the app;
    // a pinned task would otherwise refuse to close.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = {
            if (locked) Toast.makeText(context, R.string.locked, Toast.LENGTH_SHORT).show() else onExit()
        },
    )

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !locked && !currentPageZoomed && !scrubbingVideo,
            key = { photos[it] },
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val uri = photos[page]
            val isVideo = remember(uri) { context.contentResolver.getType(uri)?.startsWith("video/") == true }
            if (isVideo) {
                VideoPage(
                    uri = uri,
                    isCurrentPage = page == pagerState.settledPage,
                    isPagerScrolling = pagerState.isScrollInProgress,
                    onScrubbingChange = { scrubbingVideo = it },
                )
            } else {
                ZoomableImage(
                    uri = uri,
                    contentDescription = stringResource(R.string.photo_description, page + 1, photos.size),
                    onZoomedChange = { zoomed ->
                        if (page == pagerState.settledPage) currentPageZoomed = zoomed
                    },
                )
            }
        }

        // Overlays stay inside the system bars and camera cutout, and keep their place when the
        // bars hide on lock.
        Box(Modifier.fillMaxSize().windowInsetsPadding(stableSafeArea())) {
            if (photos.size > 1 && !locked) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${photos.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(OverlayBackdrop, MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }

            LockButton(
                locked = locked,
                onClick = if (locked) onUnlockRequest else onLock,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            )
        }
    }
}

/** A labelled amber "Lock" pill while browsing; a small subdued icon while locked, so a guest is not invited to tap it. */
@Composable
private fun LockButton(locked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        // Solid amber stands out on any photo; once locked it shrinks to a dim amber-on-dark icon.
        color = if (locked) OverlayBackdrop else MaterialTheme.colorScheme.primary,
        contentColor = if (locked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.alpha(if (locked) 0.6f else 1f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (locked) 10.dp else 16.dp, vertical = 10.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lock),
                contentDescription = if (locked) stringResource(R.string.unlock) else null,
                modifier = Modifier.size(20.dp),
            )
            if (!locked) {
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.lock), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Dark backdrop that keeps white overlays readable on bright photos. */
private val OverlayBackdrop = Color.Black.copy(alpha = 0.5f)

/** System bars and display cutout, measured as if the bars were shown, so nothing shifts when they hide. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun stableSafeArea(): WindowInsets =
    WindowInsets.systemBarsIgnoringVisibility.union(WindowInsets.displayCutout)
