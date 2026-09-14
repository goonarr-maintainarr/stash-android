package goonarr.stash.features.scenes.detail.components.fileinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.SceneFile
import goonarr.stash.features.components.DetailCard
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.StringFormatters

private val DividerAlpha = StashTokens.Alpha.Divider

@Composable
fun FileInfoSection(file: SceneFile) {
    Column(
        modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "FILE INFO",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        val cardColor = StashTheme.colors.primary.copy(alpha = 0.15f)
        val borderColor = StashTheme.colors.primary.copy(alpha = 0.5f)

        DetailCard(containerColor = cardColor, borderColor = borderColor) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val items = buildList {
                    add("Path" to (file.path ?: "Unknown"))

                    val resolution =
                        if (file.width != null && file.height != null && file.width > 0 && file.height > 0) {
                            "${file.width}x${file.height}"
                        } else {
                            "Unknown"
                        }
                    add("Resolution" to resolution)

                    add("Duration" to DateFormatters.formatDuration(file.duration ?: 0.0))
                    add("Size" to StringFormatters.formatFileSize(file.size ?: 0L))

                    file.videoCodec?.let { add("Video Codec" to it) }
                    file.audioCodec?.let { add("Audio Codec" to it) }
                }

                items.forEachIndexed { index, (label, value) ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = DividerAlpha))
                    }
                    InfoRow(label, value)
                }
            }
        }
    }
}
