package goonarr.stash.features.scenes.detail.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import goonarr.stash.R
import goonarr.stash.core.model.Scene

@Composable
fun SceneHistorySection(scene: Scene) {
    if ((scene.oHistory.isNullOrEmpty()) && (scene.playHistory.isNullOrEmpty())) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!scene.oHistory.isNullOrEmpty()) {
            HistoryList(
                title = "O HISTORY",
                dates = scene.oHistory,
                iconContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sweat_drops),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
        if (!scene.playHistory.isNullOrEmpty()) {
            HistoryList(
                title = "PLAY HISTORY",
                dates = scene.playHistory,
                totalDuration = scene.playDuration,
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    }
}
