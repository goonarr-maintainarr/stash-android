package goonarr.stash.features.studios.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Studio

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun StudioCompactRowPreview() {
    StashTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StudioCompactRow(
                studio = Studio(
                    id = "1",
                    name = "Tushy",
                    details = "The art of anal. Premium quality scenes featuring the most beautiful performers.",
                    sceneCount = 124,
                    performerCount = 56,
                    imagePath = "https://example.com/logo.png",
                    favorite = true,
                    rating100 = 80
                ),
                onClick = {}
            )

            StudioCompactRow(
                studio = Studio(
                    id = "2",
                    name = "Blacked",
                    details = "Interracial scenes at their finest.",
                    sceneCount = 89,
                    performerCount = 42,
                    imagePath = null,
                    favorite = false,
                    rating100 = 60
                ),
                onClick = {}
            )

            StudioCompactRow(
                studio = Studio(
                    id = "3",
                    name = "Very Long Studio name that should wrap to two lines if it is long enough which it is now",
                    details = "This description is also quite long and should be truncated to a single line even though" +
                        " it has more information to show.",
                    sceneCount = 42,
                    performerCount = 12,
                    imagePath = null,
                    favorite = true,
                    rating100 = 100
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun StudioCompactRowShimmerPreview() {
    StashTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StudioCompactRowShimmer()
            StudioCompactRowShimmer()
            StudioCompactRowShimmer()
        }
    }
}
