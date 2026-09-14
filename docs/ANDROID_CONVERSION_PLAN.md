# Android Conversion Plan for Stash
## Technical Design Document

**Version:** 1.0  
**Date:** December 24, 2024  
**Target Platform:** Android 8.0+ (API 26+)  
**Language:** Kotlin 2.0+

---

## Executive Summary

This document outlines a comprehensive conversion strategy for Stash, a native iOS media client for the Stash server, to Android using modern frameworks and architectural patterns. The conversion maintains feature parity while leveraging Android's ecosystem strengths.

### Key Technology Choices

- **UI Framework:** Jetpack Compose (declarative UI, similar to SwiftUI)
- **Architecture:** MVVM + Clean Architecture with Repository Pattern
- **Dependency Injection:** Hilt (Dagger-based, compiler-validated)
- **Networking:** Ktor Client (multiplatform, coroutine-based)
- **Database:** Room (SQLite abstraction with compile-time verification)
- **Reactive Programming:** Kotlin Coroutines + Flow (similar to Combine)
- **Image Loading:** Coil (modern, Kotlin-first, aggressive caching)
- **Video Playback:** ExoPlayer (Android's premier media player)
- **WebSocket:** OkHttp WebSocket + Ktor WebSocket
- **State Management:** StateFlow + MutableStateFlow
- **Navigation:** Compose Navigation

---

## 1. Architecture Overview

### 1.1 Layer Structure

The Android app will follow Clean Architecture principles matching the iOS implementation:

```
app/
├── presentation/          # UI Layer (Composables + ViewModels)
│   ├── common/
│   ├── features/
│   │   ├── home/
│   │   ├── scenes/
│   │   ├── performers/
│   │   ├── settings/
│   │   ├── stashdb/
│   │   └── whisparr/
│   └── theme/
├── domain/               # Business Logic Layer
│   ├── models/
│   ├── repositories/
│   └── usecases/
├── data/                 # Data Layer
│   ├── local/           # Room Database
│   ├── remote/          # Network Clients
│   └── repository/      # Repository Implementations
├── core/                 # Core Utilities
│   ├── di/              # Dependency Injection
│   ├── network/
│   └── utils/
└── MainActivity.kt
```

### 1.2 Architecture Comparison

| iOS Component | Android Equivalent |
|--------------|-------------------|
| SwiftUI Views | Jetpack Compose Composables |
| @StateObject/@ObservedObject | collectAsState() with StateFlow |
| Combine Publishers | Kotlin Flow |
| @Published | MutableStateFlow |
| @AppStorage | DataStore (Preferences) |
| UserDefaults | SharedPreferences / DataStore |
| AVPlayer | ExoPlayer |
| NSCache | LruCache + DiskLruCache |
| GRDB | Room Database |
| URLSession | Ktor Client / OkHttp |
| DependencyContainer | Hilt Modules |

---

## 2. Core Components Conversion

### 2.1 Application Entry Point

**iOS: `StashApp.swift`**
```swift
@main
struct StashApp: App {
    @StateObject private var container: DependencyContainer
    // ...
}
```

**Android: `StashApplication.kt`**
```kotlin
@HiltAndroidApp
class StashApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configure image pipeline (Coil)
        configureImagePipeline()
        
        // Configure ExoPlayer cache
        configureMediaCache()
    }
    
    private fun configureImagePipeline() {
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(150 * 1024 * 1024) // 150 MB
                        .build()
                }
                .build()
        )
    }
}
```

**Android: `MainActivity.kt`**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle Picture-in-Picture for video
        enableEdgeToEdge()
        
        setContent {
            StashTheme {
                MainNavigation()
            }
        }
    }
    
    override fun onUserLeaveHint() {
        // Enter PiP mode when user goes to home screen during video playback
        if (shouldEnterPiP) {
            enterPictureInPictureMode(createPipParams())
        }
    }
}
```

### 2.2 Dependency Injection

**iOS: `DependencyContainer.swift`**
- Lazy property initialization
- Manual factory methods
- Protocol-based injection

**Android: Hilt Modules**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 30_000
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }
    }
    
    @Provides
    @Singleton
    fun provideStashClient(
        client: HttpClient,
        settings: SettingsStore
    ): StashClient {
        return StashClient(client, settings)
    }
    
    @Provides
    @Singleton
    fun provideWebSocketClient(): GraphQLWebSocketClient {
        return GraphQLWebSocketClient()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideStashDatabase(@ApplicationContext context: Context): StashDatabase {
        return Room.databaseBuilder(
            context,
            StashDatabase::class.java,
            "stash.db"
        )
            .addMigrations(*StashDatabase.MIGRATIONS)
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideSceneDao(database: StashDatabase): SceneDao {
        return database.sceneDao()
    }
    
    @Provides
    fun providePerformerDao(database: StashDatabase): PerformerDao {
        return database.performerDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideSceneRepository(
        stashClient: StashClient,
        sceneDao: SceneDao,
        settings: SettingsStore,
        imagePrefetchService: ImagePrefetchService
    ): SceneRepository {
        return SceneRepositoryImpl(stashClient, sceneDao, settings, imagePrefetchService)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    
    @Provides
    @Singleton
    fun provideSettingsStore(@ApplicationContext context: Context): SettingsStore {
        return SettingsStore(context)
    }
}
```

