package goonarr.stash.core.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import goonarr.stash.features.scenes.utils.VTTParser
import goonarr.stash.util.StashUrlFormatter
import io.ktor.client.HttpClient

/**
 * EntryPoint for accessing Hilt-provided dependencies from Composables.
 * Use with EntryPointAccessors.fromApplication(context, StashEntryPoint::class.java)
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface StashEntryPoint {
    fun httpClient(): HttpClient
    fun vttParser(): VTTParser
    fun urlFormatter(): StashUrlFormatter
}
