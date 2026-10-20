package io.github.iamrehman16.showlock

import android.net.Uri
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

private const val MAX_SCALE = 4f
private const val DOUBLE_TAP_SCALE = 2.5f

/**
 * A photo that can be pinch-zoomed, double-tap-zoomed and panned.
 *
 * At scale 1 a one-finger drag is left unconsumed so the pager can swipe.
 * Two-finger gestures, and every drag while zoomed in, are consumed here.
 */
@Composable
fun ZoomableImage(
    uri: Uri,
    contentDescription: String,
    onZoomedChange: (Boolean) -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val reportZoomed by rememberUpdatedState(onZoomedChange)

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val center = Offset(width / 2, height / 2)

        fun clamp(o: Offset, s: Float): Offset {
            val maxX = width * (s - 1) / 2
            val maxY = height * (s - 1) / 2
            return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
        }

        // Keeps the content point under `focus` fixed on screen while the scale changes.
        fun zoomTo(newScale: Float, focus: Offset, pan: Offset = Offset.Zero) {
            val s = newScale.coerceIn(1f, MAX_SCALE)
            val factor = s / scale
            offset = clamp((focus - center) * (1 - factor) + offset * factor + pan, s)
            scale = s
            reportZoomed(s > 1f)
        }

        // Decode at up to twice the screen size so zoomed photos stay reasonably sharp.
        val request = ImageRequest.Builder(LocalPlatformContext.current)
            .data(uri)
            .size(constraints.maxWidth * 2, constraints.maxHeight * 2)
            .build()

        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tap ->
                            if (scale > 1f) zoomTo(1f, center) else zoomTo(DOUBLE_TAP_SCALE, tap)
                        },
                    )
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val fingers = event.changes.count { it.pressed }
                            // Unspecified (NaN) when the last finger lifts; using it would blank the image.
                            val centroid = event.calculateCentroid(useCurrent = false)
                            if (centroid.isSpecified && (fingers >= 2 || scale > 1f)) {
                                zoomTo(
                                    newScale = scale * event.calculateZoom(),
                                    focus = centroid,
                                    pan = event.calculatePan(),
                                )
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}