### 2.3 Settings & Persistence

**iOS: `SettingsStore.swift` with `@AppStorage`**

**Android: `SettingsStore.kt` with DataStore**

```kotlin
@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = "stash_settings"
    )
    
    // Flow-based reactive properties
    val serverUrl: Flow<String> = dataStore.data
        .map { preferences -> preferences[SERVER_URL] ?: "" }
    
    val apiKey: Flow<String> = dataStore.data
        .map { preferences -> preferences[API_KEY] ?: "" }
    
    val blurNsfw: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[BLUR_NSFW] ?: false }
    
    val prefetchOnScroll: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PREFETCH_ON_SCROLL] ?: true }
    
    // Generation options as JSON
    val generationOptions: Flow<GenerationOptions> = dataStore.data
        .map { preferences ->
            val json = preferences[GENERATION_OPTIONS] ?: ""
            if (json.isNotEmpty()) {
                Json.decodeFromString(json)
            } else {
                GenerationOptions()
            }
        }
    
    suspend fun updateServerUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[SERVER_URL] = url
        }
    }
    
    suspend fun updateApiKey(key: String) {
        dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }
    
    // Synchronous access for URLs (when needed)
    fun getServerUrlSync(): String? {
        return runBlocking {
            serverUrl.first()
        }
    }
    
    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val API_KEY = stringPreferencesKey("api_key")
        private val BLUR_NSFW = booleanPreferencesKey("blur_nsfw")
        private val PREFETCH_ON_SCROLL = booleanPreferencesKey("prefetch_on_scroll")
        private val GENERATION_OPTIONS = stringPreferencesKey("generation_options")
    }
}

@Serializable
data class GenerationOptions(
    val covers: Boolean = true,
    val sprites: Boolean = true,
    val previews: Boolean = true,
    val imagePreviews: Boolean = true,
    val markers: Boolean = true,
    val phashes: Boolean = true
)
```

---

## 3. UI Layer - Jetpack Compose

### 3.1 Main Navigation

**iOS: `MainTabView.swift` with TabView**

**Android: `MainNavigation.kt` with Bottom Navigation**

```kotlin
@Composable
fun MainNavigation(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val settingsStore = viewModel.settingsStore.collectAsState()
    
    // Floating video player state
    val floatingPlayerState by viewModel.floatingPlayerService
        .playerState
        .collectAsState()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Movie, "Scenes") },
                    label = { Text("Scenes") },
                    selected = currentRoute == "scenes",
                    onClick = { navController.navigate("scenes") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.People, "Performers") },
                    label = { Text("Performers") },
                    selected = currentRoute == "performers",
                    onClick = { navController.navigate("performers") }
                )
                if (settingsStore.value.whisparrUrl.isNotEmpty()) {
                    NavigationBarItem(
                        icon = { 
                            BadgedBox(badge = { 
                                if (whisparrQueue > 0) Badge { Text("$whisparrQueue") }
                            }) {
                                Icon(Icons.Default.VideoLibrary, "Whisparr")
                            }
                        },
                        label = { Text("Whisparr") },
                        selected = currentRoute == "whisparr",
                        onClick = { navController.navigate("whisparr") }
                    )
                }
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen() }
                composable("scenes") { SceneListScreen() }
                composable("performers") { PerformerListScreen() }
                composable("whisparr") { WhisparrSceneListScreen() }
                composable("settings") { SettingsScreen() }
                
                // Detail screens
                composable(
                    "scene/{sceneId}",
                    arguments = listOf(navArgument("sceneId") { type = NavType.StringType })
                ) { backStackEntry ->
                    SceneDetailScreen(
                        sceneId = backStackEntry.arguments?.getString("sceneId") ?: ""
                    )
                }
            }
            
            // Floating video player overlay
            if (floatingPlayerState.isVisible) {
                FloatingVideoPlayer(
                    state = floatingPlayerState,
                    onClose = { viewModel.closeFloatingPlayer() },
                    onExpand = { /* Navigate to full screen */ }
                )
            }
        }
    }
}
```

### 3.2 View Model Pattern

**iOS: `SceneListViewModel.swift`**
```swift
@MainActor
final class SceneListViewModel: BaseListViewModel<Scene> {
    @Published var state: ListViewState<Scene> = .loading
    @Published var searchText: String = ""
    // ...
}
```

