package goonarr.stash.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.dao.PerformerDao
import goonarr.stash.core.database.dao.SavedFilterDao
import goonarr.stash.core.database.dao.SceneDao
import goonarr.stash.core.database.dao.StudioDao
import goonarr.stash.core.database.dao.SyncMetadataDao
import goonarr.stash.core.database.dao.TagDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStashDatabase(@ApplicationContext context: Context): StashDatabase {
        return Room.databaseBuilder(
            context,
            StashDatabase::class.java,
            "stash_db"
        )
            .fallbackToDestructiveMigration(true) // Useful during early dev
            .build()
    }

    @Provides
    @Singleton
    fun provideSceneDao(database: StashDatabase): SceneDao {
        return database.sceneDao()
    }

    @Provides
    @Singleton
    fun providePerformerDao(database: StashDatabase): PerformerDao {
        return database.performerDao()
    }

    @Provides
    @Singleton
    fun provideStudioDao(database: StashDatabase): StudioDao {
        return database.studioDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: StashDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideSyncMetadataDao(database: StashDatabase): SyncMetadataDao {
        return database.syncMetadataDao()
    }

    @Provides
    @Singleton
    fun provideSavedFilterDao(database: StashDatabase): SavedFilterDao {
        return database.savedFilterDao()
    }
}
