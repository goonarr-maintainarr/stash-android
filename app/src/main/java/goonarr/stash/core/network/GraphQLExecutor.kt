package goonarr.stash.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * generic executor for GraphQL requests.
 * Handles request serialization, HTTP headers, and standard GraphQL response parsing.
 */
class GraphQLExecutor @Inject constructor(
    val client: HttpClient,
    // Injected from NetworkModule
    val json: Json
) {

    /**
     * Executes a GraphQL query and returns the parsed data.
     *
     * @param url The endpoint URL.
     * @param apiKey Optional API key for the `ApiKey` header.
     * @param query The raw GraphQL query string.
     * @param variables Optional map of variables.
     * @throws Exception If the HTTP request fails, or if the GraphQL response contains errors.
     * @return The parsed data of type [T].
     */
    suspend inline fun <reified T> execute(
        url: String,
        apiKey: String?,
        query: String,
        variables: Map<String, JsonElement>? = null
    ): T {
        val requestBody = GraphQLRequest(
            query = query,
            variables = variables
        )

        val response: GraphQLResponse<T> = client.post(url) {
            contentType(ContentType.Application.Json)
            if (!apiKey.isNullOrEmpty()) {
                header("ApiKey", apiKey)
            }
            setBody(requestBody)
        }.body()

        if (!response.errors.isNullOrEmpty()) {
            throw Exception("GraphQL Errors: ${response.errors.joinToString { it.message }}")
        }

        return response.data ?: throw Exception("GraphQL response data is null")
    }
}
