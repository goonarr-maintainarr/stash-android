# Stash Android Implementation Plan

## Overview

Convert Stash from iOS (SwiftUI) to Android (Jetpack Compose) following Clean Architecture principles. The iOS app has 148+ presentation layer files and a well-structured architecture with DI, repositories, services, and domain models.

## Current State

### Android Project (Empty)
- Fresh Android Studio project
- Package: `goonarr.stash`
- Min SDK: 24, Target SDK: 36
- Empty source tree
- Basic gradle setup with version catalogs

### iOS Implementation (Reference)
- Architecture: Clean Architecture with DI Container
- Layers: App → Presentation → Domain → Core
- Key Features: Scenes, Performers, Tags, Whisparr, StashDB
- Services: Sync, ImageCache, ImagePrefetch, FloatingVideoPlayer, Subscriptions
- Persistence: GRDB (Stash + Whisparr databases)
- Networking: URLSession with GraphQL + WebSockets
- UI: SwiftUI with 148 view files

## Implementation Strategy

Phased approach starting with infrastructure, then data layer, domain layer, and finally presentation layer. Each phase builds on the previous.

## Phase 1: Project Setup & Configuration (Week 1)

### 1.1 Update Build Configuration

Update gradle files to include all required dependencies:
- Kotlin 2.0+ with serialization plugin
- Jetpack Compose BOM
- Hilt for dependency injection
- Ktor for networking
- Room for local database
- Coil for image loading
- ExoPlayer for video playback
- OkHttp for WebSocket
- DataStore for preferences
- Coroutines and Flow

### 1.2 Configure Hilt

- Create `StashApplication.kt` with `@HiltAndroidApp`
- Configure image pipeline (Coil)
- Configure media cache (ExoPlayer)

### 1.3 Project Structure Setup

Create package structure:
- `app/` (main package)
- `core/di/` - Dependency injection modules
- `core/network/` - Network clients
- `core/database/` - Room database
- `core/util/` - Utilities and extensions
- `data/local/` - Local data sources
- `data/remote/` - Remote data sources
- `data/repository/` - Repository implementations
- `domain/model/` - Domain models
- `domain/repository/` - Repository interfaces
- `domain/usecase/` - Use cases (if needed)
- `presentation/common/` - Shared UI components
- `presentation/features/` - Feature-specific UI
- `presentation/theme/` - Theme and styling

## Phase 2: Core Infrastructure (Week 2-3)

### 2.1 Settings & Persistence

Implement `SettingsStore` using DataStore:
- Server URL
- API key
- Whisparr URL/API key
- StashDB URL/API key
- UI preferences (blur NSFW, prefetch settings, etc.)
- Generation options

Reference: iOS `SettingsStore.swift`

### 2.2 Dependency Injection Modules

Create Hilt modules:
- `NetworkModule` - Ktor client, WebSocket client
- `DatabaseModule` - Room database, DAOs
- `RepositoryModule` - Repository implementations
- `ServiceModule` - Services (ImagePrefetch, Sync, etc.)
- `SettingsModule` - SettingsStore

Reference: iOS `DependencyContainer.swift`

### 2.3 Domain Models

Port domain models from iOS to Kotlin with `@Serializable`:

**Stash Models:**
- `Scene` - Core scene model
- `Performer` - Performer/actor model
- `Tag` - Tag model
- `Studio` - Studio model
- `SceneMarker` - Scene marker/chapter
- `Stats` - Library statistics
- `Job` - Background job status

**Whisparr Models:**
- `WhisparrScene`
- `WhisparrHistory`
- `WhisparrLog`
- `WhisparrRelease`

**StashDB Models:**
- StashDB performer data
- StashDB scene data

**DTOs:**
- `SceneDTO`, `PerformerDTO`, `TagDTO`, `WhisparrDTO`

**Enums:**
- `SceneSortType`, `PerformerSortType`

Reference: iOS `Stash/Domain/Models/`

