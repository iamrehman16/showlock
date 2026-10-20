package io.github.iamrehman16.showlock

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ViewerScreen(
    photos: List<Uri>,
    initialIndex: Int,
    onPageSettled: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    // Only the settled page can be zoomed, since paging is off while zoomed.
    var currentPageZoomed by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.settledPage) {
        currentPageZoomed = false
        onPageSettled(pagerState.settledPage)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !currentPageZoomed,
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

        if (photos.size > 1) {
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
    }
}
