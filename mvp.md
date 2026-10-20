# ShowLock — MVP spec (working name)

An Android app that lets me hand my phone to someone and show only the photos I chose, with no way to swipe to anything else. Photos reach the app through the Android share sheet. Only my fingerprint or phone PIN/pattern unlocks it.

This file is the source of truth for Claude Code. Read it fully before writing code. Build one milestone at a time and stop after each for review.

## 1. Problem

When I lend my phone to show a picture, the other person swipes to the next photo. Screen pinning alone does not fix this, because the person can still swipe inside the pinned gallery. I want a viewer that contains only the photos I picked, can freeze on the current photo, and requires my biometric or device credential to unfreeze.

## 2. Principles

- **Zero data access.** The app never reads the gallery. It only receives photos I explicitly share or pick. No storage permission, no INTERNET permission, no analytics, no ads.
- **Local only.** Personal use and an open-source repo. No Play Store work.
- **Small.** One module, minimal dependencies, readable code. I will read every line, so no clever abstractions.

## 3. Scope

### In the MVP
1. Receive images via share sheet (`ACTION_SEND`, `ACTION_SEND_MULTIPLE`, `image/*`) and via "Open with" (`ACTION_VIEW`, `image/*`).
2. Launcher entry with a "Pick photos" button using the system Photo Picker (`PickMultipleVisualMedia`), so the app is testable without the share sheet.
3. Full-screen viewer: swipe between only the received photos, pinch zoom, double-tap zoom, pan, "2 / 4" indicator.
4. **Lock** button: freezes the viewer on the current photo. No swiping, no leaving the app.
5. **Unlock** via `BiometricPrompt` (fingerprint, or device PIN/pattern/password fallback).
6. Screen pinning (`startLockTask`) started with the lock, stopped on unlock, so Home and Recents are blocked too.
7. **Video (milestone M2b, after the lock works).** Videos shared or picked alongside photos play in the same pager, with basic play/pause and a seek bar. Same lock rules as photos.

### Not in the MVP
Albums, editing, video thumbnails, video fullscreen button, video zoom, delete, share-out, thumbnails grid, settings screen, cloud anything, saving the photo list between sessions, tablet layouts, Play Store listing.

## 4. UX flows

**Flow A, share:** Gallery → select 1+ photos → Share → ShowLock → viewer opens on the first photo in BROWSING state.

**Flow B, lock:** In BROWSING tap the lock icon → state becomes LOCKED, system bars hidden, screen pinning requested (system shows its own confirmation the first time; that is expected) → pager scrolling disabled.

**Flow C, unlock:** In LOCKED tap the small lock icon (top corner, low contrast so it does not invite taps) → BiometricPrompt → on success `stopLockTask()`, state returns to BROWSING.

**Flow D, exit:** In BROWSING, Back closes the app normally. In LOCKED, Back is consumed and does nothing except a brief "Locked" hint.

### State rules
- States: `BROWSING`, `LOCKED`. Held in a `ViewModel` with `SavedStateHandle`: photo URI list, current index, locked flag. Rotation or process recreation must not unlock.
- Zoom and pan are still allowed while LOCKED (it cannot reveal other photos).
- While LOCKED, a new share/view intent arriving (`onNewIntent`) must be ignored. It must never replace the photo set.
- While zoomed in (scale > 1), horizontal drags pan the image instead of changing page. Pager scrolling is re-enabled only at scale 1.
- **Video rules (M2b):** In LOCKED state a video keeps its play/pause and seek controls but cannot be left, same as photos. Only one player exists at a time: create it when a video page becomes the current page, pause and release it when the page changes, the screen turns off, or the app goes to the background. No autoplay on swipe-in unless explicitly decided later. The seek bar must not trigger paging in BROWSING; if it does, disable pager scrolling while a touch is on the seek bar.
- If screen pinning was refused or is not enabled in system settings, the in-app lock still works. Show a one-time hint that explains: "For full protection, turn on App pinning in Settings > Security".

## 5. Tech decisions

- Kotlin, Jetpack Compose (Material 3), single `app` module.
- Image loading: Coil (`coil-compose`). It loads `content://` URIs directly.
- Video (M2b only): Media3 ExoPlayer (`media3-exoplayer` and `media3-ui`). Do not add it before M2b. It plays `content://` URIs directly.
- Biometrics: `androidx.biometric`. Authenticators `BIOMETRIC_STRONG or DEVICE_CREDENTIAL`.
- `minSdk 30` (that authenticator combination is not supported on API 28-29). `compileSdk` and `targetSdk`: latest stable installed via `sdkmanager`.
- Do not persist URI permissions. The temporary read grant from the share intent lasts for the task, which is all we need. Photos are never copied to app storage.
- Permissions in the manifest: only `USE_BIOMETRIC`, plus `<applicationId>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, the signature-level permission androidx.core defines for the app itself (approved 2026-10-20). Anything else needs my approval.
- Verify Android API behaviour (screen pinning, lock task, biometric flags) against developer.android.com instead of assuming from memory.

### Suggested layout
```
app/src/main/java/<package>/
  MainActivity.kt        // intent handling, onNewIntent guard, back handling
  ViewerViewModel.kt     // photos, index, locked flag (SavedStateHandle)
  IncomingIntent.kt      // parse SEND / SEND_MULTIPLE / VIEW into List<Uri>
  ViewerScreen.kt        // pager, zoom, lock/unlock UI
  LockController.kt      // startLockTask/stopLockTask + BiometricPrompt wrapper
  PickerScreen.kt        // "Pick photos" launcher screen
  VideoPage.kt           // M2b: Media3 player for one pager page
