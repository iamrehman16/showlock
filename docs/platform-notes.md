# Platform notes (checked against developer.android.com, 2026-10-19)

Grounding for M2. Quotes are from the linked pages.

## Screen pinning / lock task
Source: https://developer.android.com/work/dpc/dedicated-devices/lock-task-mode

- If an app isn't allowlisted by a device policy controller, `startLockTask()` puts the activity into **screen pinning**, not full lock task mode.
- Pinning "appears similar ... but the person using the device can exit the mode whenever they want." So pinning only protects against a guest when the user has turned on **"Ask for PIN before unpinning"** in system settings. The one-time hint should mention that setting along with App pinning itself.
- `stopLockTask()` "only works for the activity that started lock task mode."
- `ActivityManager.lockTaskModeState`: `LOCK_TASK_MODE_PINNED` means screen pinning, `LOCK_TASK_MODE_LOCKED` means DPC lock task mode, `LOCK_TASK_MODE_NONE` means neither. Read this to find out whether pinning actually started (the user may refuse it, or it may be off in settings).
- Still to check at M2: the confirmation dialog behavior and what happens when pinning is disabled in settings. Read these in the `Activity#startLockTask` reference.

## BiometricPrompt
Source: https://developer.android.com/identity/sign-in/biometric-auth

- `DEVICE_CREDENTIAL` and `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` "aren't supported on Android 10 (API level 29) and lower". This is why minSdk is 30.
- "You can't call `setNegativeButtonText()` and `setAllowedAuthenticators(... or DEVICE_CREDENTIAL)` at the same time". The system's PIN fallback replaces the negative button.
- Before showing the prompt, call `BiometricManager.from(ctx).canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)`, passing the same authenticators. If no screen lock is set, unlocking is impossible, so don't allow locking in that case.
- The guide doesn't mention `USE_BIOMETRIC`. The androidx.biometric library declares it in its own manifest. Confirm in the merged manifest at M2.
