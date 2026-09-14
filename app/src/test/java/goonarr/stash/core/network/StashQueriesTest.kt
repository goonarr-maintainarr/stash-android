package goonarr.stash.core.network

import org.junit.Assert.assertTrue
import org.junit.Test

class StashQueriesTest {

    @Test
    fun `findScenes generates correct query with default params`() {
        val query = StashQueries.findScenes(page = 1)

        // Note: Default perPage is 100 in StashQueries.findScenes
        assertTrue(query.contains("findScenes(filter: { q: \"\", per_page: 100, page: 1, sort: \"created_at\", direction: DESC })"))
        assertTrue(query.contains("scenes {"))
        assertTrue(query.contains("id"))
        assertTrue(query.contains("title"))
    }

    @Test
    fun `findScenes handles custom parameters`() {
        val query = StashQueries.findScenes(
            searchText = "test",
            page = 2,
            perPage = 50,
            sort = "date",
            direction = "ASC"
        )

        assertTrue(query.contains("q: \"test\""))
        assertTrue(query.contains("per_page: 50"))
        assertTrue(query.contains("page: 2"))
        assertTrue(query.contains("sort: \"date\""))
        assertTrue(query.contains("direction: ASC"))
    }

    @Test
    fun `findScenes generates query with performer filter`() {
        val query = StashQueries.findScenes(page = 1, performerId = "123")
        assertTrue("Query should contain scene_filter", query.contains("scene_filter"))
        assertTrue("Query should contain performers modifier", query.contains("performers: { value: \"123\", modifier: INCLUDES }"))
    }

    @Test
    fun `findScenes generates query with tag filter`() {
        val query = StashQueries.findScenes(page = 1, tagId = "t1")
        assertTrue(query.contains("scene_filter"))
        assertTrue(query.contains("tags: { value: \"t1\", modifier: INCLUDES }"))
    }

    @Test
    fun `findScenes generates query with studio filter`() {
        val query = StashQueries.findScenes(page = 1, studioId = "s1")
        assertTrue(query.contains("scene_filter"))
        assertTrue(query.contains("studios: { value: \"s1\", modifier: INCLUDES }"))
    }

    @Test
    fun `findScenes generates query with multiple filters`() {
        val query = StashQueries.findScenes(page = 1, tagId = "t1", studioId = "s1")
        assertTrue(query.contains("scene_filter"))
        // Check both are present inside the filter block
        assertTrue(query.contains("tags: { value: \"t1\", modifier: INCLUDES }"))
        assertTrue(query.contains("studios: { value: \"s1\", modifier: INCLUDES }"))
    }

    @Test
    fun `findScenes generates query without performer filter when id null`() {
        val query = StashQueries.findScenes(page = 1, performerId = null)
        assertTrue("Query should NOT contain scene_filter", !query.contains("scene_filter"))
    }

    @Test
    fun `findScene generates correct query`() {
        val id = "123"
        val query = StashQueries.findScene(id)

        assertTrue(query.contains("findScene(id: \"123\")"))
        assertTrue(query.contains("id"))
        assertTrue(query.contains("title"))
    }

    @Test
    fun `findPerformers generates correct query`() {
        val query = StashQueries.findPerformers(searchText = "star", page = 1)

        assertTrue(query.contains("findPerformers(filter: { q: \"star\", per_page: 20, page: 1, sort: \"name\", direction: ASC })"))
        assertTrue(query.contains("performers {"))
        assertTrue(query.contains("scene_count"))
    }

    @Test
    fun `findPerformers uses correct sort value for scenes_count`() {
        val query = StashQueries.findPerformers(searchText = "", page = 1, sort = "scenes_count")
        assertTrue(query.contains("sort: \"scenes_count\""))
    }

    @Test
    fun `findPerformer generates correct query`() {
        val id = "456"
        val query = StashQueries.findPerformer(id)
        assertTrue(query.contains("findPerformer(id: \"456\")"))
    }

    @Test
    fun `stats query is correct`() {
        val query = StashQueries.stats
        assertTrue(query.contains("query Stats {"))
        assertTrue(query.contains("scene_count"))
    }

    @Test
    fun `findStudios generates correct query`() {
        val query = StashQueries.findStudios(searchText = "studio", page = 1)
        assertTrue(query.contains("findStudios(filter: { q: \"studio\", per_page: 20, page: 1, sort: \"name\", direction: ASC })"))
    }

    @Test
    fun `findStudio generates correct query`() {
        val id = "789"
        val query = StashQueries.findStudio(id)
        assertTrue(query.contains("findStudio(id: \"789\")"))
    }

    @Test
    fun `findTags generates correct query`() {
        val query = StashQueries.findTags(searchText = "tag", page = 1)
        assertTrue(query.contains("findTags(filter: { q: \"tag\", per_page: 100, page: 1 })"))
    }

    @Test
    fun `jobQueue query is correct`() {
        val query = StashQueries.jobQueue
        assertTrue(query.contains("query JobQueue {"))
    }

    @Test
    fun `logs query is correct`() {
        val query = StashQueries.logs
        assertTrue(query.contains("query Logs {"))
    }

    @Test
    fun `sceneUpdate generates correct mutation`() {
        val mutation = StashQueries.sceneUpdate(id = "123", rating = 80, details = "Test Details")
        assertTrue(mutation.contains("mutation SceneUpdate {"))
        assertTrue(mutation.contains("id: \"123\""))
        assertTrue(mutation.contains("rating100: 80"))
        assertTrue(mutation.contains("details: \"Test Details\""))
    }
}
