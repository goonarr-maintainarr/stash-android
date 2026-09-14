package goonarr.stash.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import goonarr.stash.core.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY sortOrder ASC")
    fun pagingSource(): PagingSource<Int, TagEntity>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getById(id: String): Flow<TagEntity?>

    @Query("SELECT * FROM tags WHERE id IN (:ids)")
    fun loadByIds(ids: List<String>): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(tags: List<TagEntity>)

    @Query("DELETE FROM tags")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getCount(): Int

    // Sync Pruning
    @Query("SELECT id FROM tags")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tags WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
