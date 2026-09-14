package goonarr.stash.features.tags.card

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.core.model.Tag

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TagRow(
    modifier: Modifier = Modifier,
    tag: Tag,
    onClick: () -> Unit,
    onUnfollow: (() -> Unit)? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current

            // Tag Image
            val sharedImageModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "tag_image_${tag.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = 300)
                        }
                    )
                }
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .height(72.dp)
                    .width(72.dp)
                    .then(sharedImageModifier)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.surfaceVariant)
            ) {
                if (!tag.imagePath.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(tag.imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(4.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                }

                if (tag.favorite == true) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(16.dp),
                        tint = colorScheme.error
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tag.name,
                        style = typography.titleMedium,
                        color = colorScheme.secondary
                    )
                }

                tag.sceneCount?.let { count ->
                    Text(
                        text = "$count scenes",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                tag.sceneMarkerCount?.let { count ->
                    if (count > 0) {
                        Text(
                            text = "$count markers",
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (onUnfollow != null) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUnfollow()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Unfollow",
                        tint = colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TagCardShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .height(72.dp)
                    .width(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )

            Column(modifier = Modifier.weight(1f)) {
                // Name placeholder
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .clip(shapes.small)
                        .background(colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Count placeholder
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(14.dp)
                        .clip(shapes.small)
                        .background(
                            colorScheme.onSurfaceVariant.copy(
                                alpha = 0.2f
                            )
                        )
                )
            }
        }
    }
}
