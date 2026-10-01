# Photo — Mobile

Jetpack Compose Android app for **Photo** — a personal photo gallery with albums and a simple editor.

Part of [Chaowalit Greepoke](https://bookchaowalit.com)'s 101 Portfolio Projects.

## Status

- `core/` — pure-Kotlin domain logic with unit tests. Builds and tests
  without the Android SDK. **This is the verified part of the repo.**
- `app/` — Compose UI shell (Home / Explore / Profile tabs). It depends on
  `core`, but the screens do not use it yet, and the Android build has so far
  only been checked in CI, not locally. See [docs/UPGRADE-PLAN.md](docs/UPGRADE-PLAN.md).

## Core features (`core/`, package `com.bookchaowalit.photo`)

- Image geometry: fit-within without upscaling, centred crops for 1:1 / 4:5 / 16:9 / 3:2, right-angle rotation
- Justified gallery rows (fixed container width, target row height, gaps; rounding absorbed so rows are pixel-exact)
- Timeline grouped by local day (newest first)
- Album counts with a virtual Favorites album and per-album listing

## Tech Stack

- **UI:** Jetpack Compose + Material 3 (Compose BOM 2024.06)
- **Language:** Kotlin 2.0 (Compose compiler Gradle plugin)
- **Android:** AGP 8.5, min SDK 26, target SDK 34
- **Build:** Gradle 8.10 wrapper (committed); `core` is an included build

## Getting Started

Requires JDK 17+.

```bash
./gradlew -p core build          # domain logic + unit tests, no Android SDK needed
./gradlew :app:assembleDebug     # needs the Android SDK (ANDROID_HOME or local.properties)
```

CI (`.github/workflows/build.yml`) runs both; either failing fails the workflow.

## Related

- **Frontend:** [bookchaowalit-website/photo-frontend](https://github.com/bookchaowalit-website/bookchaowalit-photo-frontend)
- **Portfolio:** [bookchaowalit.com](https://bookchaowalit.com)

## License

MIT
