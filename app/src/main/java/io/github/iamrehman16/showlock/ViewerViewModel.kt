package io.github.iamrehman16.showlock

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class ViewerViewModel(private val state: SavedStateHandle) : ViewModel() {

    val photos: StateFlow<List<Uri>> = state.getStateFlow(KEY_PHOTOS, emptyList())

    val locked: StateFlow<Boolean> = state.getStateFlow(KEY_LOCKED, false)

    val currentIndex: Int
        get() = state[KEY_INDEX] ?: 0

    /** Replaces the photo set, except while locked: a locked set must never change. */
    fun showPhotos(uris: List<Uri>) {
        if (uris.isEmpty() || locked.value) return
        state[KEY_INDEX] = 0
        state[KEY_PHOTOS] = uris
    }

    fun onPageSettled(index: Int) {
        state[KEY_INDEX] = index
    }

    fun lock() {
        state[KEY_LOCKED] = true
    }

    fun unlock() {
        state[KEY_LOCKED] = false
    }

    private companion object {
        const val KEY_PHOTOS = "photos"
        const val KEY_INDEX = "index"
        const val KEY_LOCKED = "locked"
    }
}