### 2.4 Utilities

Create utility classes:
- `DateFormatters` - Date formatting helpers
- `HapticManager` - Haptic feedback
- `Logger` extensions
- Extension functions for common operations

Reference: iOS `Stash/Core/Utilities/`

## Phase 3: Data Layer - Database (Week 3-4)

### 3.1 Room Database Setup

Create Room database structure:

**Stash Database:**
- `StashDatabase` - Main database class
- `SceneEntity` - Scene table
- `SceneDetailEntity` - Detailed scene data
- `PerformerEntity` - Performer table
- `PerformerDetailEntity` - Detailed performer data
- `TagEntity` - Tag table
- `CacheMetadataEntity` - Cache timestamps

**DAOs:**
- `SceneDao` - Scene CRUD operations
- `PerformerDao` - Performer CRUD operations
- `TagDao` - Tag CRUD operations
- `MetadataDao` - Cache metadata operations

**Migrations:**
- Define migration strategy from v1 to current

**TypeConverters:**
- JSON converters for complex fields
- Date converters

Reference: iOS `StashDatabase.swift` and related files

### 3.2 Whisparr Database

Create separate Whisparr database:
- `WhisparrDatabase`
- `WhisparrSceneEntity`
- `WhisparrSceneDao`

Reference: iOS `WhisparrDatabase.swift`

## Phase 4: Data Layer - Networking (Week 4-5)

### 4.1 GraphQL Client

Implement `StashClient` with Ktor:
- Generic `fetch()` method for GraphQL queries
- Request/response models
- Error handling with retries
- Exponential backoff
- GraphQL error parsing

Reference: iOS `StashClient.swift`

### 4.2 GraphQL Queries

Port all GraphQL queries:
- `StashQueries` - Scene, Performer, Tag queries
- `StashDBQueries` - StashDB queries

Reference: iOS `StashQueries.swift`, `StashDBQueries.swift`

### 4.3 WebSocket Client

Implement `GraphQLWebSocketClient` with OkHttp:
- WebSocket connection management
- Protocol handshake (graphql-ws)
- Subscription handling
- Reconnection logic with exponential backoff
- State management (Connected, Connecting, Reconnecting, Disconnected, Error)

Reference: iOS `GraphQLWebSocketClient.swift`

### 4.4 SignalR WebSocket (Whisparr)

Implement `SignalRWebSocketClient` for Whisparr live monitoring:
- SignalR protocol support
- Live queue updates
- Event handling

Reference: iOS `SignalRWebSocketClient.swift`

### 4.5 Additional API Clients

- `WhisparrClient` - Whisparr REST API
- `StashDBClient` - StashDB API

Reference: iOS `WhisparrClient.swift`, `StashDBClient.swift`

## Phase 5: Data Layer - Repositories (Week 5-6)

### 5.1 Repository Interfaces

Define repository interfaces in domain layer:
- `SceneRepository`
- `PerformerRepository`
- `TagRepository`
- `WhisparrRepository`
- `StashDBRepository`
- `SettingsRepository`

### 5.2 Repository Implementations

Implement repositories in data layer:

**SceneRepository:**
- `getCachedScenes()` - Load from database
- `fetchScenes(forceRefresh)` - Fetch from API
- `getSceneById(id)` - Get single scene
- `updateScene(scene)` - Update scene
- `deleteScene(id)` - Delete scene
- `observeSceneChanges()` - Flow for database changes
- Cache management with timestamps
- Image prefetching integration

**PerformerRepository:**
- Similar CRUD operations for performers
- Performer matching logic
- StashDB integration

**TagRepository:**
- Tag CRUD operations
- Tag hierarchy handling

**WhisparrRepository:**
- Whisparr scene fetching
- Queue management
- History tracking

**StashDBRepository:**
- Scene scraping from StashDB
- Performer matching
- Metadata enrichment

Reference: iOS `Stash/Domain/Repositories/`

### 5.3 Entity Mapping

