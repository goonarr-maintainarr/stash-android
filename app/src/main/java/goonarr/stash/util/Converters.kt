package goonarr.stash.util

import androidx.room.TypeConverter
import goonarr.stash.core.model.FilterCriterion
import goonarr.stash.core.model.FilterMode
import goonarr.stash.core.model.FindFilter
import goonarr.stash.core.model.JobStatus
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.StashID
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromStudio(value: Studio?): String? {
        return value?.let { Json.Default.encodeToString(it) }
    }

    @TypeConverter
    fun toStudio(value: String?): Studio? {
        return value?.let {
            try {
                Json.Default.decodeFromString(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromStashIDList(value: List<StashID>?): String {
        return Json.Default.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStashIDList(value: String): List<StashID> {
        return try {
            Json.Default.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Json.Default.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Json.Default.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return Json.Default.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return try {
            Json.Default.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromJobStatus(value: JobStatus): String {
        return value.name
    }

    @TypeConverter
    fun toJobStatus(value: String): JobStatus {
        return try {
            JobStatus.valueOf(value)
        } catch (e: Exception) {
            JobStatus.FAILED // Default fallback
        }
    }

    @TypeConverter
    fun fromSceneMarkerList(value: List<SceneMarker>?): String {
        return Json.Default.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toSceneMarkerList(value: String): List<SceneMarker> {
        return try {
            Json.Default.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromPerformerList(value: List<Performer>?): String {
        return Json.Default.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toPerformerList(value: String): List<Performer> {
        return try {
            Json.Default.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromTagList(value: List<Tag>?): String {
        return Json.Default.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toTagList(value: String): List<Tag> {
        return try {
            Json.Default.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromPerformerTagList(value: List<goonarr.stash.core.model.PerformerTag>?): String {
        return Json.Default.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toPerformerTagList(value: String): List<goonarr.stash.core.model.PerformerTag> {
        return try {
            Json.Default.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // SavedFilter converters
    @TypeConverter
    fun fromFilterMode(value: FilterMode): String {
        return value.name
    }

    @TypeConverter
    fun toFilterMode(value: String): FilterMode {
        return try {
            FilterMode.valueOf(value)
        } catch (e: Exception) {
            FilterMode.SCENES // Default fallback
        }
    }

    @TypeConverter
    fun fromFindFilter(value: FindFilter?): String? {
        return value?.let { Json.Default.encodeToString(FindFilter.serializer(), it) }
    }

    @TypeConverter
    fun toFindFilter(value: String?): FindFilter? {
        return value?.let {
            try {
                Json.Default.decodeFromString(FindFilter.serializer(), it)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromFilterCriterionMap(value: Map<String, FilterCriterion>?): String? {
        return value?.let {
            try {
                Json.Default.encodeToString(
                    MapSerializer(String.serializer(), FilterCriterion.serializer()),
                    it
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun toFilterCriterionMap(value: String?): Map<String, FilterCriterion>? {
        return value?.let {
            try {
                Json.Default.decodeFromString(
                    MapSerializer(String.serializer(), FilterCriterion.serializer()),
                    it
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { Json.Default.encodeToString(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let {
            try {
                Json.Default.decodeFromString(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}
