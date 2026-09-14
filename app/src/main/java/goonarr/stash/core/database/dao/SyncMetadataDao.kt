package goonarr.stash.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import goonarr.stash.core.database.entity.SyncMetadataEntity

@Dao
interface SyncMetadataDao {
    @Query("SELECT lastUpdatedAt FROM sync_metadata WHERE type = :type")
    suspend fun getLastUpdatedAt(type: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLastSync(metadata: SyncMetadataEntity)

    @Query("SELECT COUNT(*) FROM sync_metadata")
    suspend fun getSyncedCount(): Int
}