Create extension functions:
- `SceneEntity.toDomain()` - Convert entity to domain model
- `Scene.toEntity()` - Convert domain to entity
- Similar for Performer, Tag, etc.

## Phase 6: Services (Week 6-7)

### 6.1 Image Services

**ImagePrefetchService:**
- Prefetch images for scenes/performers
- Queue management
- Coil integration

**ImageCacheService:**
- Cache management
- Cache clearing
- Memory/disk cache stats

Reference: iOS `ImagePrefetchService.swift`, `ImageCacheService.swift`

### 6.2 Sync Service

**SyncService:**
- Coordinate syncing of scenes, performers, tags
- Background sync with WorkManager
- Sync status tracking
- Conflict resolution

Reference: iOS `SyncService.swift`

### 6.3 Subscription Service

**StashSubscriptionService:**
- Manage WebSocket subscriptions
- Scene update notifications
- Job progress updates
- Log streaming

Reference: iOS `StashSubscriptionService.swift`

### 6.4 Floating Video Player Service

**FloatingVideoPlayerService:**
- Manage floating video player state
- Position tracking
- Player lifecycle
- Picture-in-Picture integration

Reference: iOS `FloatingVideoPlayerService.swift`

### 6.5 Whisparr Live Monitor

**WhisparrLiveMonitorService:**
- SignalR connection management
- Queue updates
- Real-time notifications

Reference: iOS `WhisparrLiveMonitorService.swift` (inferred)

## Phase 7: Presentation Layer - Theme & Common (Week 7-8)

### 7.1 Material Design 3 Theme

Create theme configuration:
- `StashTheme` composable
- Color schemes (light/dark)
- Typography
- Shapes
- Dimensions

Reference: iOS `Theme.swift`

### 7.2 Common Composables

Create reusable UI components:
- `SceneCard` - Scene grid/list item
- `PerformerCard` - Performer grid/list item
- `TagChip` - Tag display
- `RatingBar` - Star rating display
- `EmptyState` - Empty list placeholder
- `ErrorState` - Error message with retry
- `LoadingIndicator` - Loading spinner
- `SearchBar` - Search input
- `AsyncImageWithBlur` - Image with NSFW blur
- `VideoThumbnail` - Video thumbnail with play indicator

Reference: iOS `Stash/Presentation/Common/Views/`

### 7.3 Base ViewModels

Create base ViewModel classes:
- `BaseListViewModel<T>` - Generic list handling
- State management patterns
- Error handling

Reference: iOS base ViewModels

## Phase 8: Presentation Layer - Navigation (Week 8)

### 8.1 Navigation Structure

Implement navigation with Compose Navigation:
- `MainNavigation` composable
- Bottom navigation bar with 5 tabs:
  - Home
  - Scenes
  - Performers
  - Whisparr (conditional)
  - Settings
- Navigation routes
- Deep linking support

Reference: iOS `MainTabView.swift`

### 8.2 Navigation Graph

Define routes:
- `home` - Home screen
- `scenes` - Scene list
- `scene/{id}` - Scene detail
- `performers` - Performer list
- `performer/{id}` - Performer detail
- `tags` - Tag list
- `whisparr` - Whisparr queue
- `whisparr/scene/{id}` - Whisparr scene detail
- `stashdb` - StashDB screens
- `settings` - Settings

## Phase 9: Presentation Layer - Home (Week 9)

### 9.1 HomeViewModel

Implement home screen logic:
- Load library stats
- Recent scenes
- Quick actions
- Connection status

Reference: iOS Home feature

### 9.2 HomeScreen Composable

Create home UI:
- Stats cards
- Recent activity
- Quick filters
- Sync button

## Phase 10: Presentation Layer - Scenes (Week 10-11)

### 10.1 SceneListViewModel

Implement scene list logic:
- State management with StateFlow
- Load from cache
- Fetch from server
- Client-side search with debouncing
- Sorting (created_at, date, rating, views, title, random)
- Filtering
- Pull-to-refresh