```

## 6. Dev environment (Linux, no Android Studio)

Everything runs from the terminal. Editor is whatever I like (VS Code is fine; no Android plugin needed). Testing is on my real phone over USB, with no emulator.

1. JDK 17: `sudo apt install openjdk-17-jdk`
2. Android command-line tools: download "Command line tools only" from developer.android.com/studio, unzip to `~/Android/Sdk/cmdline-tools/latest/`.
3. Environment:
   ```
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
   ```
4. SDK packages (use the current stable platform number): `sdkmanager --licenses` then `sdkmanager "platform-tools" "platforms;android-<N>" "build-tools;<N>.0.0"`
5. Gradle: install once (e.g. via SDKMAN), run `gradle wrapper` in the project, then only use `./gradlew`.
6. Phone: enable Developer options → USB debugging. If `adb devices` shows "unauthorized", accept the prompt on the phone. On Ubuntu/Debian `sudo apt install android-sdk-platform-tools-common` provides the udev rules.
7. Build and install: `./gradlew assembleDebug` and `./gradlew installDebug`. Launch with `adb shell monkey -p <applicationId> 1` or tap the icon.

Claude Code must run `./gradlew assembleDebug` and `./gradlew lint` after changes and fix failures before reporting a milestone done.

## 7. Milestones

**M0, scaffold.** Gradle project builds, installs on my phone, launcher opens a placeholder screen.
Done when: `installDebug` works and the app opens.

**M1, receive and view.** Share-sheet and Open-with intents, "Pick photos" button, pager with zoom/pan, index indicator.
Done when: sharing 1, then 5, photos from the stock gallery shows exactly those photos and no others; pinch zoom does not fight the pager.

**M2, lock.** BROWSING/LOCKED state, lock button, BiometricPrompt unlock, screen pinning, back handling, `onNewIntent` guard, state surviving rotation.
Done when every item in the checklist below passes on my phone.

**M2b, video.** Add Media3, accept `video/*` in the share and Open-with intent filters, switch the Photo Picker to image-and-video mode, add `VideoPage`, apply the video rules above.
Done when: sharing a mix of 2 photos and 2 videos shows all four in one pager, the video checklist below passes, and the lock checklist from M2 still passes with a video as the current page.

**M3, polish.** App icon, README with screenshots and the "why", GPL-3.0 LICENSE (matches Fossify, so the code can later move there), debug APK attached to a GitHub release.

## 8. Manual test checklist (M2)

- [ ] Lock on photo 2 of 4: swiping left/right does nothing.
- [ ] Back press while locked: nothing happens.
- [ ] Rotate while locked: still locked, same photo.
- [ ] Press power, wake the screen: still locked.
- [ ] Press Home while pinned: blocked. Without pinning: lock UI still holds when I return.
- [ ] Unlock with fingerprint works. Unlock with PIN fallback works. Cancel keeps it locked.
- [ ] Sharing again from another app is not possible while locked, and if an intent arrives it does not replace the photos.
- [ ] Locked + zoom in: panning works and does not reveal other photos.
- [ ] Manifest contains only `USE_BIOMETRIC` (plus the androidx `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`).

## 8b. Manual test checklist (M2b, video)

- [ ] Video plays from a share-sheet `content://` URI with sound.
- [ ] Swiping from a playing video to the next page stops its audio immediately.
- [ ] Screen off or app to background pauses playback and releases the player.
- [ ] Dragging the seek bar in BROWSING does not change the page.
- [ ] Locked on a video: play/pause and seek work, swiping and Back do not leave it.
- [ ] Rotating while a video plays or is locked keeps the state (position may reset, but it must stay locked).
- [ ] Unlocking from a video works with fingerprint and PIN fallback.
- [ ] Manifest still contains only `USE_BIOMETRIC` (plus the androidx `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`).

## 9. Rules for Claude Code

- One milestone at a time. After each, summarize what changed and what to test, then wait.
- Keep dependencies to Compose, Coil, `androidx.biometric`, Media3 (M2b only), and what the toolchain needs. Ask before adding anything else.
- No TODO stubs presented as finished work. If something is unclear or blocked, say so.
- Comments only where intent is not obvious from the code.
- Never add network, analytics, crash reporting, or storage permissions.

## 10. Open decisions (ask me before M0)

- App name and `applicationId`.
- Whether I want the app to auto-lock on open (guest-friendly) as an option after the MVP.