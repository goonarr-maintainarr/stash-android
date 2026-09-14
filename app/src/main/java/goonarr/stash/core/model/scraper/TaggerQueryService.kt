package goonarr.stash.core.model.scraper

import goonarr.stash.core.model.Scene
import java.io.File
import timber.log.Timber

object TaggerQueryService {

    fun prepareQueryString(
        scene: Scene,
        mode: ParseMode = ParseMode.AUTO,
        blacklist: List<String> = emptyList()
    ): String {
        Timber.d("Generating query for scene ${scene.id} with mode $mode")
        var query = ""

        when (mode) {
            ParseMode.AUTO -> {
                // Auto: Use metadata if available, else filename
                if (!scene.date.isNullOrEmpty() && scene.studio?.name?.isNotEmpty() == true) {
                    query = buildMetadataQuery(scene)
                    Timber.d("Auto-selected mode: metadata")
                } else {
                    scene.files?.firstOrNull()?.path?.let { path ->
                        query = buildFilenameQuery(path)
                        Timber.d("Auto-selected mode: filename")
                    }
                }

                // Fallback to title if query is still empty
                if (query.isEmpty() && !scene.title.isNullOrEmpty()) {
                    query = buildMetadataQuery(scene)
                    Timber.d("Auto-selected mode: title (fallback)")
                }
            }
            ParseMode.METADATA -> {
                query = buildMetadataQuery(scene)
            }
            ParseMode.FILENAME -> {
                scene.files?.firstOrNull()?.path?.let { path ->
                    query = buildFilenameQuery(path)
                }
            }
            ParseMode.PATH -> {
                scene.files?.firstOrNull()?.path?.let { path ->
                    query = buildPathQuery(path)
                }
            }
            ParseMode.DIRECTORY -> {
                scene.files?.firstOrNull()?.path?.let { path ->
                    query = buildDirectoryQuery(path)
                }
            }
        }

        // Apply blacklist patterns
        var cleanQuery = query
        if (blacklist.isNotEmpty()) {
            Timber.d("Applying ${blacklist.size} blacklist patterns")
            for (pattern in blacklist) {
                try {
                    val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                    cleanQuery = regex.replace(cleanQuery, " ")
                } catch (e: Exception) {
                    Timber.w("Invalid regex pattern in blacklist: $pattern")
                }
            }
        }

        val finalQuery = cleanQuery.replace(Regex(" +"), " ").trim()
        Timber.d("Final generated query: '$finalQuery'")
        return finalQuery
    }

    private fun buildMetadataQuery(scene: Scene): String {
        val components = mutableListOf<String>()

        // Date
        scene.date?.let { components.add(it) }

        // Studio
        scene.studio?.name?.let { components.add(it) }

        // Performers
        scene.performers?.let { performers ->
            val names = performers.mapNotNull { it.name }.joinToString(" ")
            if (names.isNotEmpty()) {
                components.add(names)
            }
        }

        // Title (clean special characters)
        scene.title?.let { title ->
            val cleanTitle = title.replace(Regex("[^a-zA-Z0-9 ]+"), "")
            if (cleanTitle.isNotEmpty()) {
                components.add(cleanTitle)
            }
        }

        return components.filter { it.isNotEmpty() }.joinToString(" ")
    }

    private fun buildFilenameQuery(path: String): String {
        val file = File(path)
        val filename = file.nameWithoutExtension
        return filename.replace(".", " ").replace(Regex(" +"), " ")
    }

    private fun buildPathQuery(path: String): String {
        val file = File(path)
        // This is a simplified version of path query logic.
        // It takes the path components, similar to iOS logic but adapted.
        // iOS logic: components.filter { $0 != "/" }, replace last with filename w/o ext

        // Android/Java File handling:
        val components = path.split(File.separator).filter { it.isNotEmpty() }.toMutableList()

        if (components.isNotEmpty()) {
            val filename = File(path).nameWithoutExtension
            components[components.lastIndex] = filename
        }

        var query = components.joinToString(" ")
        query = query.replace(".", " ").replace(Regex(" +"), " ")
        return query
    }

    private fun buildDirectoryQuery(path: String): String {
        val parent = File(path).parentFile
        return parent?.name ?: ""
    }
}
