# Stash Android

A native Android client for [Stash](https://stashapp.cc/), built with Jetpack Compose, Material 3, and Kotlin Coroutines. Features deep integration with StashDB and Whisparr, providing a modern, smooth, and feature-rich interface for browsing, managing, and streaming media from your self-hosted Stash server.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20+-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-8.0+_(API_26+)-green.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target_SDK-36-blue.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2024.05.00-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## Features

### 🎬 Media Management & Browsing
- **Scene Browsing**: Paginated grid, compact, and detailed list layouts with full-text search, multi-column sorting (Date, Title, Rating, Duration, File Size), and studio/performer/tag filters.
- **Scene Details & Markers**: Interactive detail views with inline rating, favorite toggle, O-counter incrementing, marker playback timeline, marker creation (`AddMarkerScreen`), and direct StashDB metadata scraping.
- **Interactive Scrubber**: Long-press thumbnail scrubbing with VTT sprite sheet preview frames and haptic feedback.
- **Performers**: Grid and list galleries, career statistics, biographies, external links, and StashDB performer scraping.
- **Studio Hierarchies**: Studio directory with parent/child hierarchy trees, studio details, and related scenes.
- **Tag Organization**: Tag directory, tag detail views, category groupings, and customizable followed tags.

### 🎯 Content Discovery & UI Experience
- **Home Dashboard**: Customizable horizontal carousels (Recently Added, Top Rated, Followed Tags, Studio Spotlights).
- **Customizable Home Layout**: Reorder and toggle visibility of home rows and sections directly from the app.
- **Fluid Shared Element Transitions**: Smooth shared element animations between lists and detail views for scenes, performers, and studios.
- **Material You Dynamic Theming**: Dynamic theme extraction matching Android's system wallpaper palette, with manual Dark/Light/System overrides.
- **Scroll-Responsive Navigation**: Auto-hiding bottom navigation bar on scroll with gesture preservation.

### 🎥 Video Playback
- **ExoPlayer Playback**: Hardware-accelerated streaming powered by AndroidX Media3.
- **Aspect Ratio & Resize Controls**: Toggle between Fit, Fill, and 16:9 aspect ratios.
- **Playback Resume**: Automatic tracking and resuming from last played position.

### 🔄 Integrations & Server Maintenance
- **StashDB Integration**: Direct querying and automatic scraping of scenes, performers, and studios into your local Stash server.
- **Whisparr Integration**: Download queue monitoring, release searching, and library status tags.
- **Server Maintenance**: Trigger Stash library scans and generate metadata (covers, previews, sprites, markers, and transcodes) directly from the Settings tab.
- **Statistics Dashboard**: Real-time statistics overview displaying total scene counts, performers, studios, playtime, and disk usage.

---

## Quick Start

### Prerequisites
- **Android Studio**: Jellyfish, Ladybug, or newer recommended
- **JDK**: Java 17
- **Device / Emulator**: Android 8.0+ (API 26+)

### Build & Run
```bash
# Clone the repository
git clone https://github.com/goonarr-maintainarr/stash-android.git
cd stash-android

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

### Server Configuration
1. Launch the app on your Android device or emulator.
2. Navigate to **Settings**.
3. Enter your Stash server URL (e.g., `http://192.168.1.100:9999/graphql`).
4. Enter your API key (generated in your Stash web UI under **Settings → Security**).
5. Tap **Test Connection**.
6. *(Optional)* Configure **Whisparr** (e.g., `http://192.168.1.100:8787`) and **StashDB** API keys.

---

## Technical Stack & Compatibility

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Language** | Kotlin | 2.1.20 |
| **Android Gradle Plugin** | AGP | 8.7.3 |
| **Minimum SDK** | Android 8.0 (Oreo) | API 26 |
| **Target & Compile SDK** | Android 15/16 | API 36 |
| **Java Compatibility** | OpenJDK | Java 17 |
| **UI Framework** | Jetpack Compose / Material 3 | BOM 2024.05.00 / M3 1.3.0 |
| **Dependency Injection** | Hilt | 2.54 |
| **Networking** | Ktor Client / OkHttp | 3.3.3 / 5.3.2 |
| **Local Database** | Room SQLite | 2.8.4 |
| **Paging** | AndroidX Paging 3 | 3.3.0 |
| **Image Loading** | Coil | 2.7.0 |
| **Media Player** | AndroidX Media3 / ExoPlayer | 1.2.1 |
| **State & Coroutines** | KotlinX Coroutines & Flow | 1.10.2 |

---

## Architecture Overview

The app follows **Clean Architecture** and **MVVM** principles with unidirectional data flow:

```
app/src/main/java/goonarr/stash/
├── core/                  # Core infrastructure and foundation
│   ├── database/          # Room Entities, DAOs (SceneDao, PerformerDao, StudioDao, TagDao)
│   │   ├── dao/           # Compiled SQLite queries
│   │   └── entity/        # Database cache tables
│   ├── model/             # Domain entities (Scene, Performer, Studio, Tag, StashID)
│   └── network/           # GraphQL/REST clients, Ktor engines, interceptors, DTOs
├── features/              # Jetpack Compose UI & ViewModels organized by feature
│   ├── home/              # Home carousels, reorderable sections, discovery flow
│   ├── scenes/            # Scene lists, scene detail, editing, marker creation, scraping
│   ├── performers/        # Performer list, performer detail, editing, scraping
│   ├── studios/           # Studio list, hierarchy view, detail screens, editing
│   ├── tags/              # Tag list, tag detail views, category groupings, editing
│   ├── player/            # ExoPlayer controls, texture views, playback state
│   ├── stashdb/           # StashDB search, scrape dialogs, and match flows
│   ├── stats/             # Server statistics dashboard
│   └── settings/          # Connections, theme selector, sync toggles, server maintenance
└── repositories/          # Repository implementations coordinating network and Room cache
```

---

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines, coding style, and PR workflows.

### Quick Links
- [Open Issues](https://github.com/goonarr-maintainarr/stash-android/issues)
- [Pull Requests](https://github.com/goonarr-maintainarr/stash-android/pulls)
- [Discussions](https://github.com/goonarr-maintainarr/stash-android/discussions)

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
