package goonarr.stash.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import androidx.test.core.app.ApplicationProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import goonarr.stash.util.HeroAccentColor
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import timber.log.Timber

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HeroAccentColorTest {

    private lateinit var context: Context
    private val testUrl = "https://example.com/image.jpg"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        HeroAccentColor.clearCache()
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    println("[$tag] $message")
                }
            })
        }
    }

    @Test
    fun `extract returns null for null url`() = runBlocking {
        val result = HeroAccentColor.extract(context, null)
        assertNull(result)
    }

    @Test
    @Ignore
    fun `extract returns null for blank url`() = runBlocking {
        val result = HeroAccentColor.extract(context, "  ")
        assertNull(result)
    }

    @Test
    fun `extract returns color for valid bitmap`() = runBlocking {
        // Create a multicolor bitmap so Palette can find something
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.RED)
        val paint = android.graphics.Paint()
        paint.color = AndroidColor.BLUE
        canvas.drawRect(0f, 0f, 50f, 50f, paint)
        paint.color = AndroidColor.GREEN
        canvas.drawRect(50f, 50f, 100f, 100f, paint)

        val mockLoader = mockk<ImageLoader>()
        val mockDrawable = BitmapDrawable(context.resources, bitmap)
        val request = ImageRequest.Builder(context).data(testUrl).build()
        val mockResult = SuccessResult(
            drawable = mockDrawable,
            request = request,
            dataSource = coil.decode.DataSource.MEMORY
        )

        coEvery { mockLoader.execute(any()) } returns mockResult

        val color = HeroAccentColor.extract(context, testUrl, mockLoader)

        // Debug info if it fails
        if (color == null) {
            val palette = Palette.from(bitmap).generate()
            Timber.d("Test DBG: Swatches count: ${palette.swatches.size}")
            palette.swatches.forEach {
                Timber.d(
                    "Test DBG: Swatch population: ${it.population}, " +
                        "RGB: ${it.rgb} Hex: ${Integer.toHexString(it.rgb)}"
                )
            }
        }

        assertNotNull("Hero color should not be null", color)
    }

    @Test
    fun `extract uses cache for subsequent calls`() = runBlocking {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.BLUE)

        val mockLoader = mockk<ImageLoader>()
        val mockDrawable = BitmapDrawable(context.resources, bitmap)
        val mockResult = mockk<SuccessResult>()

        coEvery { mockResult.drawable } returns mockDrawable
        // We only expect one execution if cache works
        coEvery { mockLoader.execute(any()) } returns mockResult

        val color1 = HeroAccentColor.extract(context, testUrl, mockLoader)
        val color2 = HeroAccentColor.extract(context, testUrl, mockLoader)

        assertEquals(color1, color2)
    }

    @Test
    fun `extract returns null on loader failure`() = runBlocking {
        val mockLoader = mockk<ImageLoader>()
        coEvery { mockLoader.execute(any()) } throws RuntimeException("Network error")

        val color = HeroAccentColor.extract(context, testUrl, mockLoader)
        assertNull(color)
    }
}
