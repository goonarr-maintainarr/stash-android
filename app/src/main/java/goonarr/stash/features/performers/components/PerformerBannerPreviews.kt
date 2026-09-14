package goonarr.stash.features.performers.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.util.MockData

@Preview(name = "Light Mode", heightDp = 1500, showBackground = true)
@Preview(name = "Dark Mode", heightDp = 1500, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerBannerPreview() {
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Static Mode (Card Style)", style = MaterialTheme.typography.titleMedium)
                // Use a Row with weights to simulate grid columns
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PerformerBanner(
                        performer = MockData.performer.copy(
                            name = "Card Performer",
                            favorite = true,
                            rating100 = 80,
                            sceneCount = 42,
                            oCounter = 5
                        ),
                        isInteractive = false,
                        compactRating = true,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    PerformerBanner(
                        performer = MockData.performer.copy(
                            name = "Card Performer II",
                            favorite = false,
                            rating100 = 60,
                            sceneCount = 10,
                            oCounter = 0
                        ),
                        isInteractive = false,
                        compactRating = true,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Text("List Mode (Hero Style)", style = MaterialTheme.typography.titleMedium)
                PerformerBanner(
                    performer = MockData.performer.copy(
                        name = "List Performer",
                        favorite = true,
                        rating100 = 90,
                        country = "Canada",
                        sceneCount = 15
                    ),
                    isInteractive = false,
                    showCountry = true,
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )

                Text("Interactive Mode (Detail Style)", style = MaterialTheme.typography.titleMedium)
                PerformerBanner(
                    performer = MockData.performer.copy(
                        name = "Detail Performer",
                        favorite = false,
                        rating100 = 60,
                        sceneCount = 100,
                        country = "USA"
                    ),
                    isInteractive = true,
                    showCountry = true,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )

                Text("No Details Interactive", style = MaterialTheme.typography.titleMedium)
                PerformerBanner(
                    performer = MockData.performer.copy(
                        name = "Unknown Details",
                        favorite = true
                    ),
                    isInteractive = true,
                    showDetails = false,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}
