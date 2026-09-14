# Tagger Configuration - Android

This document explains how tagger configuration settings work with scrape by fragment in Android, including how configs filter results and apply data.

## Table of Contents

- [Overview](#overview)
- [Configuration Settings](#configuration-settings)
- [How Config Affects Scraping](#how-config-affects-scraping)
- [GraphQL API](#graphql-api)
- [Data Models](#data-models)
- [Android Implementation](#android-implementation)
- [Complete Flow Example](#complete-flow-example)

---

## Overview

The tagger configuration controls how scenes are matched, what data is applied, and what metadata fields are used. All settings are stored server-side in Stash's UI configuration.

### Storage Location
- **Key**: `taggerConfig`
- **Storage**: Stash server (not local storage)
- **Scope**: Per user account
- **Mutation**: `configureUISetting`

### How They Work
Configs are **client-side only** - they control UI behavior and data filtering after scraping. They act as filters and rules for what gets applied to scenes.

---

## Configuration Settings

### Complete Settings List

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| **mode** | ParseMode | `"auto"` | How to generate search query |
| **blacklist** | List<String> | `[]` | Regex patterns to filter from queries |
| **performerGenders** | List<GenderEnum> | `null` | Only show performers with these genders |
| **setCoverImage** | Boolean | `true` | Apply cover image from scrape results |
| **setTags** | Boolean | `false` | Apply tags from scrape results |
| **tagOperation** | TagOperation | `"merge"` | How to handle tags (merge/overwrite) |
| **selectedEndpoint** | String | `null` | Default scraper source |
| **fingerprintQueue** | Map<String, List<String>> | `{}` | Internal fingerprint matching queue |
| **excludedPerformerFields** | List<String> | See below | Performer fields to exclude |
| **excludedStudioFields** | List<String> | See below | Studio fields to exclude |
| **markSceneAsOrganizedOnSave** | Boolean | `false` | Mark scene organized when saving |
| **createParentStudios** | Boolean | `true` | Auto-create parent studios |

---

## How Config Affects Scraping

### 1. Performer Gender Filtering

**Most Important:** After getting scrape results, the config filters performers by gender.

```kotlin
// Step 1: Get ALL scraped performers from source
val scrapedScene = results[0]

// Step 2: Filter by configured genders
val allowedGenders = config.performerGenders
if (allowedGenders != null) {
    scrapedScene.performers = scrapedScene.performers?.filter { performer ->
        performer.gender?.let { gender ->
            try {
                val genderEnum = GenderEnum.valueOf(gender)
                allowedGenders.contains(genderEnum)
            } catch (e: IllegalArgumentException) {
                false
            }
        } ?: false
    }
}
```

**Example:**
```kotlin
// Config: performerGenders = listOf(GenderEnum.FEMALE)

// Raw scrape results from fragment:
listOf(
    ScrapedPerformer(name = "Jane Doe", gender = "FEMALE"),    // ✅ Kept
    ScrapedPerformer(name = "John Smith", gender = "MALE"),    // ❌ Filtered out
    ScrapedPerformer(name = "Alex Lee", gender = "NON_BINARY") // ❌ Filtered out
)

// Result shown/applied:
listOf(
    ScrapedPerformer(name = "Jane Doe", gender = "FEMALE")
)
```

---

### 2. Config Application Timeline

| Config Setting | When Applied | How It Works |
|----------------|--------------|--------------|
| `performerGenders` | **After scrape** | Filters results before display/apply |
| `excludedPerformerFields` | **During apply** | Omits fields from update mutations |
| `excludedStudioFields` | **During apply** | Omits fields from studio updates |
| `setCoverImage` | **During apply** | Includes/excludes cover in mutation |
| `setTags` | **During apply** | Includes/excludes tags from mutation |
| `tagOperation` | **During apply** | Merge vs overwrite existing tags |
| `createParentStudios` | **During apply** | Creates parent or skips it |
| `markSceneAsOrganizedOnSave` | **During apply** | Sets organized flag or not |

---

### 3. Fragment Scraping Flow

```
1. Build SceneFragment from existing scene data
   ↓
2. Send fragment to scraper (via GraphQL or Stash-box)
   ↓
3. Receive ALL matching results (unfiltered)
   ↓
4. Apply performerGenders filter ← CONFIG APPLIED HERE
   ↓
5. Display filtered results to user
   ↓
6. User selects result to apply
   ↓
7. Apply other config filters (excluded fields, etc.) ← MORE CONFIG APPLIED
   ↓
8. Build update mutations with filtered data
   ↓
9. Send mutations to Stash
```

---

### 4. Other Config Effects

#### Excluded Performer Fields

Default excluded fields:
```kotlin
val defaultExcludedPerformerFields = listOf(
    "image", "urls", "details", "death_date",
    "hair_color", "weight", "penis_length", "circumcised"
)
```

**Effect:** When building performer update mutation, these fields are omitted:

```kotlin
fun buildPerformerUpdate(
    performerId: String,
    scraped: ScrapedPerformer,
    config: TaggerConfig
): PerformerUpdateInput {
    val input = PerformerUpdateInput(id = performerId)
    
    if (!config.excludedPerformerFields.contains("name")) {
        input.name = scraped.name
    }
    if (!config.excludedPerformerFields.contains("birthdate")) {
        input.birthdate = scraped.birthdate
    }
    if (!config.excludedPerformerFields.contains("image")) {
        input.image = scraped.images?.firstOrNull()
    }
    // ... etc for all fields
    
    return input
}
```

---

#### Tag Handling

```kotlin
fun buildTagIds(
    existingTags: List<Tag>,
    scrapedTags: List<ScrapedTag>,
    config: TaggerConfig
): List<String> {
    
    if (!config.setTags) {
        // Don't apply scraped tags, keep existing
        return existingTags.map { it.id }
    }
    
    // Find or create scraped tags
    val scrapedTagIds = scrapedTags.map { tag ->
        findOrCreateTag(tag)
    }
    
    return when (config.tagOperation) {
        TagOperation.MERGE -> {
            // Combine existing + scraped (remove duplicates)
            val existingIds = existingTags.map { it.id }
            (existingIds + scrapedTagIds).toSet().toList()
        }
        TagOperation.OVERWRITE -> {
            // Replace with scraped only
            scrapedTagIds
        }
    }
}
```

**Example:**
```kotlin
// Existing scene tags: ["Brunette", "HD", "POV"]
// Scraped tags: ["POV", "Threesome", "Outdoor"]

// config.setTags = true, config.tagOperation = MERGE
// Result: ["Brunette", "HD", "POV", "Threesome", "Outdoor"]

// config.setTags = true, config.tagOperation = OVERWRITE
// Result: ["POV", "Threesome", "Outdoor"]

// config.setTags = false
// Result: ["Brunette", "HD", "POV"] (unchanged)
```

---

#### Cover Image

```kotlin
fun buildSceneUpdate(
    sceneId: String,
    scraped: ScrapedScene,
    config: TaggerConfig
): SceneUpdateInput {
    val input = SceneUpdateInput(id = sceneId)
    
    input.title = scraped.title
    input.date = scraped.date
    input.details = scraped.details
    
    // Only set cover if config allows
    if (config.setCoverImage && scraped.image != null) {
        input.coverImage = scraped.image
    }
    
    return input
}
```

---

#### Parent Studios

```kotlin
suspend fun applyStudio(
    scraped: ScrapedStudio,
    config: TaggerConfig
): String {
    
    // Handle parent studio first if config allows
    var parentStudioId: String? = null
    if (config.createParentStudios && scraped.parent != null) {
        parentStudioId = applyStudio(scraped.parent, config)
    }
    
    // Create/update main studio
    val studioId = findOrCreateStudio(scraped)
    
    // Link parent if created
    if (parentStudioId != null) {
        updateStudio(
            id = studioId,
            parentId = parentStudioId
        )
    }
    
    return studioId
}
```

---

## GraphQL API

### Query Configuration

```graphql
query Configuration {
  configuration {
    ui {
      taggerConfig
    }
  }
}
```

**Response:**
```json
{
  "data": {
    "configuration": {
      "ui": {
        "taggerConfig": {
          "mode": "auto",
          "blacklist": ["1080p", "x264"],
          "performerGenders": ["FEMALE"],
          "setCoverImage": true,
          "setTags": false,
          "tagOperation": "merge",
          "selectedEndpoint": "stashbox:https://stashdb.org/graphql",
          "fingerprintQueue": {},
          "excludedPerformerFields": ["image", "urls"],
          "markSceneAsOrganizedOnSave": false,
          "excludedStudioFields": ["image"],
          "createParentStudios": true
        }
      }
    }
  }
}
```

---

### Save Configuration

```graphql
mutation SaveTaggerConfig($config: Any!) {
  configureUISetting(key: "taggerConfig", value: $config)
}
```

**Variables:**
```json
{
  "config": {
    "mode": "auto",
    "blacklist": ["1080p", "720p", "x264"],
    "performerGenders": ["FEMALE", "NON_BINARY"],
    "setCoverImage": true,
    "setTags": true,
    "tagOperation": "merge",
    "selectedEndpoint": "stashbox:https://stashdb.org/graphql",
    "fingerprintQueue": {},
    "excludedPerformerFields": ["image", "urls", "details"],
    "markSceneAsOrganizedOnSave": true,
    "excludedStudioFields": ["image"],
    "createParentStudios": true
  }
}
```

---

## Data Models

### Kotlin/Java Models

```kotlin
import com.google.gson.annotations.SerializedName

// MARK: - Parse Mode

enum class ParseMode(val value: String) {
    @SerializedName("auto")
    AUTO("auto"),
    
    @SerializedName("filename")
    FILENAME("filename"),
    
    @SerializedName("dir")
    DIRECTORY("dir"),
    
    @SerializedName("path")
    PATH("path"),
    
    @SerializedName("metadata")
    METADATA("metadata");
    
    val displayName: String
        get() = when (this) {
            AUTO -> "Auto"
            FILENAME -> "Filename"
            DIRECTORY -> "Directory"
            PATH -> "Path"
            METADATA -> "Metadata"
        }
}

// MARK: - Tag Operation

enum class TagOperation(val value: String) {
    @SerializedName("merge")
    MERGE("merge"),
    
    @SerializedName("overwrite")
    OVERWRITE("overwrite");
    
    val displayName: String
        get() = when (this) {
            MERGE -> "Merge"
            OVERWRITE -> "Overwrite"
        }
}

// MARK: - Gender Enum

enum class GenderEnum(val value: String) {
    @SerializedName("MALE")
    MALE("MALE"),
    
    @SerializedName("FEMALE")
    FEMALE("FEMALE"),
    
    @SerializedName("TRANSGENDER_MALE")
    TRANSGENDER_MALE("TRANSGENDER_MALE"),
    
    @SerializedName("TRANSGENDER_FEMALE")
    TRANSGENDER_FEMALE("TRANSGENDER_FEMALE"),
    
    @SerializedName("INTERSEX")
    INTERSEX("INTERSEX"),
    
    @SerializedName("NON_BINARY")
    NON_BINARY("NON_BINARY");
    
    val displayName: String
        get() = when (this) {
            MALE -> "Male"
            FEMALE -> "Female"
            TRANSGENDER_MALE -> "Transgender Male"
            TRANSGENDER_FEMALE -> "Transgender Female"
            INTERSEX -> "Intersex"
            NON_BINARY -> "Non-Binary"
        }
}

// MARK: - Tagger Config

data class TaggerConfig(
    val mode: ParseMode = ParseMode.AUTO,
    val blacklist: List<String> = emptyList(),
    val performerGenders: List<GenderEnum>? = null,
    val setCoverImage: Boolean = true,
    val setTags: Boolean = false,
    val tagOperation: TagOperation = TagOperation.MERGE,
    val selectedEndpoint: String? = null,
    val fingerprintQueue: Map<String, List<String>> = emptyMap(),
    val excludedPerformerFields: List<String> = defaultExcludedPerformerFields,
    val markSceneAsOrganizedOnSave: Boolean = false,
    val excludedStudioFields: List<String> = defaultExcludedStudioFields,
    val createParentStudios: Boolean = true
) {
    companion object {
        // Default excluded fields
        val defaultExcludedPerformerFields = listOf(
            "image", "urls", "details", "death_date",
            "hair_color", "weight", "penis_length", "circumcised"
        )
        
        val defaultExcludedStudioFields = listOf("image")
        
        // All available fields
        val allPerformerFields = listOf(
            "name", "image", "disambiguation", "aliases",
            "gender", "birthdate", "death_date", "country",
            "ethnicity", "hair_color", "eye_color", "height",
            "weight", "penis_length", "circumcised", "measurements",
            "fake_tits", "tattoos", "piercings", "career_length",
            "urls", "details"
        )
        
        val allStudioFields = listOf("name", "image", "url", "parent_studio")
    }
}
```

---

## Android Implementation

### Configuration Repository

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaggerConfigRepository @Inject constructor(
    private val graphQLService: GraphQLService
) {
    
    // MARK: - Load Configuration
    
    suspend fun loadConfig(): TaggerConfig = withContext(Dispatchers.IO) {
        val query = """
            query Configuration {
              configuration {
                ui {
                  taggerConfig
                }
              }
            }
        """.trimIndent()
        
        val response = graphQLService.query<ConfigResponse>(
            query = query,
            variables = null
        )
        
        response.data.configuration.ui.taggerConfig ?: TaggerConfig()
    }
    
    // MARK: - Save Configuration
    
    suspend fun saveConfig(config: TaggerConfig) = withContext(Dispatchers.IO) {
        val mutation = """
            mutation SaveTaggerConfig(${'$'}config: Any!) {
              configureUISetting(key: "taggerConfig", value: ${'$'}config)
            }
        """.trimIndent()
        
        val configMap = config.toMap()
        val variables = mapOf("config" to configMap)
        
        graphQLService.mutate<Unit>(
            mutation = mutation,
            variables = variables
        )
    }
    
    // Data classes for response parsing
    data class ConfigResponse(
        val configuration: ConfigData
    )
    
    data class ConfigData(
        val ui: UIData
    )
    
    data class UIData(
        val taggerConfig: TaggerConfig?
    )
}
```

---

### Applying Configuration During Scrape

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaggerApplyService @Inject constructor(
    private val config: TaggerConfig,
    private val performerRepository: PerformerRepository,
    private val studioRepository: StudioRepository,
    private val tagRepository: TagRepository,
    private val sceneRepository: SceneRepository
) {
    
    // MARK: - Apply Scraped Data
    
    suspend fun applyScrapedData(
        scene: Scene,
        scrapedScene: ScrapedScene
    ) = withContext(Dispatchers.IO) {
        
        // 1. Filter performers by gender
        var filteredPerformers = scrapedScene.performers.orEmpty()
        config.performerGenders?.let { allowedGenders ->
            filteredPerformers = filteredPerformers.filter { performer ->
                performer.gender?.let { gender ->
                    try {
                        val genderEnum = GenderEnum.valueOf(gender)
                        allowedGenders.contains(genderEnum)
                    } catch (e: IllegalArgumentException) {
                        false
                    }
                } ?: false
            }
        }
        
        // 2. Create/update performers
        val performerIds = filteredPerformers.map { performer ->
            applyPerformer(performer)
        }
        
        // 3. Create/update studio
        val studioId = scrapedScene.studio?.let { studio ->
            applyStudio(studio)
        }
        
        // 4. Handle tags
        val tagIds = buildTagIds(
            existingTags = scene.tags,
            scrapedTags = scrapedScene.tags.orEmpty()
        )
        
        // 5. Build scene update
        val input = SceneUpdateInput(id = scene.id).apply {
            title = scrapedScene.title
            code = scrapedScene.code
            details = scrapedScene.details
            director = scrapedScene.director
            urls = scrapedScene.urls
            date = scrapedScene.date
            this.studioId = studioId
            this.performerIds = performerIds
            this.tagIds = tagIds
            
            // Apply cover image if config allows
            if (config.setCoverImage) {
                coverImage = scrapedScene.image
            }
            
            // Mark organized if config allows
            if (config.markSceneAsOrganizedOnSave) {
                organized = true
            }
        }
        
        // 6. Update scene
        sceneRepository.updateScene(input)
    }
    
    // MARK: - Apply Performer
    
    private suspend fun applyPerformer(scraped: ScrapedPerformer): String {
        // Find or create performer
        val performerId = performerRepository.findOrCreate(scraped)
        
        // Build update input based on excluded fields
        val input = PerformerUpdateInput(id = performerId).apply {
            if (!config.excludedPerformerFields.contains("name")) {
                name = scraped.name
            }
            if (!config.excludedPerformerFields.contains("gender")) {
                gender = scraped.gender
            }
            if (!config.excludedPerformerFields.contains("birthdate")) {
                birthdate = scraped.birthdate
            }
            if (!config.excludedPerformerFields.contains("image")) {
                image = scraped.images?.firstOrNull()
            }
            // ... etc for all fields
        }
        
        performerRepository.update(input)
        return performerId
    }
    
    // MARK: - Apply Studio
    
    private suspend fun applyStudio(scraped: ScrapedStudio): String {
        // Handle parent first if config allows
        var parentId: String? = null
        if (config.createParentStudios && scraped.parent != null) {
            parentId = applyStudio(scraped.parent)
        }
        
        // Find or create main studio
        val studioId = studioRepository.findOrCreate(scraped)
        
        // Build update input
        val input = StudioUpdateInput(id = studioId).apply {
            if (!config.excludedStudioFields.contains("name")) {
                name = scraped.name
            }
            if (!config.excludedStudioFields.contains("image")) {
                image = scraped.image
            }
            if (!config.excludedStudioFields.contains("url")) {
                urls = scraped.urls
            }
            if (!config.excludedStudioFields.contains("parent_studio")) {
                this.parentId = parentId
            }
        }
        
        studioRepository.update(input)
        return studioId
    }
    
    // MARK: - Build Tag IDs
    
    private suspend fun buildTagIds(
        existingTags: List<Tag>,
        scrapedTags: List<ScrapedTag>
    ): List<String> {
        
        if (!config.setTags) {
            return existingTags.map { it.id }
        }
        
        val scrapedTagIds = scrapedTags.map { tag ->
            tagRepository.findOrCreate(tag)
        }
        
        return when (config.tagOperation) {
            TagOperation.MERGE -> {
                val existingIds = existingTags.map { it.id }
                (existingIds + scrapedTagIds).toSet().toList()
            }
            TagOperation.OVERWRITE -> {
                scrapedTagIds
            }
        }
    }
}
```

---

## Complete Flow Example

```kotlin
// 1. Load config
val configRepository = TaggerConfigRepository(graphQL)
val config = configRepository.loadConfig()

// 2. Generate query using config (if needed)
val queryService = TaggerQueryService(config)
val query = queryService.prepareQueryString(scene)

// 3. Scrape with fragment
val scraperService = SceneScraperService(graphQL)
val results = scraperService.scrapeSceneByFragment(
    fragment = SceneFragment.from(scene),
    source = ScraperSource(stashBoxIndex = 0)
)

// 4. Apply first result using config
val applyService = TaggerApplyService(
    config = config,
    performerRepository = performerRepo,
    studioRepository = studioRepo,
    tagRepository = tagRepo,
    sceneRepository = sceneRepo
)
applyService.applyScrapedData(
    scene = scene,
    scrapedScene = results[0]
)
```

---

## Key Points

✅ **Configs are client-side only** - They control UI behavior and data filtering, not server scraping

✅ **Performer gender filtering** happens AFTER scraping - all performers are scraped, then filtered

✅ **Result filtering** uses `performerGenders` after receiving scrape results

✅ **Data application** uses excluded fields and boolean flags when building mutations

✅ **No special GraphQL needed** - Standard mutations, just with conditional data based on config

✅ **The scraper returns ALL data** - filtering happens in your Android app

---

## Related Documentation

- [TAGGER_QUERY_PREPOPULATION.md](TAGGER_QUERY_PREPOPULATION.md) - Query generation details
- [SCENE_SCRAPING.md](SCENE_SCRAPING.md) - Scene scraping implementation
- [STUDIO_CRUD.md](STUDIO_CRUD.md) - Studio operations
