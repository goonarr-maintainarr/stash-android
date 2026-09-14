package goonarr.stash.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    // e.g., "scenes", "performers"
    @PrimaryKey val type: String,
    // ISO timestamp
    val lastUpdatedAt: String
)
