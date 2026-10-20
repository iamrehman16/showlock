package io.github.iamrehman16.showlock

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

/**
 * Returns the photos an intent hands us. Only content:// URIs are kept, because
 * those come with a temporary read grant and need no storage permission.
 */
fun Intent.incomingPhotos(): List<Uri> {
    val uris = when (action) {
        Intent.ACTION_SEND ->
            listOfNotNull(IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java))
        Intent.ACTION_SEND_MULTIPLE ->
            IntentCompat.getParcelableArrayListExtra(this, Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
        Intent.ACTION_VIEW -> listOfNotNull(data)
        else -> emptyList()
    }
    return uris.filter { it.scheme == ContentResolver.SCHEME_CONTENT }.distinct()
}
