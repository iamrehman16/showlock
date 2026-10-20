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
- `sdkmanager` is deprecated. Use the `android` CLI (`~/Android/Sdk/cmdline-tools/latest/bin/android`). `~/.androidrc` sets the SDK path and `--no-metrics`.
- Test phone: Xiaomi 2409BRN2CA, Android 16 (API 36), HyperOS 3.

## Skills (official Google, in `.claude/skills/`, gitignored)
Reinstall with `android skills add --agent=claude-code --project=. android-cli navigation-event edge-to-edge`.
- `android-cli`: screenshots (`android screen capture`), UI dumps (`android layout`), docs search (`android docs search`). Use it to check a milestone on the phone.
- `navigation-event`: back handling on SDK 36+ (Flow D).
- `edge-to-edge`: system bars and insets (hiding bars while LOCKED).
- Notes checked against official docs: `docs/platform-notes.md`.

## Commands
- Build: `./gradlew assembleDebug`
- Lint: `./gradlew lint` (run both after every change and fix failures before calling a milestone done)
- Install: `./gradlew installDebug`
- Launch: `adb shell monkey -p io.github.iamrehman16.showlock 1`
- Check the manifest permissions: `aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk`

## Hard rules (from mvp.md)
- The only manifest permissions are `USE_BIOMETRIC` and androidx.core's own signature-level `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (approved by the user). No INTERNET, storage, analytics or crash reporting.
- Allowed dependencies: Compose, Coil, androidx.biometric, Media3 (M2b only), plus what the toolchain needs. Ask before adding anything else.
- Never read the gallery, persist URI grants, or copy photos into app storage.
- Check screen pinning, lock task and biometric behavior against developer.android.com instead of relying on memory.
- Write comments only where the intent isn't obvious. No TODO stubs presented as finished work.
- Post-MVP: an "auto-lock on open" option is planned, so keep the locked state easy to set at launch.
