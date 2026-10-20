package io.github.iamrehman16.showlock

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
            userScrollEnabled = !locked && !currentPageZoomed,
            key = { photos[it] },
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            ZoomableImage(
                uri = photos[page],
                contentDescription = stringResource(R.string.photo_description, page + 1, photos.size),
                onZoomedChange = { zoomed ->
                    if (page == pagerState.settledPage) currentPageZoomed = zoomed
                },
            )
        }

        if (photos.size > 1 && !locked) {
            Text(
                text = "${pagerState.currentPage + 1} / ${photos.size}",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        IconButton(
            onClick = if (locked) onUnlockRequest else onLock,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
                // Subdued while locked so it does not invite a guest to tap it.
                .alpha(if (locked) 0.5f else 1f)
                // Dark backdrop keeps the white icon visible on bright photos.
                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lock),
                contentDescription = stringResource(if (locked) R.string.unlock else R.string.lock),
                tint = Color.White,
            )
        }
    }
}
