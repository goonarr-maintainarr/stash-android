package goonarr.stash.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.sqlite.db.SupportSQLiteQuery
import goonarr.stash.core.database.entity.StudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(studios: List<StudioEntity>)

    @Query("SELECT * FROM studios ORDER BY name ASC")
    fun getAll(): Flow<List<StudioEntity>>

    @Query("SELECT * FROM studios WHERE id = :id")
    fun getById(id: String): Flow<StudioEntity?>

    @Query("DELETE FROM studios")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM studios")
    suspend fun getCount(): Int

    @androidx.room.RawQuery(observedEntities = [StudioEntity::class])
    fun getPagedStudios(query: SupportSQLiteQuery): PagingSource<Int, StudioEntity>

    // Sync Pruning
    @Query("SELECT id FROM studios")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM studios WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