Reference: iOS `SceneListViewModel.swift`

### 10.2 SceneListScreen Composable

Create scene list UI:
- Search bar
- Sort/filter controls
- LazyVerticalGrid for scenes
- SceneCard for each item
- Pull-to-refresh
- Loading/empty/error states

### 10.3 SceneDetailViewModel

Implement scene detail logic (complex):
- Scene data loading
- Video player management
- Scene marker navigation
- Rating updates
- Play history tracking
- Whisparr integration (search/monitor)
- StashDB scraping
- Performer actions
- Scene operations (delete, generate, etc.)

Reference: iOS `SceneDetailViewModel.swift` and related managers

### 10.4 SceneDetailScreen Composable

Create scene detail UI:
- Video player (ExoPlayer)
- Scene info section
- Performers grid
- Tags
- Studio
- Files info
- Scene markers
- Action buttons
- Sheets/dialogs for actions

## Phase 11: Presentation Layer - Performers (Week 12)

### 11.1 PerformerListViewModel

Implement performer list logic:
- Similar to SceneListViewModel
- Performer-specific sorting/filtering

Reference: iOS `PerformerListViewModel.swift`

### 11.2 PerformerListScreen

Create performer list UI:
- Grid layout
- PerformerCard items

### 11.3 PerformerDetailViewModel

Implement performer detail logic:
- Performer data
- Performer scenes
- StashDB matching
- Edit performer

Reference: iOS `PerformerDetailViewModel.swift`

### 11.4 PerformerDetailScreen

Create performer detail UI:
- Performer image
- Bio/details
- Scenes featuring performer
- StashDB info
- Edit actions

## Phase 12: Presentation Layer - Tags (Week 12)

### 12.1 TagListViewModel & Screen

Implement tag management:
- Tag list with hierarchy
- Tag filtering
- Scenes by tag

Reference: iOS Tags feature

## Phase 13: Presentation Layer - Whisparr (Week 13)

### 13.1 WhisparrListViewModel

Implement Whisparr queue logic:
- Load monitored scenes
- Queue status
- Live updates via SignalR
- Search Whisparr

Reference: iOS Whisparr feature

### 13.2 WhisparrListScreen

Create Whisparr UI:
- Scene list with queue status
- Search interface
- Monitor/unmonitor actions

### 13.3 WhisparrSceneDetailViewModel & Screen

Detailed Whisparr scene view:
- Available releases
- Quality profiles
- Download actions
- History

## Phase 14: Presentation Layer - StashDB (Week 13)

### 14.1 StashDB ViewModels & Screens

Implement StashDB integration:
- Scene search
- Performer search
- Match results
- Scrape actions

Reference: iOS StashDB feature

## Phase 15: Presentation Layer - Settings (Week 14)

### 15.1 SettingsViewModel

Implement settings logic:
- Connection settings
- UI preferences
- Cache management
- Generation options
- About info

Reference: iOS Settings feature

### 15.2 SettingsScreen

Create settings UI:
- Grouped preferences
- Connection test
- Cache clear
- Generation options editor

## Phase 16: Media Handling (Week 14-15)

### 16.1 Video Player Implementation

Implement ExoPlayer integration:
- `VideoPlayer` composable
- Player controls
- Seek bar with scene markers
- Picture-in-Picture support
- Background audio handling
- Player state management

Reference: iOS player implementation

### 16.2 Floating Video Player

Implement floating overlay player:
- Draggable mini-player
- Expand/collapse
- Close action
- Position persistence

Reference: iOS `FloatingVideoPlayerService.swift`

### 16.3 Image Loading Optimization

Optimize Coil configuration:
- Memory cache (25% of app memory)
- Disk cache (150MB)
- Prefetching strategy
- Crossfade animations
- Placeholder/error images

## Phase 17: Advanced Features (Week 15-16)

### 17.1 WebSocket Subscriptions