**Android: `SceneListViewModel.kt`**
```kotlin
@HiltViewModel
class SceneListViewModel @Inject constructor(
    private val sceneRepository: SceneRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {
    
    // State management with StateFlow
    private val _state = MutableStateFlow<ListViewState<Scene>>(ListViewState.Loading)
    val state: StateFlow<ListViewState<Scene>> = _state.asStateFlow()
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _sortType = MutableStateFlow(SceneSortType.CREATED_AT)
    val sortType: StateFlow<SceneSortType> = _sortType.asStateFlow()
    
    private val _scenes = MutableStateFlow<List<Scene>>(emptyList())
    val scenes: StateFlow<List<Scene>> = _scenes.asStateFlow()
    
    private var allScenes: List<Scene> = emptyList()
    private var cachedScenes: List<Scene> = emptyList()
    
    init {
        observeSearchText()
        observeSortChanges()
        observeDatabaseChanges()
        loadFromCache()
    }
    
    private fun observeSearchText() {
        viewModelScope.launch {
            searchText
                .debounce(500) // Debounce by 500ms
                .distinctUntilChanged()
                .collectLatest { query ->
                    performClientSideSearch(query)
                }
        }
    }
    
    private fun observeSortChanges() {
        viewModelScope.launch {
            sortType
                .drop(1) // Skip initial value
                .distinctUntilChanged()
                .collectLatest { type ->
                    handleSortChange()
                }
        }
    }
    
    private fun observeDatabaseChanges() {
        viewModelScope.launch {
            sceneRepository.observeSceneChanges()
                .collectLatest {
                    loadFromCache()
                }
        }
    }
    
    fun loadFromCache() {
        viewModelScope.launch {
            _state.value = ListViewState.Loading
            
            try {
                val scenes = sceneRepository.getCachedScenes()
                cachedScenes = scenes
                allScenes = scenes
                
                val sorted = sortScenes(scenes)
                _scenes.value = sorted
                _state.value = if (sorted.isEmpty()) {
                    ListViewState.Empty
                } else {
                    ListViewState.Content(sorted)
                }
            } catch (e: Exception) {
                _state.value = ListViewState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun fetchScenes(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val scenes = sceneRepository.fetchScenes(forceRefresh)
                cachedScenes = scenes
                allScenes = scenes
                
                val sorted = sortScenes(scenes)
                _scenes.value = sorted
                _state.value = ListViewState.Content(sorted)
            } catch (e: Exception) {
                _state.value = ListViewState.Error(e.message ?: "Network error")
            }
        }
    }
    
    private suspend fun performClientSideSearch(query: String) {
        val trimmedQuery = query.lowercase().trim()
        
        val filtered = if (trimmedQuery.isEmpty()) {
            cachedScenes
        } else {
            withContext(Dispatchers.Default) {
                cachedScenes.filter { scene ->
                    scene.title?.lowercase()?.contains(trimmedQuery) == true ||
                    scene.details?.lowercase()?.contains(trimmedQuery) == true ||
                    scene.performers?.any { it.name?.lowercase()?.contains(trimmedQuery) == true } == true ||
                    scene.tags?.any { it.name.lowercase().contains(trimmedQuery) } == true ||
                    scene.studio?.name?.lowercase()?.contains(trimmedQuery) == true
                }
            }
        }
        
        allScenes = filtered
        val sorted = sortScenes(filtered)
        _scenes.value = sorted
        _state.value = if (sorted.isEmpty()) {
            ListViewState.Empty
        } else {
            ListViewState.Content(sorted)
        }
    }
    
    private suspend fun sortScenes(scenes: List<Scene>): List<Scene> {
        return withContext(Dispatchers.Default) {
            when (sortType.value) {
                SceneSortType.CREATED_AT -> scenes.sortedByDescending { it.created_at }
                SceneSortType.DATE -> scenes.sortedByDescending { it.date }
                SceneSortType.RATING -> scenes.sortedByDescending { it.rating100 }
                SceneSortType.VIEWS -> scenes.sortedByDescending { it.o_counter }
                SceneSortType.TITLE -> scenes.sortedBy { it.title }
                SceneSortType.RANDOM -> scenes.shuffled()
            }
        }
    }
    
    fun updateSearchText(text: String) {
        _searchText.value = text
    }
    
    fun updateSortType(type: SceneSortType) {
        _sortType.value = type
    }
}

sealed class ListViewState<T> {
    object Loading : ListViewState<Nothing>()
    data class Content<T>(val items: List<T>) : ListViewState<T>()
    object Empty : ListViewState<Nothing>()
    data class Error(val message: String) : ListViewState<Nothing>()
}
```

### 3.3 Composable Views

**iOS: SwiftUI View**
```swift
struct SceneListView: View {
    @ObservedObject var viewModel: SceneListViewModel
    // ...
}
```

**Android: Composable Screen**
```kotlin
@Composable
fun SceneListScreen(
    viewModel: SceneListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scenes") },
                actions = {
                    IconButton(onClick = { /* Show sort dialog */ }) {
                        Icon(Icons.Default.Sort, "Sort")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            SearchBar(
                query = searchText,
                onQueryChange = { viewModel.updateSearchText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Content
            when (state) {
                is ListViewState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ListViewState.Content -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = scenes,
                            key = { it.id }
                        ) { scene ->
                            SceneCard(
                                scene = scene,
                                onClick = { /* Navigate to detail */ }
                            )
                        }
                    }
                }
                is ListViewState.Empty -> {
                    EmptyState(message = "No scenes found")
                }
                is ListViewState.Error -> {
                    ErrorState(
                        message = (state as ListViewState.Error).message,
                        onRetry = { viewModel.fetchScenes(true) }
                    )
                }
            }
        }
    }
}

@Composable
fun SceneCard(
    scene: Scene,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.67f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Image with Coil
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(scene.paths?.screenshot)
                    .crossfade(true)
                    .build(),
                contentDescription = scene.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Title and metadata
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = scene.title ?: "Untitled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                scene.rating100?.let { rating ->
                    RatingBar(rating = rating / 20f) // Convert to 5-star
                }
            }
        }
    }
}
```

---

## 4. Data Layer

### 4.1 Room Database

