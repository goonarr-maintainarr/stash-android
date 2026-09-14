package goonarr.stash.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DataUriKeyerTest {

    private lateinit var keyer: DataUriKeyer

    @Before
    fun setup() {
        keyer = DataUriKeyer()
    }

    @Test
    fun `key returns hash-based key for data URIs`() {
        val dataUri = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD"

        val key = keyer.key(dataUri, mockk())

        assertNotNull(key)
        assertEquals("data_uri_${dataUri.hashCode()}", key)
    }

    @Test
    fun `key returns null for non-data URIs`() {
        val httpUrl = "https://example.com/image.jpg"

        val key = keyer.key(httpUrl, mockk())

        assertNull(key)
    }

    @Test
    fun `key returns null for empty string`() {
        val empty = ""

        val key = keyer.key(empty, mockk())

        assertNull(key)
    }

    @Test
    fun `key returns unique keys for different data URIs`() {
        val dataUri1 = "data:image/jpeg;base64,ABC123"
        val dataUri2 = "data:image/jpeg;base64,XYZ789"

        val key1 = keyer.key(dataUri1, mockk())
        val key2 = keyer.key(dataUri2, mockk())

        assertNotNull(key1)
        assertNotNull(key2)
        assert(key1 != key2) { "Keys should be different for different data" }
    }

    @Test
    fun `key returns same key for identical data URIs`() {
        val dataUri = "data:image/jpeg;base64,SAME_DATA"

        val key1 = keyer.key(dataUri, mockk())
        val key2 = keyer.key(dataUri, mockk())

        assertEquals(key1, key2)
    }

    // Mock function for Options (simplified for test purposes)
    private fun mockk(): coil.request.Options {
        return io.mockk.mockk(relaxed = true)
    }
}
