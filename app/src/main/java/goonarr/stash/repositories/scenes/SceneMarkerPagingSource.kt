package goonarr.stash.repositories.scenes

import androidx.paging.PagingSource
import androidx.paging.PagingState
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.network.StashClient
import kotlinx.coroutines.flow.first

class SceneMarkerPagingSource(
    private val client: StashClient,
    private val settingsStore: SettingsStore,
    private val tagId: String
) : PagingSource<Int, SceneMarker>() {

    override fun getRefreshKey(state: PagingState<Int, SceneMarker>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SceneMarker> {
        return try {
            val page = params.key ?: 1
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()

            val (markers, totalCount) = client.findSceneMarkers(
                url = serverUrl,
                apiKey = apiKey,
                page = page,
                perPage = params.loadSize,
                tagId = tagId
            )

            LoadResult.Page(
                data = markers,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (markers.isEmpty() || (page * params.loadSize) >= totalCount) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