Integrate real-time updates:
- Scene update subscriptions
- Job progress subscriptions
- Log streaming
- Auto-reconnection

### 17.2 Background Sync

Implement WorkManager jobs:
- Periodic sync
- Constraints (WiFi, charging)
- Foreground service for manual sync

### 17.3 Notifications

Implement notifications:
- Sync completion
- Job completion
- Whisparr downloads
- Error notifications

### 17.4 Additional UI Polish

- Haptic feedback on interactions
- Swipe gestures (refresh, delete)
- Animations and transitions
- Loading skeletons
- Edge-to-edge display

## Phase 18: Testing (Week 16-18)

### 18.1 Unit Tests

Write unit tests:
- ViewModels (all state transitions)
- Repositories (mock dependencies)
- Network clients (mock responses)
- Database operations

Target: 80%+ coverage

### 18.2 Integration Tests

Write integration tests:
- Repository + Database
- Repository + Network
- End-to-end flows

### 18.3 UI Tests

Write Compose UI tests:
- Navigation flows
- User interactions
- State rendering
- Search/filter functionality

## Phase 19: Optimization & Polish (Week 18-19)

### 19.1 Performance Optimization

- Optimize recomposition
- Lazy loading improvements
- Memory profiling
- Database query optimization
- Image loading optimization

### 19.2 Error Handling

- Comprehensive error messages
- Retry mechanisms
- Offline mode support
- Graceful degradation

### 19.3 Accessibility

- Content descriptions
- Screen reader support
- Touch target sizes
- Color contrast

### 19.4 Edge Cases

- Empty states
- Network errors
- Invalid data
- Configuration changes
- Process death

## Phase 20: Final Testing & Release Prep (Week 19-20)

### 20.1 Manual Testing

- Full feature testing
- Cross-device testing (phones/tablets)
- Different Android versions
- Different screen sizes/orientations

### 20.2 Beta Testing

- Internal testing
- Bug fixes
- Performance tuning

### 20.3 Documentation

- README
- Architecture documentation
- API documentation
- User guide

### 20.4 Release Preparation

- ProGuard configuration
- Release signing
- Play Store assets
- Versioning

## Timeline Summary

- **Phase 1-2:** Weeks 1-3 - Project setup, infrastructure, DI, models
- **Phase 3-5:** Weeks 3-7 - Data layer (database, networking, repositories, services)
- **Phase 7-8:** Weeks 7-8 - Presentation foundation (theme, common components, navigation)
- **Phase 9-15:** Weeks 9-16 - Feature implementation (all screens and ViewModels)
- **Phase 16-17:** Weeks 14-16 - Advanced features (video, WebSocket, sync)
- **Phase 18-20:** Weeks 16-20 - Testing, optimization, release prep

**Total: 20 weeks (5 months)**

## Success Criteria

✅ Full feature parity with iOS version
✅ Material Design 3 UI following Android conventions
✅ Performance on par or better than iOS
✅ Proper Android lifecycle handling
✅ 80%+ test coverage
✅ Battery and memory efficient
✅ Support for Android 7.0+ (API 24+)
✅ Picture-in-Picture video support
✅ Background sync with WorkManager
✅ Real-time updates via WebSocket

## Key Technical Decisions

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| UI | Jetpack Compose | Modern declarative UI, similar to SwiftUI |
| DI | Hilt | Compiler-validated, Android-optimized |
| Networking | Ktor | Multiplatform, coroutine-based, clean API |
| Database | Room | Type-safe, compile-time verification |
| Async | Coroutines + Flow | Native Kotlin, efficient |
| Image Loading | Coil | Modern, Kotlin-first, excellent caching |
| Video | ExoPlayer | Industry-standard Android player |
| WebSocket | OkHttp | Reliable, well-tested |
| Preferences | DataStore | Modern replacement for SharedPreferences |
| Serialization | kotlinx.serialization | Type-safe, multiplatform |
