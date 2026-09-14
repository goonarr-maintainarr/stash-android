package goonarr.stash.core.network

/**
 * Singleton object containing raw GraphQL query strings for the Stash API.
 *
 * This object mirrors the iOS `StashQueries.swift` implementation and provides methods
 * to generate query strings with dynamic parameters (pagination, sorting, filtering).
 */
object StashQueries {

    // MARK: - Common Fragments

    private const val sceneFields = """
        id
        title
        date
        created_at
        updated_at
        rating100
        o_counter
        o_history
        play_history
        play_duration
        last_played_at
        resume_time
        studio {
            id
            name
            image_path
        }
        stash_ids {
            stash_id
            endpoint
        }
        director
        code
        url
        urls
        details
    """

    private const val scenePathsFields = """
        paths {
            screenshot
            preview
            stream
            sprite
            vtt
        }
    """

    private const val sceneFilesFields = """
        files {
            path
            size
            duration
            video_codec
            audio_codec
            width
            height
        }
    """

    private const val performerFields = """
        id
        name
        created_at
        updated_at
        image_path
        birthdate
        death_date
        country
        scene_count
        o_counter
    """

    private const val performerDetailFields = """
        id
        name
        disambiguation
        urls
        gender
        birthdate
        ethnicity
        country
        eye_color
        height_cm
        measurements
        fake_tits
        penis_length
        circumcised
        career_length
        tattoos
        piercings
        alias_list
        favorite
        tags {
            id
            name
        }
        image_path
        scene_count
        image_count
        gallery_count
        group_count
        o_counter
        rating100
        details
        death_date
        hair_color
        weight
        created_at
        updated_at
        stash_ids {
            stash_id
            endpoint
        }
    """

    private const val tagFields = """
        id
        name
        aliases
        image_path
        scene_count
        scene_marker_count
        image_count
        gallery_count
        performer_count
        parent_count
        child_count
        favorite
        ignore_auto_tag
        description
        created_at
        updated_at
        stash_ids {
            stash_id
            endpoint
        }
        parents {
            id
            name
            image_path
        }
        children {
            id
            name
            image_path
        }
    """

    private const val sceneMarkerFields = """
        id
        title
        seconds
        end_seconds
        stream
        preview
    """

    private const val studioFields = """
        id
        name
        image_path
        scene_count
        image_count
        gallery_count
        performer_count
        group_count
        parent_studio {
            id
            name
            image_path
        }
        child_studios {
            id
            name
            image_path
        }
        details
        rating100
        favorite
        created_at
        updated_at
        o_counter
        ignore_auto_tag
        aliases
        urls
        stash_ids {
            stash_id
            endpoint
        }
    """

    // MARK: - Scraper Fragments

    private const val scrapedSceneFields = """
        title
        details
        date
        image
        director
        code
        url
        urls
        studio {
            name
        }
        performers {
            name
            gender
            images
        }
        tags {
            name
        }
        remote_site_id
    """

    private const val scrapedPerformerFields = """
        stored_id
        name
        disambiguation
        gender
        urls
        birthdate
        ethnicity
        country
        eye_color
        hair_color
        height
        weight
        measurements
        fake_tits
        penis_length
        circumcised
        career_length
        tattoos
        piercings
        aliases
        images
        details
        death_date
        remote_site_id
        tags {
            name
        }
    """

    // MARK: - Queries

    /**
     * Simple query to test the connection and authentication.
     * Fetches a single scene ID.
     */
    const val testConnection = """
        query TestConnection {
            findScenes(filter: { per_page: 1 }) {
                scenes {
                    id
                }
                count
            }
        }
    """

    /**
     * Fetches global statistics for the library (scene count, performer count, etc.).
     */
    const val stats = """
        query Stats {
            stats {
                scene_count
                scenes_size
                scenes_duration
                image_count
                images_size
                gallery_count
                performer_count
                studio_count
                tag_count
                group_count
                movie_count
                total_o_count
                total_play_duration
                total_play_count
                scenes_played
            }
        }
    """

    /**
     * Fetches the current background job queue status.
     */
    const val jobQueue = """
        query JobQueue {
            jobQueue {
                id
                addTime
                description
                status
                progress
                startTime
                endTime
                subTasks
                error
            }
        }
    """

    /**
     * Fetches recent system logs.
     */
    const val logs = """
        query Logs {
            logs {
                time
                level
                message
            }
        }
    """

