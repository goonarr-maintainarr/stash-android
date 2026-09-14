package goonarr.stash.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import goonarr.stash.core.database.entity.SceneEntity
import goonarr.stash.core.database.entity.SceneTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scenes: List<SceneEntity>)

    @Query("SELECT * FROM scenes ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes ORDER BY sortOrder ASC")
    fun pagingSource(): PagingSource<Int, SceneEntity>

    @Query("SELECT * FROM scenes WHERE id = :id")
    fun getById(id: String): Flow<SceneEntity?>

    @Query("DELETE FROM scenes")
    suspend fun clearAll()

    @Query("DELETE FROM scenes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT sortOrder FROM scenes WHERE id = :id")
    suspend fun getSortOrder(id: String): Int?

    @Query("SELECT COUNT(*) FROM scenes")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM scenes")
    fun getCountFlow(): Flow<Int>

    @RawQuery(observedEntities = [SceneEntity::class])
    fun getPagedScenes(query: SupportSQLiteQuery): PagingSource<Int, SceneEntity>

    @RawQuery(observedEntities = [SceneEntity::class])
    fun getScenesCount(query: SupportSQLiteQuery): Flow<Int>

    @RawQuery(observedEntities = [SceneEntity::class])
    fun getHomeScenes(query: SupportSQLiteQuery): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes ORDER BY RANDOM() LIMIT :limit")
    fun getRandom(limit: Int): Flow<List<SceneEntity>>

    /**
     * Get scenes by tag ID by searching the JSON tags column.
     * This is simpler and more reliable than maintaining a separate cross-ref table.
     * The LIKE query searches for the tag ID within the JSON structure.
     */
    @Query(
        """
        SELECT * FROM scenes 
        WHERE tags LIKE '%"id":"' || :tagId || '"%'
        ORDER BY date DESC 
        LIMIT :limit
    """
    )
    fun getByTagId(tagId: String, limit: Int): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes WHERE id IN (:ids)")
    fun getByIds(ids: List<String>): Flow<List<SceneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSceneTagCrossRefs(refs: List<SceneTagCrossRef>)

    @Query("DELETE FROM scene_tag_cross_ref WHERE sceneId = :sceneId")
    suspend fun deleteSceneTagCrossRefs(sceneId: String)

    @Query("SELECT COUNT(*) FROM scene_tag_cross_ref")
    suspend fun getSceneTagCrossRefCount(): Int

    // Sync Pruning
    @Query("SELECT id FROM scenes")
    suspend fun getAllIds(): List<String>

    @Query("SELECT stashIds FROM scenes")
    suspend fun getAllStashIds(): List<SceneStashIds>

    @Query("DELETE FROM scenes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}

data class SceneStashIds(
    val stashIds: List<goonarr.stash.core.model.StashID>?
)
