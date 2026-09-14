package goonarr.stash.repositories.scenes

import androidx.paging.PagingSource
import androidx.paging.PagingState
import goonarr.stash.core.database.dao.SceneDao
import goonarr.stash.core.database.entity.SceneEntity
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * A PagingSource that loads scenes by their IDs, preserving the order of the IDs.
 * Used for the "Random" category to show the same scenes from the home page.
 */
class SceneIdsPagingSource(
    private val sceneDao: SceneDao,
    private val ids: List<String>
) : PagingSource<Int, SceneEntity>() {

    override fun getRefreshKey(state: PagingState<Int, SceneEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SceneEntity> {
        return try {
            val entities = sceneDao.getByIds(ids).first()
            val entityMap = entities.associateBy { it.id }
            val orderedScenes = ids.mapNotNull { id -> entityMap[id] }

            LoadResult.Page(
                data = orderedScenes,
                prevKey = null,
                nextKey = null
            )
        } catch (e: Exception) {
            Timber.e(e, "📦 SceneIdsPagingSource: Error loading scenes")
            LoadResult.Error(e)
        }
    }
}