    /**
     * Generates a query to find scenes with filters.
     *
     * @param searchText Query string for text search.
     * @param page Page number (1-based).
     * @param perPage Number of items per page.
     * @param sort Field to sort by (default: "created_at").
     * @param direction Sort direction ("ASC" or "DESC").
     * @return Raw GraphQL query string.
     */
    fun findScenes(
        searchText: String = "",
        page: Int,
        perPage: Int = 100,
        sort: String = "created_at",
        direction: String = "DESC",
        performerId: String? = null,
        tagId: String? = null,
        studioId: String? = null,
        updatedAt: String? = null
    ): String {
        val performerFilter = if (performerId != null) {
            ", performers: { value: \"$performerId\", modifier: INCLUDES }"
        } else {
            ""
        }

        val studioFilter = if (studioId != null) {
            ", studios: { value: \"$studioId\", modifier: INCLUDES }"
        } else {
            ""
        }

        val tagFilter = if (tagId != null) {
            ", tags: { value: \"$tagId\", modifier: INCLUDES }"
        } else {
            ""
        }

        val updatedAtFilter = if (updatedAt != null) {
            ", updated_at: { value: \"$updatedAt\", modifier: GREATER_THAN }"
        } else {
            ""
        }

        val sceneFilter = if (
            performerFilter.isNotEmpty() ||
            tagFilter.isNotEmpty() ||
            studioFilter.isNotEmpty() ||
            updatedAtFilter.isNotEmpty()
        ) {
            val filters = listOf(performerFilter, tagFilter, studioFilter, updatedAtFilter)
                .joinToString(" ") { it.removePrefix(", ") }
            ", scene_filter: { $filters }"
        } else {
            ""
        }

        return """
        query FindScenes {
            findScenes(filter: { q: "$searchText", per_page: $perPage, page: $page, sort: "$sort", direction: $direction }$sceneFilter) {
                scenes {
                    $sceneFields
                    $scenePathsFields
                    $sceneFilesFields
                    performers {
                        $performerFields
                    }
                    tags {
                        $tagFields
                    }
                    scene_markers {
                        $sceneMarkerFields
                    }
                }
                count
            }
        }
        """
    }

    /**
     * Generates a query to fetch a single scene by ID.
     *
     * @param id The Stash scene ID.
     * @return Raw GraphQL query string.
     */
    fun findScene(id: String): String {
        return """
        query FindScene {
            findScene(id: "$id") {
                $sceneFields
                $scenePathsFields
                $sceneFilesFields
                performers {
                    $performerFields
                }
                tags {
                    $tagFields
                }
                scene_markers {
                    $sceneMarkerFields
                }
            }
        }
        """
    }

    /**
     * Generates a query to find performers with filters.
     *
     * @param searchText Query string for text search.
     * @param page Page number (1-based).
     * @param perPage Number of items per page.
     * @param sort Field to sort by (default: "name").
     * @param direction Sort direction ("ASC" or "DESC").
     * @return Raw GraphQL query string.
     */
    fun findPerformers(
        searchText: String,
        page: Int,
        perPage: Int = 20,
        sort: String = "name",
        direction: String = "ASC",
        studioId: String? = null,
        tagId: String? = null,
        updatedAt: String? = null
    ): String {
        val studioFilter = if (studioId != null) {
            ", studios: { value: \"$studioId\", modifier: INCLUDES }"
        } else {
            ""
        }

        val tagFilter = if (tagId != null) {
            ", tags: { value: \"$tagId\", modifier: INCLUDES }"
        } else {
            ""
        }

        val updatedAtFilter = if (updatedAt != null) {
            ", updated_at: { value: \"$updatedAt\", modifier: GREATER_THAN }"
        } else {
            ""
        }

        val performerFilter = if (studioFilter.isNotEmpty() || tagFilter.isNotEmpty() || updatedAtFilter.isNotEmpty()) {
            ", performer_filter: { ${studioFilter.removePrefix(", ")}$tagFilter$updatedAtFilter }"
        } else {
            ""
        }

        return """
        query FindPerformers {
            findPerformers(filter: { q: "$searchText", per_page: $perPage, page: $page, sort: "$sort", direction: $direction }$performerFilter) {
                performers {
                    $performerDetailFields
                }
                count
            }
        }
        """
    }

    /**
     * Generates a query to fetch a single performer by ID.
     * Includes detailed fields like birthdate, measurements, etc.
     *
     * @param id The Stash performer ID.
     * @return Raw GraphQL query string.
     */
    fun findPerformer(id: String): String {
        return """
        query FindPerformer {
            findPerformer(id: "$id") {
                $performerDetailFields
            }
        }
        """
    }

    /**
     * Generates a query to find studios with filters.
     *
     * @param searchText Query string for text search.
     * @param page Page number (1-based).
     * @param perPage Number of items per page.
     * @param sort Field to sort by (default: "name").
     * @param direction Sort direction ("ASC" or "DESC").
     * @return Raw GraphQL query string.
     */
    fun findStudios(
        searchText: String,
        page: Int,
        perPage: Int = 20,
        sort: String = "name",
        direction: String = "ASC",
        tagId: String? = null,
        updatedAt: String? = null
    ): String {
        val updatedAtFilter = if (updatedAt != null) {
            ", updated_at: { value: \"$updatedAt\", modifier: GREATER_THAN }"
        } else {
            ""
        }

        val tagFilter = if (tagId != null) {
            ", tags: { value: \"$tagId\", modifier: INCLUDES }"
        } else {
            ""
        }

        val studioFilter = if (updatedAtFilter.isNotEmpty() || tagFilter.isNotEmpty()) {
            ", studio_filter: { ${updatedAtFilter.removePrefix(", ")}$tagFilter }"
        } else {
            ""
        }

        return """
        query FindStudios {
            findStudios(filter: { q: "$searchText", per_page: $perPage, page: $page, sort: "$sort", direction: $direction }$studioFilter) {
                studios {
                    $studioFields
                    tags {
                        $tagFields
                    }
                    stash_ids {
                        stash_id
                        endpoint
                    }
                }
                count
            }
        }
        """
    }

    /**
     * Generates a query to fetch a single studio by ID.
     *
     * @param id The Stash studio ID.
     * @return Raw GraphQL query string.
     */
    fun findStudio(id: String): String {
        return """
        query FindStudio {
            findStudio(id: "$id") {
                $studioFields
                tags {
                    $tagFields
                }
                stash_ids {
                    stash_id
                    endpoint
                }
            }
        }
        """
    }