**iOS: `StashDatabase.swift` with GRDB**

**Android: `StashDatabase.kt` with Room**

```kotlin
@Database(
    entities = [
        SceneEntity::class,
        SceneDetailEntity::class,
        PerformerEntity::class,
        PerformerDetailEntity::class,
        TagEntity::class,
        CacheMetadataEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class StashDatabase : RoomDatabase() {
    
    abstract fun sceneDao(): SceneDao
    abstract fun performerDao(): PerformerDao
    abstract fun tagDao(): TagDao
    abstract fun metadataDao(): MetadataDao
    
    companion object {
        val MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6
        )
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE scenes ADD COLUMN created_at TEXT")
                database.execSQL("ALTER TABLE scenes ADD COLUMN updated_at TEXT")
            }
        }
        
        // Additional migrations...
    }
}

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val details: String?,
    val date: String?,
    val created_at: String?,
    val updated_at: String?,
    val rating100: Int?,
    val o_counter: Int?,
    val pathsJSON: String?,
    val filesJSON: String?,
    val performersJSON: String?,
    val tagsJSON: String?,
    val studioJSON: String?,
    val stash_idsJSON: String?,
    val sceneMarkersJSON: String?,
    val oHistoryJSON: String?,
    val playHistoryJSON: String?
)

@Dao
interface SceneDao {
    
    @Query("SELECT * FROM scenes ORDER BY created_at DESC")
    suspend fun getAllScenes(): List<SceneEntity>
    
    @Query("SELECT * FROM scenes ORDER BY created_at DESC")
    fun observeScenes(): Flow<List<SceneEntity>>
    
    @Query("SELECT * FROM scenes WHERE id = :sceneId")
    suspend fun getSceneById(sceneId: String): SceneEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenes(scenes: List<SceneEntity>)
    
    @Query("DELETE FROM scenes")
    suspend fun deleteAllScenes()
    
    @Query("SELECT * FROM scenes WHERE title LIKE '%' || :query || '%'")
    suspend fun searchScenes(query: String): List<SceneEntity>
}

class DatabaseConverters {
    private val json = Json { ignoreUnknownKeys = true }
    
    @TypeConverter
    fun fromScenePaths(value: ScenePaths?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toScenePaths(value: String?): ScenePaths? {
        return value?.let { json.decodeFromString(it) }
    }
    
    // Additional converters for other JSON types...
}
```

### 4.2 Network Client

**iOS: `StashClient.swift` with URLSession**

**Android: `StashClient.kt` with Ktor**

```kotlin
interface StashClient {
    suspend fun <T> fetch(
        query: String,
        variables: Map<String, Any>,
        url: String,
        apiKey: String
    ): T
    
    suspend fun fetchStats(url: String, apiKey: String): Stats
}

class StashClientImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val settings: SettingsStore
) : StashClient {
    
    private val logger = Logger.getLogger("StashClient")
    
    override suspend fun <T> fetch(
        query: String,
        variables: Map<String, Any>,
        url: String,
        apiKey: String
    ): T = withContext(Dispatchers.IO) {
        var retries = MAX_RETRIES
        var lastException: Exception? = null
        
        while (retries >= 0) {
            try {
                return@withContext performFetch(query, variables, url, apiKey)
            } catch (e: Exception) {
                lastException = e
                
                if (e.shouldRetry() && retries > 0) {
                    val delay = calculateBackoff(MAX_RETRIES - retries)
                    logger.warning("Network error: ${e.message}. Retrying in ${delay}ms...")
                    delay(delay)
                    retries--
                } else {
                    throw e
                }
            }
        }
        
        throw lastException ?: IOException("Request failed")
    }
    
    private suspend inline fun <reified T> performFetch(
        query: String,
        variables: Map<String, Any>,
        url: String,
        apiKey: String
    ): T {
        val request = GraphQLRequest(query, variables)
        
        val response: HttpResponse = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("ApiKey", apiKey)
            setBody(request)
        }
        
        if (response.status.value !in 200..299) {
            throw HttpException(response.status.value, "HTTP error")
        }
        
        val graphQLResponse: GraphQLResponse<T> = response.body()
        
        if (graphQLResponse.errors?.isNotEmpty() == true) {
            throw GraphQLException(graphQLResponse.errors)
        }
        
        return graphQLResponse.data 
            ?: throw IOException("No data in response")
    }
    
    override suspend fun fetchStats(url: String, apiKey: String): Stats {
        logger.info("📊 Fetching library stats")
        val result: StatsResult = fetch(
            query = StashQueries.STATS,
            variables = emptyMap(),
            url = url,
            apiKey = apiKey
        )
        logger.info("✅ Fetched stats: ${result.stats.scene_count} scenes")
        return result.stats
    }
    
    private fun Exception.shouldRetry(): Boolean {
        return this is IOException || 
               this is UnknownHostException ||
               this is SocketTimeoutException
    }
    
    private fun calculateBackoff(attempt: Int): Long {
        return (BASE_DELAY * (1 shl attempt)).coerceAtMost(MAX_DELAY)
    }
    
    companion object {
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY = 1000L
        private const val MAX_DELAY = 8000L
    }
}

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any>
)

@Serializable
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<GraphQLError>?
)

@Serializable
data class GraphQLError(
    val message: String,
    val path: List<String>? = null
)
```

