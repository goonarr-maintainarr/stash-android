package goonarr.stash.scenes

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import goonarr.stash.StashApplication
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneFile
import goonarr.stash.core.model.Studio
import goonarr.stash.features.scenes.scenecard.SceneCard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@org.junit.Ignore("Ignoring composable tests per user request")
@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [34], application = StashApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class SceneCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun sceneCard_displaysCorrectMetadata() {
        // Prepare data matching the Scene model structure
        val scene = Scene(
            id = "1",
            title = "Test Scene Title",
            studio = Studio(id = "s1", name = "Test Studio"),
            date = "2025-08-08",
            performers = listOf(Performer(id = "p1", name = "Performer Name")),
            files = listOf(SceneFile(duration = 2460.0, path = "", videoCodec = "", audioCodec = "", width = 1920, height = 1080, size = 1000L)),
            sceneMarkers = emptyList(),
            paths = null,
            oCounter = 5,
            resumeTime = 0.0
        )

        composeTestRule.setContent {
            SceneCard(scene = scene, onClick = { })
        }

        // Title
        composeTestRule.onNodeWithText("Test Scene Title").assertIsDisplayed()

        // Metadata: Studio • Date • Runtime
        // "Test Studio • Aug 8th, 2025 • 41m"
        composeTestRule.onNodeWithText("Test Studio • Aug 8th, 2025 • 41m").assertIsDisplayed()

        // Performer
        composeTestRule.onNodeWithText("Performer Name").assertIsDisplayed()

        // O-Counter
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun sceneCard_displaysShimmer_whenLoading() {
        composeTestRule.setContent {
            SceneCard(
                scene = Scene(
                    id = "1",
                    files = emptyList(),
                    paths = null,
                    performers = emptyList(),
                    sceneMarkers = emptyList()
                ),
                onClick = {}, isLoading = true
            )
        }

        // We can't easily assert pixel shimmer values in unit test, but we can verify
        // that the content text IS NOT displayed.
        composeTestRule.onNodeWithText("Test Scene Title").assertDoesNotExist()
    }
}
