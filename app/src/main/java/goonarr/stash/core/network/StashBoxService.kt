package goonarr.stash.core.network

import javax.inject.Inject
import kotlinx.serialization.Serializable
import timber.log.Timber

@Serializable
data class StashBoxTag(
    val id: String,
    val name: String,
    val description: String? = null,
    val aliases: List<String> = emptyList(),
    val category: StashBoxTagCategory? = null
)

@Serializable
data class StashBoxTagCategory(
    val id: String,
    val name: String
)

@Serializable
data class SearchTagResponse(
    val searchTag: List<StashBoxTag>
)

class StashBoxService @Inject constructor(
    private val executor: GraphQLExecutor
) {

    suspend fun searchTags(
        term: String,
        endpoint: String,
        apiKey: String
    ): List<StashBoxTag> {
        val query = """
            query SearchTag(${'$'}term: String!, ${'$'}limit: Int) {
              searchTag(term: ${'$'}term, limit: ${'$'}limit) {
                id
                name
                description
                aliases
                category {
                  id
                  name
                }
              }
            }
        """.trimIndent()

        // Variables must be passed as JsonElement map if implementing strictly with GraphQLExecutor
        // But GraphQLExecutor expects Map<String, JsonElement>?
        // Let's see if I can construct that easily or if I should just use string interpolation for now since it's simpler
        // and GraphQLExecutor supports it via arguments.
        // Wait, GraphQLExecutor takes `variables: Map<String, JsonElement>?`.
        // Constructing JsonElements manually is a bit verbose in Kotlin serialization.

        // I'll try to just interpolate the query for simplicity as the docs example did,
        // OR better yet, let's just use the `variables` param properly if I can.
        // Given I might not have easy JsonElement builders handy without imports, I'll stick to interpolation/hardcoding for now
        // OR I can use `buildJsonObject`.

        // Actually, looking at `GraphQLExecutor.execute`, it takes `variables`.
        // But `StashQueries` usually interpolates values directly into the string.
        // Let's follow `StashQueries` pattern and interpolate for now.

        // However, `term` needs to be escaped if I interpolate.
        // The safest bet is using variables if `GraphQLExecutor` handles them.
        // But `StashQueries` functions return a String query.

        // Let's double check `GraphQLExecutor`. It takes a `query` string and `variables` map.
        // If I use interpolation, I don't need variables.
        // Let's use interpolation for consistency with the rest of the app.

        val safeTerm = term.replace("\"", "\\\"")
        val limit = 20

        val interpolatedQuery = """
            query SearchTag {
              searchTag(term: "$safeTerm", limit: $limit) {
                id
                name
                description
                aliases
                category {
                  id
                  name
                }
              }
            }
        """.trimIndent()

        return try {
            val response = executor.execute<SearchTagResponse>(
                url = endpoint,
                apiKey = apiKey,
                query = interpolatedQuery
            )
            response.searchTag
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to search StashBox tags")
            emptyList()
        }
    }
}