    /**
     * Generates a query to find tags with filters.
     *
     * @param searchText Query string for text search.
     * @param page Page number (1-based).
     * @param perPage Number of items per page (default: 100).
     * @return Raw GraphQL query string.
     */
    fun findTags(
        searchText: String = "",
        page: Int = 1,
        perPage: Int = 100,
        updatedAt: String? = null
    ): String {
        val updatedAtFilter = if (updatedAt != null) {
            ", updated_at: { value: \"$updatedAt\", modifier: GREATER_THAN }"
        } else {
            ""
        }

        val tagFilter = if (updatedAtFilter.isNotEmpty()) {
            ", tag_filter: { ${updatedAtFilter.removePrefix(", ")} }"
        } else {
            ""
        }

        return """
        query FindTags {
            findTags(filter: { q: "$searchText", per_page: $perPage, page: $page }$tagFilter) {
                tags {
                    $tagFields
                }
                count
            }
        }
        """
    }

    /**
     * Fetches tags by their IDs.
     *
     * @param ids List of tag IDs to fetch.
     * @return Raw GraphQL query string.
     */
    fun findTagsByIds(ids: List<String>): String {
        val idsString = ids.joinToString(", ") { "\"$it\"" }
        return """
        query FindTagsByIds {
            findTags(filter: { per_page: -1 }, ids: [$idsString]) {
                tags {
                    $tagFields
                }
                count
            }
        }
        """
    }

    /**
     * Generates a query to find scene markers with filters.
     *
     * @param page Page number (1-based).
     * @param perPage Number of items per page.
     * @param tagId Optional tag ID to filter by.
     * @return Raw GraphQL query string.
     */
    fun findSceneMarkers(
        page: Int = 1,
        perPage: Int = 20,
        tagId: String? = null
    ): String {
        val tagFilter = if (tagId != null) {
            ", tags: { value: \"$tagId\", modifier: INCLUDES }"
        } else {
            ""
        }

        val markerFilter = if (tagFilter.isNotEmpty()) {
            ", scene_marker_filter: { ${tagFilter.removePrefix(", ")} }"
        } else {
            ""
        }

        return """
        query FindSceneMarkers {
            findSceneMarkers(filter: { per_page: $perPage, page: $page, sort: "created_at", direction: DESC }$markerFilter) {
                scene_markers {
                    $sceneMarkerFields
                    scene {
                        $sceneFields
                    }
                }
                count
            }
        }
        """
    }

    // MARK: - Lightweight ID Queries for Sync Pruning

    /**
     * Fetches all scene IDs. Used for sync pruning to detect deleted scenes.
     */
    const val findAllSceneIds = """
        query FindAllSceneIds {
            findScenes(filter: { per_page: -1 }) {
                scenes {
                    id
                }
            }
        }
    """

    /**
     * Fetches all performer IDs. Used for sync pruning to detect deleted performers.
     */
    const val findAllPerformerIds = """
        query FindAllPerformerIds {
            findPerformers(filter: { per_page: -1 }) {
                performers {
                    id
                }
            }
        }
    """

    /**
     * Fetches all tag IDs. Used for sync pruning to detect deleted tags.
     */
    const val findAllTagIds = """
        query FindAllTagIds {
            findTags(filter: { per_page: -1 }) {
                tags {
                    id
                }
            }
        }
    """

    /**
     * Fetches all studio IDs. Used for sync pruning to detect deleted studios.
     */
    const val findAllStudioIds = """
        query FindAllStudioIds {
            findStudios(filter: { per_page: -1 }) {
                studios {
                    id
                }
            }
        }
    """

    // MARK: - Mutations

