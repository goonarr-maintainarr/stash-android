package goonarr.stash.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import goonarr.stash.core.database.dao.PerformerDao
import goonarr.stash.core.database.dao.SavedFilterDao
import goonarr.stash.core.database.dao.SceneDao
import goonarr.stash.core.database.dao.StudioDao
import goonarr.stash.core.database.dao.SyncMetadataDao
import goonarr.stash.core.database.dao.TagDao
import goonarr.stash.core.database.entity.PerformerEntity
import goonarr.stash.core.database.entity.RemoteKey
import goonarr.stash.core.database.entity.SavedFilterEntity
import goonarr.stash.core.database.entity.SceneEntity
import goonarr.stash.core.database.entity.SceneTagCrossRef
import goonarr.stash.core.database.entity.StudioEntity
import goonarr.stash.core.database.entity.SyncMetadataEntity
import goonarr.stash.core.database.entity.TagEntity
import goonarr.stash.util.Converters

@Database(
    entities = [
        SceneEntity::class,
        PerformerEntity::class,
        StudioEntity::class,
        TagEntity::class,
        RemoteKey::class,
        SyncMetadataEntity::class,
        SceneTagCrossRef::class,
        SavedFilterEntity::class
    ],
    version = 21,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StashDatabase : RoomDatabase() {
    abstract fun sceneDao(): SceneDao
    abstract fun performerDao(): PerformerDao
    abstract fun studioDao(): StudioDao
    abstract fun tagDao(): TagDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun savedFilterDao(): SavedFilterDao
}
