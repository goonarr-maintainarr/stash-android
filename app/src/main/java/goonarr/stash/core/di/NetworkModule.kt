package goonarr.stash.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import goonarr.stash.core.network.GraphQLExecutor
import goonarr.stash.core.network.GraphQLWebSocketClient
import goonarr.stash.core.network.KtorStashClient
import goonarr.stash.core.network.StashClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    /**
     * Standard HTTP client using Android engine for REST/GraphQL queries.
     */
    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }

            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("HttpClient").d(message)
                    }
                }
            }

            engine {
                connectTimeout = 30_000
                socketTimeout = 30_000
            }
        }
    }

    /**
     * WebSocket-capable HTTP client using OkHttp engine.
     * The Android engine doesn't support WebSockets, so we need OkHttp for subscriptions.
     */
    @Provides
    @Singleton
    @Named("WebSocket")
    fun provideWebSocketHttpClient(json: Json): HttpClient {
        return HttpClient(OkHttp) {
            install(WebSockets)

            install(ContentNegotiation) {
                json(json)
            }

            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("WebSocketClient").d(message)
                    }
                }
            }

            engine {
                config {
                    connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideGraphQLExecutor(client: HttpClient, json: Json): GraphQLExecutor {
        return GraphQLExecutor(client, json)
    }

    @Provides
    @Singleton
    fun provideStashClient(executor: GraphQLExecutor): StashClient {
        return KtorStashClient(executor)
    }

    @Provides
    @Singleton
    fun provideGraphQLWebSocketClient(@Named("WebSocket") client: HttpClient, json: Json): GraphQLWebSocketClient {
        return GraphQLWebSocketClient(client, json)
    }

    @Provides
    @Singleton
    fun provideStashDBClient(executor: GraphQLExecutor): goonarr.stash.core.network.stashdb.StashDBClient {
        return goonarr.stash.core.network.stashdb.StashDBClient(executor)
    }
}