### 4.3 WebSocket Client

**iOS: `GraphQLWebSocketClient.swift` with URLSession WebSocket**

**Android: `GraphQLWebSocketClient.kt` with OkHttp**

```kotlin
class GraphQLWebSocketClient @Inject constructor() {
    
    private val logger = Logger.getLogger("GraphQLWebSocket")
    
    private var webSocket: WebSocket? = null
    private val handlers = ConcurrentHashMap<String, (String) -> Unit>()
    private val connectionParams = AtomicReference<Pair<String, String>?>()
    
    private val _state = MutableStateFlow<State>(State.Disconnected)
    val state: StateFlow<State> = _state.asStateFlow()
    
    private var retryCount = 0
    private val maxRetryDelay = 30_000L
    
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for WebSocket
        .build()
    
    fun connect(url: String, apiKey: String) {
        connectionParams.set(url to apiKey)
        retryCount = 0
        performConnect()
    }
    
    private fun performConnect() {
        val params = connectionParams.get() ?: return
        val (url, apiKey) = params
        
        disconnect()
        
        _state.value = if (retryCount > 0) {
            State.Reconnecting(retryCount)
        } else {
            State.Connecting
        }
        
        val wsUrl = url.replace("http://", "ws://")
            .replace("https://", "wss://")
        
        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Sec-WebSocket-Protocol", "graphql-ws")
            .addHeader("ApiKey", apiKey)
            .build()
        
        logger.info("📡 Connecting to $wsUrl")
        
        webSocket = client.newWebSocket(request, WebSocketListener())
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        handlers.clear()
        _state.value = State.Disconnected
    }
    
    fun subscribe(id: String, query: String, variables: Map<String, Any> = emptyMap(), handler: (String) -> Unit) {
        handlers[id] = handler
        
        if (_state.value == State.Connected) {
            sendSubscription(id, query, variables)
        }
    }
    
    private fun sendSubscription(id: String, query: String, variables: Map<String, Any>) {
        val message = buildJsonObject {
            put("id", id)
            put("type", "start")
            putJsonObject("payload") {
                put("query", query)
                putJsonObject("variables") {
                    variables.forEach { (key, value) ->
                        put(key, JsonPrimitive(value.toString()))
                    }
                }
            }
        }
        
        webSocket?.send(message.toString())
    }
    
    private inner class WebSocketListener : okhttp3.WebSocketListener() {
        
        override fun onOpen(webSocket: WebSocket, response: Response) {
            logger.info("✅ WebSocket opened")
            
            // Send connection_init
            val initMessage = buildJsonObject {
                put("type", "connection_init")
                putJsonObject("payload") {}
            }
            webSocket.send(initMessage.toString())
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            handleProtocolMessage(text)
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            logger.error("❌ WebSocket failure: ${t.message}")
            _state.value = State.Error(t.message ?: "Unknown error")
            scheduleReconnect()
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            logger.info("WebSocket closed: $reason")
            if (connectionParams.get() != null) {
                scheduleReconnect()
            }
        }
    }
    
    private fun handleProtocolMessage(text: String) {
        val json = Json.parseToJsonElement(text).jsonObject
        val type = json["type"]?.jsonPrimitive?.content
        
        when (type) {
            "connection_ack" -> {
                logger.info("✅ Protocol handshake complete")
                retryCount = 0
                _state.value = State.Connected
                startPing()
                resubscribeAll()
            }
            "data" -> {
                val id = json["id"]?.jsonPrimitive?.content
                val payload = json["payload"]?.toString()
                if (id != null && payload != null) {
                    handlers[id]?.invoke(payload)
                }
            }
            "error" -> {
                val id = json["id"]?.jsonPrimitive?.content
                val message = json["payload"]?.toString()
                logger.error("GraphQL subscription error: $message")
            }
            "ka" -> {
                // Keep-alive, no action needed
            }
        }
    }
    
    private fun startPing() {
        // Send periodic pings to keep connection alive
        // Implementation using coroutine timer
    }
    
    private fun resubscribeAll() {
        // Resubscribe all active subscriptions after reconnect
        logger.info("📥 Resubscribing all active streams")
        // Implementation
    }
    
    private fun scheduleReconnect() {
        retryCount++
        val delay = (1 shl retryCount) * 1000L coerceAtMost maxRetryDelay
        logger.info("🔄 Reconnecting in ${delay}ms (attempt #$retryCount)")
        
        // Schedule reconnect using coroutine
        CoroutineScope(Dispatchers.IO).launch {
            delay(delay)
            if (connectionParams.get() != null) {
                performConnect()
            }
        }
    }
    
    sealed class State {
        object Disconnected : State()
        object Connecting : State()
        object Connected : State()
        data class Reconnecting(val attempt: Int) : State()
        data class Error(val message: String) : State()
    }
}
```

### 4.4 Repository Pattern

**iOS: `SceneRepository.swift`**

**Android: `SceneRepository.kt`**

