package goonarr.stash.core.model.scraper

import kotlinx.serialization.Serializable

@Serializable
enum class ParseMode(val value: String) {
    AUTO("auto"),
    FILENAME("filename"),
    DIRECTORY("dir"),
    PATH("path"),
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

@Serializable
enum class TagOperation(val value: String) {
    ADD("ADD"),
    REPLACE("REPLACE");

    val displayName: String
        get() = when (this) {
            ADD -> "Add"
            REPLACE -> "Replace"
        }
}

@Serializable
data class TaggerConfig(
    val mode: ParseMode = ParseMode.AUTO,
    val blacklist: List<String> = emptyList(),
    val setTags: Boolean = true,
    val tagOperation: TagOperation = TagOperation.ADD,
    val setCoverImage: Boolean = true,
    val markSceneAsOrganizedOnSave: Boolean = false,
    val createParentStudios: Boolean = false,
    val performerGenders: List<String>? = null,
    val excludedPerformerFields: List<String> = emptyList(),
    val excludedStudioFields: List<String> = emptyList()
) {
    companion object {
        val allPerformerFields = listOf(
            "name", "gender", "birthdate", "ethnicity", "country", "eye_color",
            "height", "measurements", "fake_tits", "career_length", "tattoos",
            "piercings", "aliases", "details", "death_date", "hair_color", "weight"
        )

        val allStudioFields = listOf(
            "name", "url", "image", "details"
        )
    }
}
