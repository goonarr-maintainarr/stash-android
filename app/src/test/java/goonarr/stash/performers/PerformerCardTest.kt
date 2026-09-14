package goonarr.stash.performers

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import goonarr.stash.StashApplication
import goonarr.stash.core.model.Performer
import goonarr.stash.features.performers.PerformerCard
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
class PerformerCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun performerCard_displaysCorrectMetadata() {
        val performer = Performer(
            id = "1",
            name = "Test Performer",
            imagePath = "https://example.com/performer.jpg",
            country = "USA",
            birthdate = "1990-01-15",
            sceneCount = 25,
            oCounter = 10
        )

        composeTestRule.setContent {
            PerformerCard(performer = performer, onClick = { })
        }

        // Name
        composeTestRule.onNodeWithText("Test Performer").assertIsDisplayed()

        // Age & Country (34y • USA)
        // Note: Age calculation depends on current date, so we just check country
        composeTestRule.onNodeWithText("USA", substring = true).assertIsDisplayed()

        // Scene Count
        composeTestRule.onNodeWithText("25").assertIsDisplayed()

        // O-Counter
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun performerCard_displaysShimmer_whenLoading() {
        composeTestRule.setContent {
            PerformerCard(
                performer = Performer(id = "1", name = "Test"),
                onClick = {},
                isLoading = true
            )
        }

        // When loading, the actual content should not be displayed
        composeTestRule.onNodeWithText("Test").assertDoesNotExist()
    }

    @Test
    fun performerCard_handlesNullValues() {
        val performer = Performer(
            id = "1",
            name = null,
            imagePath = null,
            country = null,
            birthdate = null,
            sceneCount = null,
            oCounter = null
        )

        composeTestRule.setContent {
            PerformerCard(performer = performer, onClick = { })
        }

        // Should display "Unknown" when name is null
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed()

        // Should display 0 for null counts
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }
}
