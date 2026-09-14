package goonarr.stash.core.network.stashdb

/**
 * Centralized GraphQL queries and fragments for StashDB.
 * All queries target the StashDB GraphQL API (https://stashdb.org/graphql).
 */
object StashDBQueries {

    // MARK: - Tag Constants

    /** StashDB tag ID for VR scenes */
    const val VR_TAG_ID = "3da5244d-b0f9-48af-b5e1-d46ecedb4308"

    /** StashDB tag ID for compilation scenes */
    const val COMPILATION_TAG_ID = "6458e5cf-4f65-400b-9067-582141e2a329"

    // MARK: - Common Fragments

    private val performerFields = """
        id
        name
        gender
        scene_count
        is_favorite
        images {
            id
            url
            width
            height
        }
    """.trimIndent()

    private val sceneOverviewFields = """
        id
        title
        date
        duration
        studio {
            id
            name
        }
        performers {
            performer {
                id
                name
                gender
            }
        }
        images {
            id
            url
            width
            height
        }
    """.trimIndent()

    private val sceneDetailFields = """
        id
        title
        details
        date
        release_date
        production_date
        duration
        director
        code
        deleted
        created
        updated
        studio {
            id
            name
            aliases
            deleted
            is_favorite
            created
            updated
        }
        performers {
            performer {
                id
                name
                gender
            }
        }
        tags {
            id
            name
        }
        images {
            id
            url
            width
            height
        }
        urls {
            url
            type
        }
    """.trimIndent()

    private val studioFields = """
        id
        name
        aliases
        deleted
        is_favorite
        created
        updated
    """.trimIndent()

    // MARK: - Performer Queries

    val favoritePerformersOverview = """
        query GetFavoritePerformersOverview(${'$'}page: Int!, ${'$'}perPage: Int!) {
          queryPerformers(input: {
            is_favorite: true
            page: ${'$'}page
            per_page: ${'$'}perPage
            sort: NAME
            direction: ASC
          }) {
            count
            performers {
              $performerFields
            }
          }
        }
    """.trimIndent()

    // MARK: - Studio Queries

    val favoriteStudios = """
        query QueryStudios(${'$'}page: Int!, ${'$'}perPage: Int!) {
            queryStudios(input: { is_favorite: true, page: ${'$'}page, per_page: ${'$'}perPage }) {
                count
                studios {
                    $studioFields
                }
            }
        }
    """.trimIndent()

    // MARK: - Scene Queries

    /**
     * Generates a query for scenes filtered by performers OR studios.
     */
    fun scenesWithFilters(
        performersFilter: String,
        studiosFilter: String,
        dateFilter: String,
        tagsFilter: String,
        useOverviewFields: Boolean = true
    ): String {
        val fields = if (useOverviewFields) sceneOverviewFields else sceneDetailFields
        return """
            query QueryScenes(${'$'}page: Int!, ${'$'}perPage: Int!) {
                queryScenes(
                    input: { 
                        studios: $studiosFilter,
                        performers: $performersFilter,
                        date: $dateFilter,
                        tags: $tagsFilter,
                        page: ${'$'}page,
                        per_page: ${'$'}perPage
                    }
                ) {
                    count
                    scenes {
                        $fields
                    }
                }
            }
        """.trimIndent()
    }

    /**
     * Generates a batch find scenes query for fetching multiple scenes by ID.
     */
    fun batchFindScenes(ids: List<String>): String {
        val queryBody = ids.mapIndexed { index, id ->
            """s$index: findScene(id: "$id") { $sceneDetailFields }"""
        }.joinToString("\n")

        return """
            query BatchFindScenes {
                $queryBody
            }
        """.trimIndent()
    }

    // MARK: - Helper Functions

    /**
     * Builds an array of excluded tag IDs based on filter options.
     */
    fun buildExcludedTags(excludeVR: Boolean, excludeCompilations: Boolean): List<String> {
        return buildList {
            if (excludeVR) add(VR_TAG_ID)
            if (excludeCompilations) add(COMPILATION_TAG_ID)
        }
    }
}
