# AI Agent Maintenance Guide (stash-android)

This document provides orientation, architecture patterns, command references, and maintenance rules for AI agents maintaining this repository.

---

## 1. Project Overview & Architecture

- **Platform**: Android 8.0+ (API 26), Target SDK 36, Compile SDK 36.
- **Language**: Kotlin 2.1.20+ with Coroutines & StateFlow.
- **UI Framework**: Jetpack Compose with Material 3 & Material You dynamic theming.
- **Architecture**: MVVM with Repository Pattern + Unidirectional Data Flow (UDF).
- **Dependency Injection**: Hilt (`@HiltViewModel`, `@Inject`).
- **Persistence**: Room SQLite database (`AppDatabase`) with KSP code generation.
- **Network**: Ktor / OkHttp + GraphQL client for Stash API, REST for Whisparr & StashDB.
- **Video Player**: Media3 / ExoPlayer.

---

## 2. Directory Layout

```
app/src/main/java/goonarr/stash/
├── core/
│   ├── database/       # Room Database, DAOs, Entities, TypeConverters
│   ├── model/          # Domain data classes and enums
│   ├── network/        # GraphQL queries, StashClient, StashDBClient
│   └── services/       # WebSocket subscriptions, SignalR sync
├── features/           # Feature UI & ViewModels
│   ├── home/           # Home dashboard & category rows
│   ├── scenes/         # Scene list, detail, player, scrubber, scraping
│   ├── performers/     # Performer list, detail, editing
│   ├── studios/        # Studio list, detail, scraping
│   ├── tags/           # Tag list, card, filtering
│   ├── whisparr/       # Whisparr integration & release search
│   └── settings/       # Server configuration & sync settings
└── repositories/       # Repositories mediating Network <-> Database cache
```

---

## 3. Essential Maintenance Commands

Always set `ANDROID_HOME` when running build tasks:
```bash
export ANDROID_HOME=/Users/jeremy/Library/Android/sdk
```

### Running Tests
```bash
# Run all unit tests (344 tests)
./gradlew test

# Run a specific test class
./gradlew :app:testDebugUnitTest --tests "goonarr.stash.features.scenes.scrape.SceneScrapeViewModelTest"

# Run a specific test method
./gradlew :app:testDebugUnitTest --tests "goonarr.stash.features.scenes.scrape.SceneScrapeViewModelTest.loadScene updates query based on config"
```

### Linting & Formatting
```bash
# Check code style with ktlint
./gradlew ktlintCheck

# Auto-format Kotlin code
./gradlew ktlintFormat
```

### Compiling APKs
```bash
# Build debug APK for testing & GitHub releases
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

## 4. Key Implementation Rules & Invariants

1. **Zero PII Policy**:
   - Never commit personal names, real email addresses, internal IP addresses (`192.168.x.x`), private tokens, or personal paths into the codebase.
   - Use placeholder URLs like `https://stash.example.com` or `Secrets.serverUrl`.
2. **Room DAO Return Types**:
   - Room KSP does not support nested lists in query returns. Use dedicated flat projection data classes (e.g. `data class SceneStashIds(val stashIds: List<StashID>?)`).
3. **MockK Test Guidelines**:
   - Default relaxed mocks (`mockk(relaxed = true)`) return dummy instances for non-null types. Explicitly mock methods returning nullable domain models (e.g., `coEvery { repo.fetchSceneFromApi(any()) } returns null`).
4. **Scraper & Tag Merging**:
   - When merging scraped tags or performers in ADD mode, always guard against `current.tags` being `null`:
     ```kotlin
     val existingIds = current.tags?.map { it.id } ?: emptyList()
     (existingIds + newTagIds).distinct()
     ```
5. **Git Commit Hygiene**:
   - Use Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`).
   - Maintain author identity: `Goonarr Maintainers <maintainers@goonarr.dev>`.
