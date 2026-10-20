package io.github.iamrehman16.showlock

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : FragmentActivity() {

    private val viewModel: ViewerViewModel by viewModels()
    private lateinit var lockController: LockController

    // Set while the system's pinning confirmation may still be on screen.
    private var awaitingPinResult = false
    private var showPinningHint by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        lockController = LockController(this, onUnlocked = viewModel::unlock)
        // On recreation the photos come back from SavedStateHandle, not from the intent.
        if (savedInstanceState == null) viewModel.showPhotos(intent.incomingPhotos())

        setContent {
            ShowLockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val photos by viewModel.photos.collectAsStateWithLifecycle()
                    val locked by viewModel.locked.collectAsStateWithLifecycle()

                    LaunchedEffect(locked) { setSystemBarsHidden(locked) }

                    if (photos.isEmpty()) {
                        PickerScreen(onPhotosPicked = viewModel::showPhotos)
                    } else {
                        // A new photo set gets a fresh pager that starts at the first photo.
                        key(photos) {
                            ViewerScreen(
                                photos = photos,
                                initialIndex = viewModel.currentIndex,
                                locked = locked,
                                onPageSettled = viewModel::onPageSettled,
                                onLock = ::lock,
                                onUnlockRequest = lockController::requestUnlock,
                                onExit = ::exit,
                            )
                        }
                    }

                    if (showPinningHint) {
                        AlertDialog(
                            onDismissRequest = { showPinningHint = false },
                            title = { Text(stringResource(R.string.pinning_hint_title)) },
                            text = { Text(stringResource(R.string.pinning_hint)) },
                            confirmButton = {
                                TextButton(onClick = { showPinningHint = false }) {
                                    Text(stringResource(R.string.ok))
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // A locked photo set must never be replaced, so the intent is dropped entirely.
        if (viewModel.locked.value) return
        setIntent(intent)
        viewModel.showPhotos(intent.incomingPhotos())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Focus returns when the pinning confirmation closes, whether it was accepted or not.
        if (hasFocus && awaitingPinResult) checkPinResultSoon()
    }

    private fun lock() {
        if (!lockController.canAuthenticate()) {
            Toast.makeText(this, R.string.no_screen_lock, Toast.LENGTH_LONG).show()
            return
        }
        viewModel.lock()
        if (lockController.isPinned()) return
        awaitingPinResult = true
        lockController.startPinning()
        // If no confirmation appears (pinning is off in Settings), focus never changes, so check anyway.
        checkPinResultSoon()
    }

    private fun exit() {
        lockController.stopPinning()
        finish()
    }

    private fun checkPinResultSoon() {
        window.decorView.postDelayed({
            if (!awaitingPinResult || !hasWindowFocus()) return@postDelayed
            awaitingPinResult = false
            val prefs = getPreferences(Context.MODE_PRIVATE)
            if (!lockController.isPinned() && !prefs.getBoolean(KEY_PINNING_HINT_SHOWN, false)) {
                prefs.edit { putBoolean(KEY_PINNING_HINT_SHOWN, true) }
                showPinningHint = true
            }
        }, PIN_CHECK_DELAY_MS)
    }

    private fun setSystemBarsHidden(hidden: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (hidden) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private companion object {
        const val KEY_PINNING_HINT_SHOWN = "pinning_hint_shown"
        const val PIN_CHECK_DELAY_MS = 1000L
    }
}
