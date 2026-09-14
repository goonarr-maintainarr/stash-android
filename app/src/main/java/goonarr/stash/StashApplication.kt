package goonarr.stash

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import goonarr.stash.core.network.DataUriFetcher
import goonarr.stash.core.network.DataUriKeyer
import goonarr.stash.core.network.StashUrlInterceptor
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Main Application class for Stash Android.
 *
 * Configures:
 * - Hilt dependency injection
 * - Coil image loading pipeline with aggressive caching
 * - ExoPlayer media cache
 */
@HiltAndroidApp
class StashApplication : Application(), ImageLoaderFactory {

    @Inject lateinit var stashUrlInterceptor: StashUrlInterceptor

    @Inject
    lateinit var sceneRepository: goonarr.stash.repositories.scenes.SceneRepository

    // Application Scope for long-running operations
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return "StashApp_${super.createStackElementTag(element)}"
                }
            })
        }

        // Trigger Tag Index Migration
        applicationScope.launch {
            try {
                sceneRepository.ensureTagIndices()
            } catch (e: Exception) {
                Timber.e(e, "Failed to migrate tag indices")
            }
        }

        // Additional initialization can be done here
        // ExoPlayer cache will be configured in ExoPlayer module
    }

    /**
     * Configure Coil ImageLoader with aggressive caching strategy
     * similar to iOS Nuke implementation.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    // Use 25% of app memory for image cache
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    // 150MB disk cache (same as iOS)
                    .maxSizeBytes(150 * 1024 * 1024)
                    .build()
            }
            // Cache policies
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // Enable crossfade animations
            .crossfade(true)
            // Respect cache headers from server
            .respectCacheHeaders(false)
            .components {
                add(stashUrlInterceptor)
                add(SvgDecoder.Factory())
                add(DataUriFetcher.Factory())
                add(DataUriKeyer())
            }
            .build()
    }
}