```kotlin
interface SceneRepository {
    suspend fun getCachedScenes(): List<Scene>
    suspend fun fetchScenes(forceRefresh: Boolean = false): List<Scene>
    suspend fun getSceneById(id: String): Scene?
    suspend fun updateScene(scene: Scene)
    suspend fun deleteScene(id: String)
    fun observeSceneChanges(): Flow<Unit>
}

class SceneRepositoryImpl @Inject constructor(
    private val stashClient: StashClient,
    private val sceneDao: SceneDao,
    private val settings: SettingsStore,
    private val imagePrefetchService: ImagePrefetchService
) : SceneRepository {
    
    private val logger = Logger.getLogger("SceneRepository")
    private val defaultCacheExpiration = 43200_000L // 12 hours in ms
    
    override suspend fun getCachedScenes(): List<Scene> = withContext(Dispatchers.IO) {
        sceneDao.getAllScenes().map { it.toDomain() }
    }
    
    override suspend fun fetchScenes(forceRefresh: Boolean): List<Scene> = withContext(Dispatchers.IO) {
        // Check cache freshness
        val lastSync = getLastSyncDate()
        val shouldRefresh = forceRefresh || shouldRefreshCache(lastSync)
        
        if (shouldRefresh) {
            logger.info("🔄 Fetching scenes from server")
            
            val serverUrl = settings.getServerUrlSync() ?: throw IllegalStateException("No server URL")
            val apiKey = settings.getApiKeySync() ?: throw IllegalStateException("No API key")
            
            val result: SceneResult = stashClient.fetch(
                query = StashQueries.FIND_SCENES,
                variables = mapOf(
                    "filter" to mapOf("per_page" to -1),
                    "scene_filter" to emptyMap<String, Any>()
                ),
                url = serverUrl,
                apiKey = apiKey
            )
            
            val scenes = result.findScenes.scenes
            
            // Save to cache
            sceneDao.insertScenes(scenes.map { it.toEntity() })
            updateLastSyncDate()
            
            // Prefetch images
            val imageUrls = scenes.mapNotNull { it.paths?.screenshot }.mapNotNull { URL(it) }
            imagePrefetchService.prefetch(imageUrls)
            
            logger.info("✅ Fetched ${scenes.size} scenes")
            scenes
        } else {
            logger.info("✅ Using cached scenes")
            getCachedScenes()
        }
    }
    
    override suspend fun getSceneById(id: String): Scene? = withContext(Dispatchers.IO) {
        sceneDao.getSceneById(id)?.toDomain()
    }
    
    override suspend fun updateScene(scene: Scene) = withContext(Dispatchers.IO) {
        sceneDao.insertScenes(listOf(scene.toEntity()))
    }
    
    override suspend fun deleteScene(id: String) = withContext(Dispatchers.IO) {
        // Implementation
    }
    
    override fun observeSceneChanges(): Flow<Unit> {
        return sceneDao.observeScenes().map { }
    }
    
    private fun shouldRefreshCache(lastSync: Long?): Boolean {
        if (lastSync == null) return true
        
        val cacheAge = System.currentTimeMillis() - lastSync
        return cacheAge > defaultCacheExpiration
    }
    
    private suspend fun getLastSyncDate(): Long? {
        // Retrieve from metadata table
        return null // Implementation
    }
    
    private suspend fun updateLastSyncDate() {
        // Update metadata table
    }
}

// Extension functions for mapping
fun SceneEntity.toDomain(): Scene {
    val json = Json { ignoreUnknownKeys = true }
    return Scene(
        id = id,
        title = title,
        details = details,
        date = date,
        created_at = created_at,
        updated_at = updated_at,
        rating100 = rating100,
        o_counter = o_counter,
        paths = pathsJSON?.let { json.decodeFromString(it) },
        files = filesJSON?.let { json.decodeFromString(it) },
        performers = performersJSON?.let { json.decodeFromString(it) },
        tags = tagsJSON?.let { json.decodeFromString(it) },
        studio = studioJSON?.let { json.decodeFromString(it) },
        stash_ids = stash_idsJSON?.let { json.decodeFromString(it) },
        scene_markers = sceneMarkersJSON?.let { json.decodeFromString(it) },
        o_history = oHistoryJSON?.let { json.decodeFromString(it) },
        play_history = playHistoryJSON?.let { json.decodeFromString(it) }
    )
}

fun Scene.toEntity(): SceneEntity {
    val json = Json { ignoreUnknownKeys = true }
    return SceneEntity(
        id = id,
        title = title,
        details = details,
        date = date,
        created_at = created_at,
        updated_at = updated_at,
        rating100 = rating100,
        o_counter = o_counter,
        pathsJSON = paths?.let { json.encodeToString(it) },
        filesJSON = files?.let { json.encodeToString(it) },
        performersJSON = performers?.let { json.encodeToString(it) },
        tagsJSON = tags?.let { json.encodeToString(it) },
        studioJSON = studio?.let { json.encodeToString(it) },
        stash_idsJSON = stash_ids?.let { json.encodeToString(it) },
        sceneMarkersJSON = scene_markers?.let { json.encodeToString(it) },
        oHistoryJSON = o_history?.let { json.encodeToString(it) },
        playHistoryJSON = play_history?.let { json.encodeToString(it) }
    )
}
```

---

## 5. Media Handling

### 5.1 Image Loading & Caching

**iOS: Nuke + ImageCacheService**

**Android: Coil**

