package goonarr.stash.features.performers

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.util.MockData

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 600)
@Composable
fun PerformerCardPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Standard Cards - Various Data", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Favorite with 5 star rating & O-Counter
                    PerformerCard(
                        performer = MockData.performer.copy(
                            name = "Jane Star",
                            favorite = true,
                            rating100 = 100,
                            sceneCount = 150,
                            oCounter = 42,
                            country = "USA",
                            birthdate = "1995-03-15"
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    // 3 star rating, no O-Counter
                    PerformerCard(
                        performer = MockData.performer.copy(
                            name = "Alex Blue",
                            favorite = false,
                            rating100 = 60,
                            sceneCount = 45,
                            oCounter = null,
                            country = null,
                            birthdate = "1992-08-20"
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Favorite, no rating, minimal data
                    PerformerCard(
                        performer = MockData.performer.copy(
                            name = "Sam Nova",
                            favorite = true,
                            rating100 = null,
                            sceneCount = 0,
                            oCounter = null,
                            country = null,
                            birthdate = null
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    // 4 stars, lots of data
                    PerformerCard(
                        performer = MockData.performer.copy(
                            name = "Riley Fox",
                            favorite = false,
                            rating100 = 80,
                            sceneCount = 230,
                            oCounter = 67,
                            country = "UK"
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 800)
@Composable
fun PerformerHeroCardPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Hero Cards - Various Data", style = MaterialTheme.typography.labelLarge)

                // Favorite with full data
                PerformerHeroCard(
                    performer = MockData.fullPerformer.copy(
                        name = "Emma Thompson",
                        favorite = true,
                        rating100 = 100,
                        sceneCount = 180,
                        oCounter = 55,
                        country = "Canada"
                    ),
                    onClick = {}
                )

                // Not favorite, 2 stars, no o-counter
                PerformerHeroCard(
                    performer = MockData.performer.copy(
                        name = "Marcus Stone",
                        favorite = false,
                        rating100 = 40,
                        sceneCount = 25,
                        oCounter = null,
                        country = "Germany",
                        birthdate = "1988-03-15"
                    ),
                    onClick = {}
                )

                // Favorite, no rating, minimal scenes
                PerformerHeroCard(
                    performer = MockData.performer.copy(
                        name = "Luna Grey",
                        favorite = true,
                        rating100 = null,
                        sceneCount = 3,
                        oCounter = 1,
                        country = null,
                        birthdate = null
                    ),
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerCompactCardPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Compact Cards - Favorites & Ratings", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Favorite, 5 stars, with O-Counter
                    PerformerCompactCard(
                        performer = MockData.performer.copy(
                            name = "Star One",
                            favorite = true,
                            rating100 = 100,
                            sceneCount = 90,
                            oCounter = 25,
                            birthdate = "1994-05-10"
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    // Not favorite, 4 stars, no O-Counter
                    PerformerCompactCard(
                        performer = MockData.performer.copy(
                            name = "Star Two",
                            favorite = false,
                            rating100 = 80,
                            sceneCount = 45,
                            oCounter = null,
                            birthdate = "1997-11-03"
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    // Favorite, no rating, with O-Counter
                    PerformerCompactCard(
                        performer = MockData.performer.copy(
                            name = "Star Three",
                            favorite = true,
                            rating100 = null,
                            sceneCount = 12,
                            oCounter = 8,
                            birthdate = "1999-02-28"
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    )
                }

                Text("Compact Cards - Minimal Data", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // No scenes, no age
                    PerformerCompactCard(
                        performer = MockData.performer.copy(
                            name = "New One",
                            favorite = false,
                            rating100 = null,
                            sceneCount = 0,
                            birthdate = null
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    // Favorite, 1 star, minimal
                    PerformerCompactCard(
                        performer = MockData.performer.copy(
                            name = "New Two",
                            favorite = true,
                            rating100 = 20,
                            sceneCount = 2,
                            birthdate = null
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    // 3 stars, no favorite
                    PerformerCompactCard(
                        performer = MockData.performer.copy(
                            name = "New Three",
                            favorite = false,
                            rating100 = 60,
                            sceneCount = 78
                        ),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerCardShimmerPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Card Shimmer", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PerformerCard(
                        performer = MockData.performer,
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.weight(1f)
                    )
                    PerformerCard(
                        performer = MockData.performer,
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Compact Card Shimmer", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PerformerCompactCard(
                        performer = MockData.performer,
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.weight(1f)
                    )
                    PerformerCompactCard(
                        performer = MockData.performer,
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.weight(1f)
                    )
                    PerformerCompactCard(
                        performer = MockData.performer,
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
