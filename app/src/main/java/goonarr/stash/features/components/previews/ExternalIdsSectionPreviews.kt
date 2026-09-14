package goonarr.stash.features.components.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.core.model.StashID
import goonarr.stash.features.components.ExternalIdsSection

@Preview(name = "External IDs Section", showBackground = true)
@Composable
private fun ExternalIdsSectionPreview() {
    val stashIds = listOf(
        StashID(endpoint = "https://stashdb.org/graphql", stashId = "12345678-1234-1234-1234-1234567890ab"),
        StashID(endpoint = "https://themoviedb.org", stashId = "987654321")
    )
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                ExternalIdsSection(stashIds = stashIds)
            }
        }
    }
}

@Preview(name = "External IDs Section Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExternalIdsSectionDarkPreview() {
    val stashIds = listOf(
        StashID(endpoint = "https://stashdb.org/graphql", stashId = "12345678-1234-1234-1234-1234567890ab"),
        StashID(endpoint = "https://themoviedb.org", stashId = "987654321")
    )
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                ExternalIdsSection(stashIds = stashIds)
            }
        }
    }
}

@Preview(name = "External IDs Section Single", showBackground = true)
@Composable
private fun ExternalIdsSectionSinglePreview() {
    val stashIds = listOf(
        StashID(endpoint = "https://stashdb.org/graphql", stashId = "12345678-1234-1234-1234-1234567890ab")
    )
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                ExternalIdsSection(stashIds = stashIds)
            }
        }
    }
}

@Preview(name = "External IDs Section Many", showBackground = true)
@Composable
private fun ExternalIdsSectionManyPreview() {
    val stashIds = (1..10).map { i ->
        StashID(endpoint = "https://example.com/endpoint/$i", stashId = "id-$i")
    }
    StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                ExternalIdsSection(stashIds = stashIds)
            }
        }
    }
}