```kotlin
// Coil configuration in Application class
val imageLoader = ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25) // 25% of app memory
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizeBytes(150 * 1024 * 1024) // 150 MB
            .build()
    }
    .respectCacheHeaders(false)
    .build()

// Usage in Composable
@Composable
fun SceneImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .memoryCacheKey(url)
            .diskCacheKey(url)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = painterResource(R.drawable.placeholder),
        error = painterResource(R.drawable.error),
        contentScale = ContentScale.Crop
    )
}

// Image prefetching service
@Singleton
class ImagePrefetchService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imageLoader = Coil.imageLoader(context)
    
    fun prefetch(urls: List<String>) {
        urls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            imageLoader.enqueue(request)
        }
    }
    
    fun clearCache() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}
```

### 5.2 Video Playback

**iOS: AVPlayer + PlayerViewController**

**Android: ExoPlayer + AndroidView**

```kotlin
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onPlaybackStateChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    
    // Create ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = false
            }
    }
    
    // Observe playback state
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onPlaybackStateChanged(isPlaying)
            }
        }
        exoPlayer.addListener(listener)
        
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    // AndroidView to embed ExoPlayer
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                controllerShowTimeoutMs = 3000
                controllerHideOnTouch = true
            }
        },
        modifier = modifier
    )
}

// Picture-in-Picture support
class VideoPlayerActivity : ComponentActivity() {
    
    private lateinit var player: ExoPlayer
    private val pictureInPictureParams: PictureInPictureParams
        get() = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
    
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player.isPlaying) {
            enterPictureInPictureMode(pictureInPictureParams)
        }
    }
    
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        
        if (isInPictureInPictureMode) {
            // Hide controls in PiP mode
        } else {
            // Show controls when exiting PiP
        }
    }
}

// Floating video player (overlay)
@Composable
fun FloatingVideoPlayer(
    state: FloatingPlayerState,
    onClose: () -> Unit,
    onExpand: () -> Unit
) {
    if (!state.isVisible) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { /* Detect gestures */ }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(200.dp)
                .height(120.dp)
                .offset { IntOffset(state.offsetX.toInt(), state.offsetY.toInt()) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        // Update position
                    },
                    orientation = Orientation.Horizontal
                ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box {
                VideoPlayer(
                    videoUrl = state.videoUrl,
                    modifier = Modifier.fillMaxSize()
                )
                
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    IconButton(onClick = onExpand) {
                        Icon(Icons.Default.Fullscreen, "Expand", tint = Color.White)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}

data class FloatingPlayerState(
    val isVisible: Boolean = false,
    val videoUrl: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)
```

---

## 6. Additional Features

### 6.1 Pull-to-Refresh

```kotlin
@Composable
fun SceneListWithRefresh(
    viewModel: SceneListViewModel = hiltViewModel()
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )
    
    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyColumn {
            // Content
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

### 6.2 Haptic Feedback

```kotlin
@Singleton
class HapticManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    fun lightImpact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        }
    }
    
    fun mediumImpact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }
    
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
        }
    }
}
```

### 6.3 Toast/Snackbar Notifications

```kotlin
@Composable
fun MainScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // Content
        
        // Show snackbar from ViewModel events
        LaunchedEffect(Unit) {
            viewModel.toastEvents.collect { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
}
```

---

## 7. Testing Strategy

### 7.1 Unit Tests

```kotlin
@ExperimentalCoroutinesTest
class SceneListViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: SceneListViewModel
    private lateinit var mockRepository: SceneRepository
    private lateinit var mockSettings: SettingsStore
    
    @Before
    fun setup() {
        mockRepository = mockk()
        mockSettings = mockk()
        viewModel = SceneListViewModel(mockRepository, mockSettings)
    }
    
    @Test
    fun `loadFromCache should update state to Content when scenes available`() = runTest {
        // Given
        val scenes = listOf(
            Scene(id = "1", title = "Test Scene"),
            Scene(id = "2", title = "Another Scene")
        )
        coEvery { mockRepository.getCachedScenes() } returns scenes
        
        // When
        viewModel.loadFromCache()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.state.value
        assertTrue(state is ListViewState.Content)
        assertEquals(2, (state as ListViewState.Content).items.size)
    }
    
    @Test
    fun `search should filter scenes by title`() = runTest {
        // Given
        val scenes = listOf(
            Scene(id = "1", title = "Action Movie"),
            Scene(id = "2", title = "Drama Film")
        )
        coEvery { mockRepository.getCachedScenes() } returns scenes
        viewModel.loadFromCache()
        advanceUntilIdle()
        
        // When
        viewModel.updateSearchText("Action")
        advanceUntilIdle()
        
        // Then
        val filteredScenes = viewModel.scenes.value
        assertEquals(1, filteredScenes.size)
        assertEquals("Action Movie", filteredScenes[0].title)
    }
}
```

### 7.2 UI Tests (Compose)

```kotlin
@RunWith(AndroidJUnit4::class)
class SceneListScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun scenesDisplayed_whenDataLoaded() {
        val scenes = listOf(
            Scene(id = "1", title = "Test Scene 1"),
            Scene(id = "2", title = "Test Scene 2")
        )
        
        composeTestRule.setContent {
            SceneListScreen(
                viewModel = FakeSceneListViewModel(scenes)
            )
        }
        
