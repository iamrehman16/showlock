package io.github.iamrehman16.showlock

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val viewModel: ViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        // On recreation the photos come back from SavedStateHandle, not from the intent.
        if (savedInstanceState == null) viewModel.showPhotos(intent.incomingPhotos())

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val photos by viewModel.photos.collectAsStateWithLifecycle()
                    if (photos.isEmpty()) {
                        PickerScreen(onPhotosPicked = viewModel::showPhotos)
                    } else {
                        // A new photo set gets a fresh pager that starts at the first photo.
                        key(photos) {
                            ViewerScreen(
                                photos = photos,
                                initialIndex = viewModel.currentIndex,
                                onPageSettled = viewModel::onPageSettled,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.showPhotos(intent.incomingPhotos())
    }
}
