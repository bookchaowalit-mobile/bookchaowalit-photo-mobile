# Upgrade plan

## Current state

Score: 5/10 (was 4/10) — tested pure-Kotlin domain core and honest CI; the
Compose UI is still a placeholder and the Android build is unverified locally.

## Backlog

- P0: Confirm the `android` CI job is green (AGP 8.5 + Kotlin 2.0.21 Compose
  plugin + material dependency were fixed statically, without an Android SDK).
- P0: Replace the placeholder Home/Explore screens with UI backed by `core`
  (ViewModel + state holder calling the `core` API).
- P1: Load photos from MediaStore (READ_MEDIA_IMAGES) and render the justified grid with `core` sizes.
- P1: Apply crop/rotate with `Bitmap` and save a copy (never overwrite originals).
- P2: Compose UI tests (`androidx.compose.ui:ui-test-junit4`) for the main flow.
- P2: Pin `distributionSha256Sum` in `gradle-wrapper.properties`.

## Done in this pass (pass 1)

- Added `core/` (included Gradle build) with the app's domain logic and 5 unit tests.
- Committed the Gradle wrapper (8.10.2).
- Fixed Kotlin 2.0 Compose setup: `org.jetbrains.kotlin.plugin.compose`
  replaces the removed `composeOptions.kotlinCompilerExtensionVersion`; Java/Kotlin
  target 17; added `com.google.android.material` for the manifest theme.
- CI: removed `|| true`; separate `core` and `android` jobs.
- README now states what is verified and what is not.

## Done in this pass (pass 2)

- Fixed: a favourite photo that was also in a user album named "Favorites" was counted twice, and `inAlbum("Favorites")` ignored that user album; counts and listing now agree (union, one count per photo).
- 7 more edge-case tests (12 total): Favorites merge, empty library, timeline zone, size/rotation validation, crops of extreme images stay in bounds, 1px fit, justified rows keep order and exact widths. Verified with `./gradlew -p core test --offline`.
- Still not verified locally: the Compose `app/` (no Android SDK; dl.google.com is blocked here).

## Done in this pass (pass 3)

- Fixed duplicate photo ids (same photo synced twice) being counted twice in `albumCounts` and listed twice in `inAlbum`/`timeline`, contradicting "counted at most once per album".
- Album names are trimmed and blank / zero-width-only names (`""`, `"​"`) are ignored instead of appearing as an empty album; album list is sorted case-insensitively ("beach" no longer sorts after "Zoo").
- Fixed `justifiedRows` overflow: with a narrow container the gaps alone could exceed the width, producing rows wider than the container; rows now close while each photo still gets 1px.
- 4 regression tests (16 total), verified with `./gradlew -p core test --offline`.
