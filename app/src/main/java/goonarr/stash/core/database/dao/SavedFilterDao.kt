package goonarr.stash.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import goonarr.stash.core.database.entity.SavedFilterEntity
import goonarr.stash.core.model.FilterMode
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedFilterDao {

    @Query("SELECT * FROM saved_filters ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<SavedFilterEntity>>

    @Query("SELECT * FROM saved_filters WHERE mode = :mode ORDER BY sortOrder ASC")
    fun getByMode(mode: FilterMode): Flow<List<SavedFilterEntity>>

    @Query("SELECT * FROM saved_filters WHERE id = :id")
    fun getById(id: String): Flow<SavedFilterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(filters: List<SavedFilterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(filter: SavedFilterEntity)

    @Query("DELETE FROM saved_filters WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM saved_filters")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM saved_filters")
    suspend fun getCount(): Int
}
