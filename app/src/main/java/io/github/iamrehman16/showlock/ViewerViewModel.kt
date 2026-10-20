package io.github.iamrehman16.showlock

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class ViewerViewModel(private val state: SavedStateHandle) : ViewModel() {

    val photos: StateFlow<List<Uri>> = state.getStateFlow(KEY_PHOTOS, emptyList())

    val currentIndex: Int
        get() = state[KEY_INDEX] ?: 0

    fun showPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        state[KEY_INDEX] = 0
        state[KEY_PHOTOS] = uris
    }

    fun onPageSettled(index: Int) {
        state[KEY_INDEX] = index
    }

    private companion object {
        const val KEY_PHOTOS = "photos"
        const val KEY_INDEX = "index"
    }
}
