package goonarr.stash.features.performers

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import goonarr.stash.core.model.Performer
import goonarr.stash.features.components.bounceClickable
import goonarr.stash.features.components.rememberShimmerBrush
import goonarr.stash.features.performers.components.PerformerBanner

@Composable
fun PerformerCard(
    performer: Performer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    if (isLoading) {
        PerformerCardShimmer(modifier)
    } else {
        PerformerCardContent(performer, onClick, modifier, animatedVisibilityScope, sharedTransitionScope)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PerformerCardContent(
    performer: Performer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val sharedElementModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "performer_image_${performer.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 300)
                }
            )
        }
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .bounceClickable(shape = RoundedCornerShape(12.dp), onClick = onClick)
            .then(sharedElementModifier),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        PerformerBanner(
            performer = performer,
            isInteractive = false,
            showDetails = true,
            compactRating = true,
            useSmallFonts = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PerformerCardShimmer(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()

    Card(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image shimmer background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush)
            )

            // Gradient Overlay (same as actual card)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )

            // Info Overlay shimmer
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                // Name shimmer
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Age & Country shimmer
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scene count and O-counter shimmer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Scene Count shimmer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                    }

                    // O-Counter shimmer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PerformerHeroCard(
    performer: Performer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 300.dp,
    isLoading: Boolean = false,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    if (isLoading) {
        PerformerHeroCardShimmer(modifier, height)
    } else {
        PerformerHeroCardContent(performer, onClick, modifier, height, animatedVisibilityScope, sharedTransitionScope)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PerformerHeroCardContent(
    performer: Performer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 300.dp,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val sharedElementModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "performer_image_${performer.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 300)
                }
            )
        }
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .bounceClickable(shape = RoundedCornerShape(16.dp), onClick = onClick)
            .then(sharedElementModifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        PerformerBanner(
            performer = performer,
            isInteractive = false,
            showDetails = true,
            showCountry = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PerformerHeroCardShimmer(
    modifier: Modifier = Modifier,
    height: Dp = 300.dp
) {
    val brush = rememberShimmerBrush()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush)
            )

            // Dynamic Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 0f
                        )
                    )
            )

            // Metadata alignment
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Name
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Age
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )

                    // Country
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )

                    // Scene info
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(50.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}

/**
 * Compact performer card for Grid (Small) view.
 * Shows only the performer image with their name overlaid.
 * Optimized for fast scrolling through many performers.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PerformerCompactCard(
    performer: Performer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    if (isLoading) {
        PerformerCompactCardShimmer(modifier)
    } else {
        val sharedElementModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
            with(sharedTransitionScope) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "performer_image_${performer.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 300)
                    }
                )
            }
        } else {
            Modifier
        }

        Card(
            modifier = modifier
                .aspectRatio(3f / 4f)
                .bounceClickable(
                    maxElevation = 2.dp,
                    minElevation = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    onClick = onClick
                )
                .then(sharedElementModifier),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            PerformerBanner(
                performer = performer,
                isInteractive = false,
                showDetails = true,
                compactRating = true,
                useTinyFonts = true,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PerformerCompactCardShimmer(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()

    Card(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 200f
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .height(14.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}
