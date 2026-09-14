package goonarr.stash.features.scenes.detail.components.metadata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Scene
import goonarr.stash.features.components.CollapsibleLinks
import goonarr.stash.features.components.DetailCard
import goonarr.stash.features.scenes.detail.components.fileinfo.InfoRow
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

private val DividerAlpha = StashTokens.Alpha.Divider

@Composable
fun SceneAdditionalMetadataSection(scene: Scene) {
    if (scene.director.isNullOrEmpty() && scene.code.isNullOrEmpty() &&
        scene.url.isNullOrEmpty() && (scene.urls.isNullOrEmpty()) &&
        scene.createdAt.isNullOrEmpty() && scene.updatedAt.isNullOrEmpty()
    ) {
        return
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "DETAILS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        val cardColor = StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBackground)
        val borderColor = StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBorder)

        DetailCard(containerColor = cardColor, borderColor = borderColor) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var hasPreviousItem = false

                // Director
                scene.director?.takeIf { it.isNotEmpty() }?.let {
                    InfoRow(label = "Director", value = it)
                    hasPreviousItem = true
                }

                // Code
                scene.code?.takeIf { it.isNotEmpty() }?.let {
                    if (hasPreviousItem) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = DividerAlpha
                            )
                        )
                    }
                    InfoRow(label = "Code", value = it)
                    hasPreviousItem = true
                }

                // URLs
                val urls = scene.urls ?: emptyList()
                val singleUrl = scene.url

                if (urls.size > 1) {
                    if (hasPreviousItem) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = DividerAlpha
                            )
                        )
                    }
                    CollapsibleLinks(
                        urls = urls,
                        title = "URLs",
                        modifier = Modifier.fillMaxWidth(),
                        showBackground = false
                    )
                    hasPreviousItem = true
                } else {
                    val urlToShow = urls.firstOrNull() ?: singleUrl
                    urlToShow?.takeIf { it.isNotEmpty() }?.let {
                        if (hasPreviousItem) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = DividerAlpha
                                )
                            )
                        }
                        val uriHandler = LocalUriHandler.current
                        val haptic = LocalStashHapticFeedback.current
                        InfoRow(
                            label = "URL",
                            value = it,
                            isLink = true,
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Light)
                                uriHandler.openUri(it)
                            }
                        )
                        hasPreviousItem = true
                    }
                }

                // Created At
                scene.createdAt?.takeIf { it.isNotEmpty() }?.let {
                    if (hasPreviousItem) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = DividerAlpha
                            )
                        )
                    }
                    InfoRow(label = "Created", value = DateFormatters.formatTimestamp(it))
                    hasPreviousItem = true
                }

                // Updated At
                scene.updatedAt?.takeIf { it.isNotEmpty() }?.let {
                    if (hasPreviousItem) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = DividerAlpha
                            )
                        )
                    }
                    InfoRow(label = "Updated", value = DateFormatters.formatTimestamp(it))
                }
            }
        }
    }
}
