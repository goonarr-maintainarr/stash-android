package goonarr.stash.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import goonarr.stash.core.database.entity.PerformerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(performers: List<PerformerEntity>)

    @Query("SELECT * FROM performers ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<PerformerEntity>>

    @Query("SELECT * FROM performers WHERE id = :id")
    fun getById(id: String): Flow<PerformerEntity?>

    @Query("DELETE FROM performers")
    suspend fun clearAll()

    @Query("DELETE FROM performers WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT sortOrder FROM performers WHERE id = :id")
    suspend fun getSortOrder(id: String): Int?

    @Query("SELECT COUNT(*) FROM performers")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM performers")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT * FROM performers ORDER BY sortOrder ASC")
    fun pagingSource(): androidx.paging.PagingSource<Int, PerformerEntity>

    @androidx.room.RawQuery(observedEntities = [PerformerEntity::class])
    fun getPagedPerformers(query: androidx.sqlite.db.SupportSQLiteQuery): androidx.paging.PagingSource<Int, PerformerEntity>

    // Sync Pruning
    @Query("SELECT id FROM performers")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM performers WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
