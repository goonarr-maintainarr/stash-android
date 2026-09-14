package goonarr.stash.features.scenes.detail.components.markers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.core.model.SceneMarker

@Composable
fun SceneMarkersSection(
    markers: List<SceneMarker>,
    onSeek: (Double) -> Unit,
    getStreamUrl: suspend (String?) -> String?
) {
    if (markers.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SCENE MARKERS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelLarge,
                    color = StashTheme.colors.complementary
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${markers.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, end = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(markers.sortedBy { it.seconds }) { marker ->
                SceneMarkerCard(
                    marker = marker,
                    onSeek = onSeek,
                    getStreamUrl = getStreamUrl,
                    modifier = Modifier.width(320.dp)
                )
            }
        }
    }
}