    /**
     * Updates a scene.
     *
     * @param id The Stash scene ID.
     * @param title Title of the scene.
     * @param details Scene details/description.
     * @param performerIds List of performer IDs.
     * @param tagIds List of tag IDs.
     * @param director Director name.
     * @param code Scene code.
     * @param url Scene URL.
     * @param rating Rating value (0-100).
     * @return Raw GraphQL mutation string.
     */
    fun sceneUpdate(
        id: String,
        title: String? = null,
        details: String? = null,
        performerIds: List<String>? = null,
        tagIds: List<String>? = null,
        director: String? = null,
        code: String? = null,
        url: String? = null,
        date: String? = null,
        rating: Int? = null,
        studioId: String? = null,
        coverImage: String? = null,
        stashIds: List<Pair<String, String>>? = null
    ): String {
        val inputs = mutableListOf("id: \"$id\"")

        title?.let { inputs.add("title: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        details?.let { inputs.add("details: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        performerIds?.let { ids -> inputs.add("performer_ids: [${ids.joinToString(", ") { "\"$it\"" }}]") }
        tagIds?.let { ids -> inputs.add("tag_ids: [${ids.joinToString(", ") { "\"$it\"" }}]") }
        director?.let { inputs.add("director: \"${it.replace("\"", "\\\"")}\"") }
        code?.let { inputs.add("code: \"${it.replace("\"", "\\\"")}\"") }
        url?.let { inputs.add("url: \"${it.replace("\"", "\\\"")}\"") }
        date?.let { inputs.add("date: \"$it\"") }
        rating?.let { inputs.add("rating100: $it") }
        studioId?.let { inputs.add("studio_id: \"$it\"") }
        coverImage?.let { inputs.add("cover_image: \"$it\"") }
        stashIds?.let { ids ->
            val stashIdsString = ids.joinToString(", ") {
                "{ endpoint: \"${it.first.replace("\"", "\\\"")}\", stash_id: \"${it.second.replace("\"", "\\\"")}\" }"
            }
            inputs.add("stash_ids: [$stashIdsString]")
        }

        return """
        mutation SceneUpdate {
            sceneUpdate(input: { ${inputs.joinToString(", ")} }) {
                $sceneFields
                details
                $scenePathsFields
                $sceneFilesFields
                performers {
                    $performerFields
                }
                tags {
                    $tagFields
                }
                scene_markers {
                    $sceneMarkerFields
                }
            }
        }
        """
    }

    /**
     * Deletes O-counter history entries for a scene.
     *
     * @param id The Stash scene ID.
     * @param times List of timestamps to delete.
     * @return Raw GraphQL mutation string.
     */
    fun sceneDeleteO(id: String, times: List<String>): String {
        val timesString = times.joinToString(", ") { "\"$it\"" }
        return """
        mutation SceneDeleteO {
            sceneDeleteO(id: "$id", times: [$timesString]) {
                count
            }
        }
        """
    }

    /**
     * Deletes a scene.
     *
     * @param id The Stash scene ID.
     * @param deleteFile Whether to delete the source file.
     * @param deleteGenerated Whether to delete generated files (sprites, previews).
     * @return Raw GraphQL mutation string.
     */
    fun sceneDestroy(id: String, deleteFile: Boolean, deleteGenerated: Boolean): String {
        return """
        mutation SceneDestroy {
            sceneDestroy(input: { id: "$id", delete_file: $deleteFile, delete_generated: $deleteGenerated })
        }
        """
    }

    /**
     * Deletes play history entries for a scene.
     *
     * @param id The Stash scene ID.
     * @param times List of timestamps to delete.
     * @return Raw GraphQL mutation string.
     */
    fun sceneDeletePlay(id: String, times: List<String>): String {
        val timesString = times.joinToString(", ") { "\"$it\"" }
        return """
        mutation SceneDeletePlay {
            sceneDeletePlay(id: "$id", times: [$timesString]) {
                count
            }
        }
        """
    }

    /**
     * Increments the O-counter for a scene.
     *
     * @param id The Stash scene ID.
     * @return Raw GraphQL mutation string.
     */
    fun sceneIncrementO(id: String): String {
        return """
        mutation SceneIncrementO {
            sceneIncrementO(id: "$id")
        }
        """
    }

    /**
     * Increments the play count for a scene.
     * Called once per viewing session when the first 10-second threshold is reached.
     *
     * @param id The Stash scene ID.
     * @return Raw GraphQL mutation string.
     */
    fun sceneIncrementPlayCount(id: String): String {
        return """
        mutation SceneIncrementPlayCount {
            sceneIncrementPlayCount(id: "$id")
        }
        """
    }

    /**
     * Generates a screenshot for a scene at a specific timestamp.
     *
     * @param sceneId The Stash scene ID.
     * @param at The timestamp in seconds.
     * @return Raw GraphQL mutation string.
     */
    fun sceneGenerateScreenshot(sceneId: String, at: Double): String {
        return """
        mutation SceneGenerateScreenshot {
            sceneGenerateScreenshot(id: "$sceneId", at: $at)
        }
        """
    }

    /**
     * Updates the watch history for a scene.
     *
     * @param id The Stash scene ID.
     * @param resumeTime The time in seconds to resume playback from.
     * @param playDuration The duration in seconds that the scene was watched.
     * @return Raw GraphQL mutation string.
     */
    fun sceneSaveActivity(
        id: String,
        resumeTime: Double? = null,
        playDuration: Double? = null
    ): String {
        val inputs = mutableListOf("id: \"$id\"")
        resumeTime?.let { inputs.add("resume_time: $it") }
        playDuration?.let { inputs.add("playDuration: $it") }

        return """
        mutation SceneSaveActivity {
            sceneSaveActivity(${inputs.joinToString(", ")})
        }
        """
    }

    /**
     * Updates a performer.
     *
     * @param id The Stash performer ID.
     * @param name Name of the performer.
     * @param url Performer URL.
     * @param gender Gender.
     * @param birthdate Birthdate.
     * @param ethnicity Ethnicity.
     * @param country Country.
     * @param eyeColor Eye color.
     * @param heightCm Height in cm.
     * @param measurements Measurements.
     * @param fakeTits Fake tits.
     * @param careerLength Career length.
     * @param tattoos Tattoos.
     * @param piercings Piercings.
     * @param aliases Aliases.
     * @param details Details/Bio.
     * @param deathDate Death date.
     * @param hairColor Hair color.
     * @param weight Weight.
     * @param rating Rating (0-100).
     * @return Raw GraphQL mutation string.
     */
    fun performerUpdate(
        id: String,
        name: String? = null,
        disambiguation: String? = null,
        url: String? = null,
        gender: String? = null,
        birthdate: String? = null,
        ethnicity: String? = null,
        country: String? = null,
        eyeColor: String? = null,
        heightCm: Int? = null,
        measurements: String? = null,
        fakeTits: String? = null,
        careerLength: String? = null,
        tattoos: String? = null,
        piercings: String? = null,
        aliases: String? = null,
        details: String? = null,
        deathDate: String? = null,
        hairColor: String? = null,
        weight: Int? = null,
        rating: Int? = null,
        favorite: Boolean? = null,
        urls: List<String>? = null,
        penisLength: Double? = null,
        circumcised: String? = null,
        image: String? = null,
        tagIds: List<String>? = null,
        stashIds: List<Pair<String, String>>? = null
    ): String {
        val inputs = mutableListOf("id: \"$id\"")

        name?.let { inputs.add("name: \"${it.replace("\"", "\\\"")}\"") }
        disambiguation?.let { inputs.add("disambiguation: \"${it.replace("\"", "\\\"")}\"") }
        url?.let { inputs.add("url: \"${it.replace("\"", "\\\"")}\"") }
        gender?.let { inputs.add("gender: $it") }
        birthdate?.let { inputs.add("birthdate: \"$it\"") }
        ethnicity?.let { inputs.add("ethnicity: \"${it.replace("\"", "\\\"")}\"") }
        country?.let { inputs.add("country: \"${it.replace("\"", "\\\"")}\"") }
        eyeColor?.let { inputs.add("eye_color: \"${it.replace("\"", "\\\"")}\"") }
        heightCm?.let { inputs.add("height_cm: $it") }
        measurements?.let { inputs.add("measurements: \"${it.replace("\"", "\\\"")}\"") }
        fakeTits?.let { inputs.add("fake_tits: \"${it.replace("\"", "\\\"")}\"") }
        careerLength?.let { inputs.add("career_length: \"${it.replace("\"", "\\\"")}\"") }
        tattoos?.let { inputs.add("tattoos: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        piercings?.let { inputs.add("piercings: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        aliases?.let {
            val aliasList = it.split(",").map { alias -> "\"${alias.trim().replace("\"", "\\\"")}\"" }
            inputs.add("alias_list: [${aliasList.joinToString(", ")}]")
        }
        details?.let { inputs.add("details: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        deathDate?.let { inputs.add("death_date: \"$it\"") }
        hairColor?.let { inputs.add("hair_color: \"${it.replace("\"", "\\\"")}\"") }
        weight?.let { inputs.add("weight: $it") }
        rating?.let {
            if (it == -1) {
                inputs.add("rating100: null")
            } else {
                inputs.add("rating100: $it")
            }
        }
        favorite?.let { inputs.add("favorite: $it") }
        urls?.let { list -> inputs.add("urls: [${list.joinToString(", ") { "\"${it.replace("\"", "\\\"")}\"" }}]") }
        penisLength?.let { inputs.add("penis_length: $it") }
        circumcised?.let { inputs.add("circumcised: $it") }
        image?.let { inputs.add("image: \"$it\"") }
        tagIds?.let { ids -> inputs.add("tag_ids: [${ids.joinToString(", ") { "\"$it\"" }}]") }
        stashIds?.let { ids ->
            val stashIdsString = ids.joinToString(", ") {
                "{ endpoint: \"${it.first.replace("\"", "\\\"")}\", stash_id: \"${it.second.replace("\"", "\\\"")}\" }"
            }
            inputs.add("stash_ids: [$stashIdsString]")
        }

        return """
        mutation PerformerUpdate {
            performerUpdate(input: { ${inputs.joinToString(", ")} }) {
                $performerDetailFields
            }
        }
        """
    }

    /**
     * Helper to format GenerationOptions into a GraphQL input string.
     */
    private fun formatGenerationOptions(options: goonarr.stash.core.model.GenerationOptions): String {
        val sceneIdsPart = options.sceneIds?.let { ids ->
            if (ids.isNotEmpty()) {
                "sceneIDs: [${ids.joinToString(", ") { "\"$it\"" }}], "
            } else {
                ""
            }
        } ?: ""

        return "input: { " +
            sceneIdsPart +
            "covers: ${options.covers}, " +
            "sprites: ${options.sprites}, " +
            "previews: ${options.previews}, " +
            "imagePreviews: ${options.imagePreviews}, " +
            "markers: ${options.markers}, " +
            "markerImagePreviews: ${options.markerImagePreviews}, " +
            "markerScreenshots: ${options.markerScreenshots}, " +
            "transcodes: ${options.transcodes}, " +
            "forceTranscodes: ${options.forceTranscodes}, " +
            "phashes: ${options.phashes}, " +
            "interactiveHeatmapsSpeeds: ${options.interactiveHeatmapsSpeeds}, " +
            "imageThumbnails: ${options.imageThumbnails}, " +
            "clipPreviews: ${options.clipPreviews}, " +
            "overwrite: ${options.overwrite} " +
            "}"
    }

    /**
     * Helper to format ScanOptions into a GraphQL input string.
     */
    private fun formatScanOptions(options: goonarr.stash.core.model.ScanOptions): String {
        val paths = options.pathsArray.joinToString(", ") { "\"${it.replace("\"", "\\\"")}\"" }
        return "input: { " +
            "paths: [$paths], " +
            "rescan: ${options.rescan}, " +
            "scanGenerateCovers: ${options.scanGenerateCovers}, " +
            "scanGeneratePreviews: ${options.scanGeneratePreviews}, " +
            "scanGenerateImagePreviews: ${options.scanGenerateImagePreviews}, " +
            "scanGenerateSprites: ${options.scanGenerateSprites}, " +
            "scanGeneratePhashes: ${options.scanGeneratePhashes}, " +
            "scanGenerateThumbnails: ${options.scanGenerateThumbnails}, " +
            "scanGenerateClipPreviews: ${options.scanGenerateClipPreviews} " +
            "}"
    }

    fun metadataScan(options: goonarr.stash.core.model.ScanOptions): String {
        return """
        mutation MetadataScan {
            metadataScan(${formatScanOptions(options)})
        }
        """
    }

    fun metadataGenerate(options: goonarr.stash.core.model.GenerationOptions): String {
        return """
        mutation MetadataGenerate {
            metadataGenerate(${formatGenerationOptions(options)})
        }
        """
    }

    fun stopJob(jobId: String): String {
        return """
        mutation StopJob {
            stopJob(job_id: "$jobId")
        }
        """
    }

    fun stopAllJobs(): String {
        return """
        mutation StopAllJobs {
            stopAllJobs
        }
        """
    }

    /**
     * Fetches configuration including stash boxes.
     */
    const val configuration = """
        query Configuration {
            configuration {
                general {
                    stashBoxes {
                        name
                        endpoint
                        api_key
                    }
                }
            }
        }
    """

    /**
     * Fetches configuration including UI settings like taggerConfig.
     */
    const val configurationUI = """
        query ConfigurationUI {
            configuration {
                ui
            }
        }
    """

    /**
     * Updates a UI setting by key.
     */
    const val configureUISetting = """
        mutation ConfigureUISetting(${'$'}config: Any!) {
            configureUISetting(key: "taggerConfig", value: ${'$'}config)
        }
    """

    /**
     * Scrapes a single scene.
     */
    fun scrapeSingleScene(
        scraperName: String,
        scraperEndpoint: String,
        scraperApiKey: String?,
        sceneTitle: String?,
        sceneCode: String?,
        sceneDetails: String?,
        sceneDirector: String?,
        sceneUrl: String?,
        sceneDate: String?,
        query: String?
    ): String {
        // Construct the source input object
        // NOTE: The Stash API 'ScraperSourceInput' expects 'stash_box_endpoint'.
        // It does NOT accept 'name' or 'api_key' directly in this input object for StashBox queries.
        val sourceInputs = mutableListOf<String>()
        sourceInputs.add("stash_box_endpoint: \"${scraperEndpoint}\"")

        // Construct the scrape input object
        val inputInputs = mutableListOf<String>()
        if (query != null) {
            inputInputs.add("query: \"${query.replace("\"", "\\\"")}\"")
        }

        // Construct scene_input if any fragment fields are present
        val sceneInputFields = mutableListOf<String>()
        if (sceneTitle != null) sceneInputFields.add("title: \"${sceneTitle.replace("\"", "\\\"")}\"")
        if (sceneCode != null) sceneInputFields.add("code: \"${sceneCode.replace("\"", "\\\"")}\"")
        if (sceneDetails != null) sceneInputFields.add("details: \"${sceneDetails.replace("\"", "\\\"")}\"")
        if (sceneDirector != null) sceneInputFields.add("director: \"${sceneDirector.replace("\"", "\\\"")}\"")
        if (sceneUrl != null) sceneInputFields.add("url: \"${sceneUrl.replace("\"", "\\\"")}\"")
        if (sceneDate != null) sceneInputFields.add("date: \"$sceneDate\"")

        if (sceneInputFields.isNotEmpty()) {
            inputInputs.add("scene_input: { ${sceneInputFields.joinToString(", ")} }")
        }

        return """
        query ScrapeSingleScene {
            scrapeSingleScene(
                source: { ${sourceInputs.joinToString(", ")} },
                input: { ${inputInputs.joinToString(", ")} }
            ) {
                $scrapedSceneFields
            }
        }
        """
    }

    /**
     * Scrapes a single performer from a StashBox source.
     */
    fun scrapeSinglePerformer(
        scraperName: String,
        scraperEndpoint: String,
        scraperApiKey: String?,
        query: String
    ): String {
        val sourceInputs = mutableListOf<String>()
        sourceInputs.add("stash_box_endpoint: \"${scraperEndpoint}\"")

        val inputInputs = mutableListOf<String>()
        inputInputs.add("query: \"${query.replace("\"", "\\\"")}\"")

        return """
        query ScrapeSinglePerformer {
            scrapeSinglePerformer(
                source: { ${sourceInputs.joinToString(", ")} },
                input: { ${inputInputs.joinToString(", ")} }
            ) {
                $scrapedPerformerFields
            }
        }
        """
    }

    private const val scrapedStudioFields = """
        stored_id
        name
        urls
        parent {
            name
        }
        image
        remote_site_id
        details
        tags {
            name
        }
    """

    /**
     * Scrapes a single studio from a StashBox source.
     */
    fun scrapeSingleStudio(
        scraperName: String,
        scraperEndpoint: String,
        scraperApiKey: String?,
        query: String
    ): String {
        val sourceInputs = mutableListOf<String>()
        sourceInputs.add("stash_box_endpoint: \"${scraperEndpoint}\"")

        val inputInputs = mutableListOf<String>()
        inputInputs.add("query: \"${query.replace("\"", "\\\"")}\"")

        return """
        query ScrapeSingleStudio {
            scrapeSingleStudio(
                source: { ${sourceInputs.joinToString(", ")} },
                input: { ${inputInputs.joinToString(", ")} }
            ) {
                $scrapedStudioFields
            }
        }
        """
    }

    fun performerDestroy(id: String): String {
        return """
        mutation PerformerDestroy {
            performerDestroy(input: { id: "$id" })
        }
        """
    }

    /**
     * Creates a new performer.
     */
    fun performerCreate(
        name: String,
        url: String? = null,
        gender: String? = null,
        birthdate: String? = null,
        ethnicity: String? = null,
        country: String? = null,
        eyeColor: String? = null,
        heightCm: Int? = null,
        measurements: String? = null,
        fakeTits: String? = null,
        careerLength: String? = null,
        tattoos: String? = null,
        piercings: String? = null,
        aliases: String? = null,
        details: String? = null,
        deathDate: String? = null,
        hairColor: String? = null,
        weight: Int? = null,
        rating: Int? = null,
        image: String? = null
    ): String {
        val inputs = mutableListOf("name: \"${name.replace("\"", "\\\"")}\"")

        url?.let { inputs.add("url: \"${it.replace("\"", "\\\"")}\"") }
        gender?.let { inputs.add("gender: $it") }
        birthdate?.let { inputs.add("birthdate: \"$it\"") }
        ethnicity?.let { inputs.add("ethnicity: \"${it.replace("\"", "\\\"")}\"") }
        country?.let { inputs.add("country: \"${it.replace("\"", "\\\"")}\"") }
        eyeColor?.let { inputs.add("eye_color: \"${it.replace("\"", "\\\"")}\"") }
        heightCm?.let { inputs.add("height_cm: $it") }
        measurements?.let { inputs.add("measurements: \"${it.replace("\"", "\\\"")}\"") }
        fakeTits?.let { inputs.add("fake_tits: \"${it.replace("\"", "\\\"")}\"") }
        careerLength?.let { inputs.add("career_length: \"${it.replace("\"", "\\\"")}\"") }
        tattoos?.let { inputs.add("tattoos: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        piercings?.let { inputs.add("piercings: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        aliases?.let {
            val aliasList = it.split(",").map { alias -> "\"${alias.trim().replace("\"", "\\\"")}\"" }
            inputs.add("alias_list: [${aliasList.joinToString(", ")}]")
        }
        details?.let { inputs.add("details: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        deathDate?.let { inputs.add("death_date: \"$it\"") }
        hairColor?.let { inputs.add("hair_color: \"${it.replace("\"", "\\\"")}\"") }
        weight?.let { inputs.add("weight: $it") }
        rating?.let { inputs.add("rating100: $it") }
        image?.let { inputs.add("image: \"${it.replace("\"", "\\\"")}\"") }

        return """
        mutation PerformerCreate {
            performerCreate(input: { ${inputs.joinToString(", ")} }) {
                $performerDetailFields
            }
        }
        """
    }

    /**
     * Creates a new scene marker.
     *
     * @param title The marker title.
     * @param seconds The start time in seconds.
     * @param endSeconds Optional end time in seconds.
     * @param sceneId The scene ID.
     * @param primaryTagId The primary tag ID.
     * @param tagIds Optional list of additional tag IDs.
     * @return Raw GraphQL mutation string.
     */
    fun createSceneMarker(
        title: String,
        seconds: Double,
        endSeconds: Double?,
        sceneId: String,
        primaryTagId: String,
        tagIds: List<String>?
    ): String {
        require(seconds.isFinite()) { "seconds must be a finite number, got: $seconds" }
        endSeconds?.let { require(it.isFinite()) { "endSeconds must be a finite number, got: $it" } }
        val inputs = mutableListOf<String>()
        inputs.add("title: \"${title.replace("\"", "\\\"")}\"")
        inputs.add("seconds: $seconds")
        endSeconds?.let { inputs.add("end_seconds: $it") }
        inputs.add("scene_id: \"$sceneId\"")
        inputs.add("primary_tag_id: \"$primaryTagId\"")
        tagIds?.takeIf { it.isNotEmpty() }?.let {
            inputs.add("tag_ids: [${it.joinToString(",") { id -> "\"$id\"" }}]")
        }

        return """
        mutation SceneMarkerCreate {
            sceneMarkerCreate(input: { ${inputs.joinToString(", ")} }) {
                $sceneMarkerFields
            }
        }
        """
    }

    /**
     * Deletes a scene marker.
     *
     * @param id The marker ID.
     * @return Raw GraphQL mutation string.
     */
    fun deleteSceneMarker(id: String): String {
        return """
        mutation SceneMarkerDestroy {
            sceneMarkerDestroy(id: "$id")
        }
        """
    }

    /**
     * Updates a studio.
     *
     * @param id The Stash studio ID.
     * @param name Studio name.
     * @param urls List of URLs.
     * @param parentId Parent studio ID.
     * @param rating100 Rating (0-100).
     * @param favorite Favorite status.
     * @param details Description/details.
     * @param aliases List of aliases.
     * @param tagIds List of tag IDs.
     * @param ignoreAutoTag Whether to ignore auto-tagging.
     * @return Raw GraphQL mutation string.
     */
    fun studioUpdate(
        id: String,
        name: String? = null,
        urls: List<String>? = null,
        parentId: String? = null,
        rating100: Int? = null,
        favorite: Boolean? = null,
        details: String? = null,
        aliases: List<String>? = null,
        tagIds: List<String>? = null,
        ignoreAutoTag: Boolean? = null,
        image: String? = null
    ): String {
        val inputs = mutableListOf("id: \"$id\"")

        name?.let { inputs.add("name: \"${it.replace("\"", "\\\"")}\"") }
        urls?.let { list -> inputs.add("urls: [${list.joinToString(", ") { "\"${it.replace("\"", "\\\"")}\"" }}]") }
        parentId?.let { inputs.add("parent_id: \"$it\"") }
        rating100?.let { inputs.add("rating100: $it") }
        favorite?.let { inputs.add("favorite: $it") }
        details?.let { inputs.add("details: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        aliases?.let { list -> inputs.add("aliases: [${list.joinToString(", ") { "\"${it.replace("\"", "\\\"")}\"" }}]") }
        tagIds?.let { ids -> inputs.add("tag_ids: [${ids.joinToString(", ") { "\"$it\"" }}]") }
        ignoreAutoTag?.let { inputs.add("ignore_auto_tag: $it") }
        image?.let { inputs.add("image: \"$it\"") }

        return """
        mutation StudioUpdate {
            studioUpdate(input: { ${inputs.joinToString(", ")} }) {
                $studioFields
                tags {
                    $tagFields
                }
                stash_ids {
                    stash_id
                    endpoint
                }
            }
        }
        """
    }

    /**
     * Deletes a studio.
     *
     * @param id The Stash studio ID.
     * @return Raw GraphQL mutation string.
     */
    fun studioDestroy(id: String): String {
        return """
        mutation StudioDestroy {
            studioDestroy(input: { id: "$id" })
        }
        """
    }

    /**
     * Updates a tag.
     *
     * @param id The Stash tag ID.
     * @param name Tag name.
     * @param aliases List of aliases.
     * @param image Tag image.
     * @param parentIds List of parent tag IDs.
     * @param childIds List of child tag IDs.
     * @param favorite Favorite status.
     * @param ignoreAutoTag Whether to ignore auto-tagging.
     * @param description Tag description.
     * @return Raw GraphQL mutation string.
     */
    fun tagUpdate(
        id: String,
        name: String? = null,
        aliases: List<String>? = null,
        image: String? = null,
        parentIds: List<String>? = null,
        childIds: List<String>? = null,
        favorite: Boolean? = null,
        ignoreAutoTag: Boolean? = null,
        description: String? = null,
        stashIds: List<Pair<String, String>>? = null
    ): String {
        val inputs = mutableListOf("id: \"$id\"")

        name?.let { inputs.add("name: \"${it.replace("\"", "\\\"")}\"") }
        aliases?.let { list -> inputs.add("aliases: [${list.joinToString(", ") { "\"${it.replace("\"", "\\\"")}\"" }}]") }
        image?.let { inputs.add("image: \"$it\"") }
        parentIds?.let { ids -> inputs.add("parent_ids: [${ids.joinToString(", ") { "\"$it\"" }}]") }
        childIds?.let { ids -> inputs.add("child_ids: [${ids.joinToString(", ") { "\"$it\"" }}]") }
        favorite?.let { inputs.add("favorite: $it") }
        ignoreAutoTag?.let { inputs.add("ignore_auto_tag: $it") }
        description?.let { inputs.add("description: \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"") }
        stashIds?.let { list ->
            val formatted = list.joinToString(", ") { pair ->
                "{ endpoint: \"${pair.first}\", stash_id: \"${pair.second}\" }"
            }
            inputs.add("stash_ids: [$formatted]")
        }

        return """
        mutation TagUpdate {
            tagUpdate(input: { ${inputs.joinToString(", ")} }) {
                $tagFields
            }
        }
        """
    }

    /**
     * Deletes a tag.
     *
     * @param id The Stash tag ID.
     * @return Raw GraphQL mutation string.
     */
    fun tagDestroy(id: String): String {
        return """
        mutation TagDestroy {
            tagDestroy(input: { id: "$id" })
        }
        """
    }

    // MARK: - Saved Filters

    /**
     * Fetches all saved filters, optionally filtered by mode.
     *
     * @param mode Optional filter mode (SCENES, PERFORMERS, STUDIOS, TAGS, etc.)
     * @return Raw GraphQL query string.
     */
    fun findSavedFilters(mode: String? = null): String {
        val modeFilter = if (mode != null) "(mode: $mode)" else ""
        return """
        query FindSavedFilters {
            findSavedFilters$modeFilter {
                id
                mode
                name
                find_filter {
                    q
                    page
                    per_page
                    sort
                    direction
                }
                object_filter
                ui_options
            }
        }
        """
    }

    /**
     * Fetches a single saved filter by ID.
     *
     * @param id The saved filter ID.
     * @return Raw GraphQL query string.
     */
    fun findSavedFilter(id: String): String {
        return """
        query FindSavedFilter {
            findSavedFilter(id: "$id") {
                id
                mode
                name
                find_filter {
                    q
                    page
                    per_page
                    sort
                    direction
                }
                object_filter
                ui_options
            }
        }
        """
    }

    /**
     * Fetches the UI configuration including frontPageContent (home screen layout)
     */
    fun getConfiguration(): String {
        return """
        query GetConfiguration {
            configuration {
                ui
            }
        }
        """
    }
}
