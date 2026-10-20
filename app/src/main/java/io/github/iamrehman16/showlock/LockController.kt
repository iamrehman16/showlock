package io.github.iamrehman16.showlock

import android.app.ActivityManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Screen pinning plus the BiometricPrompt that guards unlocking.
 *
 * Pinning starts with the first lock and ends only when the user leaves the viewer.
 * Unlocking just lets the user swipe again.
 *
 * Must be created in onCreate: BiometricPrompt re-attaches its callback there
 * after rotation, so a prompt that was open keeps working.
 */
class LockController(
    private val activity: FragmentActivity,
    private val onUnlocked: () -> Unit,
) {
    private val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            // Pinning stays on: it lasts for the whole session, so re-locking needs no new
            // confirmation and the system's "lock when unpinning" does not fire on every unlock.
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onUnlocked()
            }
        },
    )

    // No negative button: with DEVICE_CREDENTIAL the system offers the PIN fallback instead.
    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(activity.getString(R.string.unlock_title))
        .setAllowedAuthenticators(AUTHENTICATORS)
        .build()

    /** False when the device has no screen lock, so a lock could never be undone. */
    fun canAuthenticate(): Boolean =
        BiometricManager.from(activity).canAuthenticate(AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun requestUnlock() = prompt.authenticate(promptInfo)

    /** Asks the system to pin the task; it shows its own confirmation first. */
    fun startPinning() = activity.startLockTask()

    fun isPinned(): Boolean =
        activity.getSystemService(ActivityManager::class.java).lockTaskModeState ==
            ActivityManager.LOCK_TASK_MODE_PINNED

    /** Ends the session's pinning. The system may lock the device here ("Ask for PIN before unpinning"). */
    fun stopPinning() {
        if (isPinned()) activity.stopLockTask()
    }

    private companion object {
        const val AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    }
}
