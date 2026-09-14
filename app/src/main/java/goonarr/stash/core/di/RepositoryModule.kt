package goonarr.stash.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.dao.StudioDao
import goonarr.stash.core.network.StashClient
import goonarr.stash.repositories.SavedFilterRepository
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSceneRepository(
        db: StashDatabase,
        client: StashClient,
        settingsStore: SettingsStore,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SceneRepository {
        return SceneRepository(db, client, settingsStore, ioDispatcher)
    }

    @Provides
    @Singleton
    fun providePerformerRepository(
        db: StashDatabase,
        client: StashClient,
        settingsStore: SettingsStore,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PerformerRepository {
        return PerformerRepository(db, client, settingsStore, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideStudioRepository(
        studioDao: StudioDao,
        client: StashClient,
        settingsStore: SettingsStore,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): StudioRepository {
        return StudioRepository(studioDao, client, settingsStore, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideTagRepository(
        db: StashDatabase,
        client: StashClient,
        settingsStore: SettingsStore,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): TagRepository {
        return TagRepository(db, client, settingsStore, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideSavedFilterRepository(
        db: StashDatabase,
        client: StashClient,
        settingsStore: SettingsStore,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SavedFilterRepository {
        return SavedFilterRepository(db, client, settingsStore, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideStashDBRepository(
        stashDBClient: goonarr.stash.core.network.stashdb.StashDBClient,
        settingsStore: SettingsStore,
        db: StashDatabase
    ): goonarr.stash.repositories.stashdb.StashDBRepository {
        return goonarr.stash.repositories.stashdb.StashDBRepository(stashDBClient, settingsStore, db)
    }
}
