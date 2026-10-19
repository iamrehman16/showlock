# ShowLock

`mvp.md` is the source of truth. Read it before writing code. Work one milestone at a time and stop after each for review.

## Identity
- App name: ShowLock. applicationId / namespace: `io.github.iamrehman16.showlock`
- Repo: github.com/iamrehman16/showlock (public). License: GPL-3.0.

## Environment (Fedora 44, no Android Studio, no emulator)
- JDK: Temurin 21 at `~/.local/share/jdks/temurin-21` (Fedora 44 has no JDK 17 package). AGP needs JDK 17 or newer.
- SDK: `~/Android/Sdk`. Env vars are in `~/.bashrc.d/android.sh`.
- Build only through `./gradlew`. Test on a real phone over USB (`adb devices`).
- Versions are pinned in `gradle/libs.versions.toml`. AGP 9 has built-in Kotlin support, so do not apply `org.jetbrains.kotlin.android`.

## Commands
- Build: `./gradlew assembleDebug`
- Lint: `./gradlew lint` (run both after every change and fix failures before calling a milestone done)
- Install: `./gradlew installDebug`
- Launch: `adb shell monkey -p io.github.iamrehman16.showlock 1`
- Check the manifest permissions: `aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk`

## Hard rules (from mvp.md)
- The only manifest permission is `USE_BIOMETRIC`. No INTERNET, storage, analytics or crash reporting.
- Allowed dependencies: Compose, Coil, androidx.biometric, Media3 (M2b only), plus what the toolchain needs. Ask before adding anything else.
- Never read the gallery, persist URI grants, or copy photos into app storage.
- Check screen pinning, lock task and biometric behavior against developer.android.com instead of relying on memory.
- Write comments only where the intent isn't obvious. No TODO stubs presented as finished work.
- Post-MVP: an "auto-lock on open" option is planned, so keep the locked state easy to set at launch.