        composeTestRule.onNodeWithText("Test Scene 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Scene 2").assertIsDisplayed()
    }
    
    @Test
    fun searchBar_filtersScenes() {
        composeTestRule.setContent {
            SceneListScreen()
        }
        
        composeTestRule.onNodeWithTag("search_bar")
            .performTextInput("Action")
        
        composeTestRule.onNodeWithText("Action Movie").assertIsDisplayed()
        composeTestRule.onNodeWithText("Drama Film").assertDoesNotExist()
    }
}
```

---

## 8. Build Configuration

### 8.1 Gradle Files

**Project-level `build.gradle.kts`**
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0" apply false
}
```

**App-level `build.gradle.kts`**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.stash.app"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.stash.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Ktor
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")
    implementation("io.ktor:ktor-client-websockets:2.3.7")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // OkHttp (for WebSocket)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

## 9. Migration Checklist

### Phase 1: Core Infrastructure (2-3 weeks)
- [ ] Set up Android project with Gradle
- [ ] Configure Hilt dependency injection
- [ ] Implement SettingsStore with DataStore
- [ ] Create Room database schema with migrations
- [ ] Implement Ktor network client
- [ ] Set up WebSocket client for subscriptions
- [ ] Create base repository classes

### Phase 2: Data Layer (2-3 weeks)
- [ ] Implement all Repository interfaces
- [ ] Create DAO interfaces for Room
- [ ] Build entity-to-domain mapping
- [ ] Implement image caching with Coil
- [ ] Set up image prefetch service
- [ ] Create sync service for data synchronization

### Phase 3: Domain Layer (1-2 weeks)
- [ ] Port all domain models
- [ ] Implement use cases
- [ ] Create utility classes (formatters, helpers)

### Phase 4: Presentation Layer - Core (3-4 weeks)
- [ ] Create base ViewModels
- [ ] Implement HomeViewModel and HomeScreen
- [ ] Implement SceneListViewModel and SceneListScreen
- [ ] Implement PerformerListViewModel and PerformerListScreen
- [ ] Create common composables (cards, lists, etc.)
- [ ] Implement bottom navigation

### Phase 5: Presentation Layer - Details (2-3 weeks)
- [ ] Implement SceneDetailViewModel and SceneDetailScreen
- [ ] Implement PerformerDetailViewModel and PerformerDetailScreen
- [ ] Create video player with ExoPlayer
- [ ] Implement floating video player
- [ ] Add Picture-in-Picture support

### Phase 6: Settings & Integration (2 weeks)
- [ ] Implement SettingsScreen
- [ ] Create Whisparr integration screens
- [ ] Implement StashDB screens
- [ ] Add task management UI

### Phase 7: Polish & Testing (2-3 weeks)
- [ ] Write unit tests for ViewModels
- [ ] Write integration tests for repositories
- [ ] Create UI tests with Compose Test
- [ ] Implement haptic feedback
- [ ] Add animations and transitions
- [ ] Optimize performance
- [ ] Handle edge cases and errors

### Phase 8: Additional Features (1-2 weeks)
- [ ] Implement search functionality
- [ ] Add filtering and sorting
- [ ] Create tag selection UI
- [ ] Implement pull-to-refresh
- [ ] Add notification support

**Total Estimated Time: 15-22 weeks (4-5.5 months)**

---

## 10. Key Considerations & Best Practices

### 10.1 Android-Specific Optimizations

1. **Lifecycle Awareness**
   - Use `viewModelScope` for coroutines tied to ViewModel lifecycle
   - Observe state with `collectAsStateWithLifecycle()` to respect Android lifecycle
   - Handle configuration changes (rotation) properly with ViewModel state preservation

2. **Background Processing**
   - Use WorkManager for periodic sync tasks
   - Implement foreground services for long-running operations
   - Use JobScheduler for scheduled tasks

3. **Memory Management**
   - Properly manage Bitmap memory
   - Use `onLowMemory()` callbacks
   - Implement memory cache eviction strategies

4. **Battery Optimization**
   - Use Doze mode considerations
   - Implement battery-aware prefetching
   - Optimize network requests (batching, caching)

### 10.2 Material Design 3

```kotlin
// Theme configuration
@Composable
fun StashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6)
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### 10.3 Performance Tips

1. **Lazy Loading**: Use `LazyColumn`/`LazyVerticalGrid` with proper keys
2. **State Hoisting**: Keep state at the appropriate level
3. **Derivation**: Use `derivedStateOf` for computed properties
4. **Recomposition**: Minimize unnecessary recompositions with `remember` and stable types
5. **Background Work**: Use `Dispatchers.Default` for CPU-intensive work

---

## 11. Conclusion

This conversion plan provides a comprehensive roadmap for bringing Stash to Android while maintaining architectural integrity and leveraging modern Android development practices. The key is maintaining feature parity while embracing Android's unique capabilities like Picture-in-Picture, Material Design, and advanced media handling with ExoPlayer.

The estimated timeline of 4-5.5 months assumes a single experienced Android developer. With multiple developers or existing Android expertise, this could be reduced to 3-4 months.

### Success Criteria

- ✅ Full feature parity with iOS version
- ✅ Native Android UX with Material Design 3
- ✅ Performance on par or better than iOS
- ✅ Proper Android lifecycle handling
- ✅ Comprehensive test coverage (>80%)
- ✅ Battery and memory efficient
- ✅ Support for Android 8.0+ (API 26+)
