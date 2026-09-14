package goonarr.stash.features.scenes.edit

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import coil.compose.AsyncImage
import goonarr.stash.LocalBlurNsfw
import goonarr.stash.R
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.StashID
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.EditSection
import goonarr.stash.features.components.EditTextField
import goonarr.stash.features.components.SearchField
import goonarr.stash.features.components.bounceClickable
import goonarr.stash.features.performers.PerformerCompactCard
import goonarr.stash.features.performers.PerformerHeroCard
import goonarr.stash.features.player.VideoPlayer
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun DetailsSection(
    title: String,
    onTitleChange: (String) -> Unit,
    director: String,
    onDirectorChange: (String) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit
) {
    EditSection(title = "DETAILS") {
        Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
            EditTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "Title"
            )
            EditTextField(
                value = director,
                onValueChange = onDirectorChange,
                label = "Director"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
                EditTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = "URL",
                    modifier = Modifier.weight(1f)
                )
                EditTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    label = "Code",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DescriptionSection(details: String, onDetailsChange: (String) -> Unit) {
    EditSection(title = "DESCRIPTION") {
        EditTextField(
            value = details,
            onValueChange = onDetailsChange,
            label = "Description",
            minLines = 3,
            singleLine = false
        )
    }
}

@Composable
fun PerformersSection(
    currentPerformers: List<Performer>,
    onRemovePerformer: (String) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchResults: List<Performer>,
    isSearching: Boolean,
    onAddPerformer: (Performer) -> Unit
) {
    EditSection(
        title = "PERFORMERS",
        count = currentPerformers.size,
        dotColor = StashTheme.colors.tertiary,
        iconContent = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
            currentPerformers.forEach { performer ->
                PerformerEditItem(performer = performer, onRemove = { onRemovePerformer(performer.id) })
            }

            SearchField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = "Add Performer...",
                isSearching = isSearching,
                color = StashTheme.colors.primary
            )

            if (searchResults.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(searchResults) { performer ->
                        PerformerSearchItem(
                            performer = performer,
                            onAdd = { onAddPerformer(performer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagsSection(
    currentTags: List<Tag>,
    onRemoveTag: (String) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchResults: List<Tag>,
    isSearching: Boolean,
    onAddTag: (Tag) -> Unit
) {
    EditSection(
        title = "TAGS",
        count = currentTags.size,
        dotColor = StashTheme.colors.tertiary,
        iconContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    ) {
        goonarr.stash.features.components.TagPicker(
            selectedTags = currentTags,
            onRemoveTag = onRemoveTag,
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            searchResults = searchResults,
            isSearching = isSearching,
            onAddTag = onAddTag,
            color = StashTheme.colors.tertiary
        )
    }
}

@Composable
fun HistorySection(title: String, items: List<String>, onRemove: (String) -> Unit) {
    if (items.isEmpty()) return
    EditSection(
        title = title,
        count = items.size,
        dotColor = StashTheme.colors.primary,
        iconContent = {
            if (title.contains("O HISTORY", ignoreCase = true)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sweat_drops),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical)) {
            items.forEach { timestamp ->
                HistoryEditItem(timestamp = timestamp, onRemove = { onRemove(timestamp) })
            }
        }
    }
}

@Composable
fun MarkersSection(
    currentMarkers: List<SceneMarker>,
    onDeleteMarker: (String) -> Unit,
    getStreamUrl: suspend (String?) -> String?
) {
    if (currentMarkers.isEmpty()) return
    EditSection(
        title = "SCENE MARKERS",
        count = currentMarkers.size,
        dotColor = StashTheme.colors.complementary,
        iconContent = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical)) {
            currentMarkers.forEach { marker ->
                MarkerEditItem(
                    marker = marker,
                    onDelete = { onDeleteMarker(marker.id) },
                    getStreamUrl = getStreamUrl
                )
            }
        }
    }
}

@Composable
fun PerformerEditItem(performer: Performer, onRemove: () -> Unit) {
    val haptic = LocalStashHapticFeedback.current
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        PerformerHeroCard(
            performer = performer,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            height = 360.dp
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(StashTokens.Spacing.CardPadding)
                .size(36.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = StashTokens.Alpha.TextSecondary)
        ) {
            IconButton(onClick = {
                haptic.perform(StashHapticFeedbackType.Medium)
                onRemove()
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PerformerSearchItem(
    performer: Performer,
    onAdd: (Performer) -> Unit,
    imagePath: Any? = performer.imagePath
) {
    val haptic = LocalStashHapticFeedback.current
    Box(
        modifier = Modifier
            .width(140.dp)
    ) {
        PerformerCompactCard(
            performer = performer,
            onClick = {
                haptic.perform(StashHapticFeedbackType.Selection)
                onAdd(performer)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HistoryEditItem(timestamp: String, onRemove: () -> Unit) {
    val haptic = LocalStashHapticFeedback.current
    val dynamicColors = StashTheme.colors
    Surface(
        color = dynamicColors.primary.copy(alpha = 0.3f),
        shape = RoundedCornerShape(StashTokens.Radius.Medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = dynamicColors.complementary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = DateFormatters.formatTimestamp(timestamp),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onRemove()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun MarkerEditItem(
    marker: SceneMarker,
    onDelete: () -> Unit,
    getStreamUrl: suspend (String?) -> String?
) {
    var streamUrl by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    val haptic = LocalStashHapticFeedback.current
    val dynamicColors = StashTheme.colors

    // Resolve stream URL
    LaunchedEffect(marker.stream) {
        streamUrl = getStreamUrl(marker.stream)
    }

    val containerShape = RoundedCornerShape(StashTokens.Radius.Card)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClickable(
                shape = containerShape,
                spotColor = dynamicColors.primary,
                ambientColor = dynamicColors.primary,
                onClick = {
                    if (streamUrl != null) {
                        isPlaying = !isPlaying
                    }
                }
            )
            .background(dynamicColors.complementary.copy(alpha = 0.3f), containerShape)
    ) {
        Row(
            modifier = Modifier.padding(StashTokens.Spacing.ContentVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Preview image or video
            Box(
                modifier = Modifier
                    .size(90.dp, 65.dp)
                    .clip(RoundedCornerShape(StashTokens.Radius.Medium))
                    .background(Color.Black)
                    .then(
                        if (isPlaying) {
                            Modifier.background(StashTheme.colors.primary.copy(alpha = 0.2f))
                        } else {
                            Modifier
                        }
                    )
            ) {
                val isBlurred = LocalBlurNsfw.current

                if (isPlaying && streamUrl != null && !isBlurred) {
                    VideoPlayer(
                        url = streamUrl,
                        isMuted = true,
                        useController = false,
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                        onPlayerCreated = { player ->
                            player.repeatMode = Player.REPEAT_MODE_ONE
                            player.play()
                        }
                    )
                } else {
                    AsyncImage(
                        model = marker.preview ?: marker.stream,
                        contentDescription = marker.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (isBlurred) Modifier.blur(10.dp) else Modifier)
                    )

                    if (streamUrl != null && !isBlurred) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = marker.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val durationText = DateFormatters.formatDuration(marker.seconds) +
                    (marker.endSeconds?.let { " - ${DateFormatters.formatDuration(it)}" } ?: "")
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val tags = (marker.tags.orEmpty() + listOfNotNull(marker.primaryTag)).distinctBy { it.id }
                if (tags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(tags) { tag ->
                            Surface(
                                color = StashTheme.colors.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(StashTokens.Radius.Small),
                            ) {
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onDelete()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun StashIdsSection(
    stashIds: List<StashID>,
    onRemove: (String) -> Unit
) {
    if (stashIds.isEmpty()) return

    EditSection(
        title = "STASH IDS",
        count = stashIds.size,
        dotColor = StashTheme.colors.primary,
        iconContent = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stashIds.forEach { stashId ->
                StashIdEditItem(
                    stashId = stashId,
                    onRemove = { onRemove(stashId.stashId) }
                )
            }
        }
    }
}

@Composable
fun StashIdEditItem(
    stashId: StashID,
    onRemove: () -> Unit
) {
    val haptic = LocalStashHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StashTheme.colors.primary.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stashId.endpoint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stashId.stashId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = {
                haptic.perform(StashHapticFeedbackType.Medium)
                onRemove()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Stash ID",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
