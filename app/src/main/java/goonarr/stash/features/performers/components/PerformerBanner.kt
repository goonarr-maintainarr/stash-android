package goonarr.stash.features.performers.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.LocalBlurNsfw
import goonarr.stash.R
import goonarr.stash.core.model.Performer
import goonarr.stash.features.components.RatingBar
import goonarr.stash.util.DateFormatters

@Composable
fun PerformerBanner(
    performer: Performer,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.TopCenter,
    showDetails: Boolean = true,
    showCountry: Boolean = false,
    compactRating: Boolean = false,
    useSmallFonts: Boolean = false,
    useTinyFonts: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onRatingChange: (Int) -> Unit = {}
) {
    Box(modifier = modifier) {
        val context = LocalContext.current
        val metrics = BannerDefaults.getMetrics(useSmallFonts, useTinyFonts)

        val stablePath = performer.imagePath?.substringBefore("?")
        val imageRequest = remember(stablePath) {
            ImageRequest.Builder(context)
                .data(stablePath)
                .crossfade(true)
                .build()
        }

        // Image
        AsyncImage(
            model = imageRequest,
            contentDescription = performer.name,
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier
                .fillMaxSize()
                .then(if (LocalBlurNsfw.current) Modifier.blur(20.dp) else Modifier)
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Favorite Heart
        FavoriteIcon(
            isFavorite = performer.favorite == true,
            isInteractive = isInteractive,
            size = metrics.favoriteSize,
            onFavoriteClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(if (isInteractive) 8.dp else 12.dp)
        )

        // Details Overlay (Name, Stats)
        // Rating (Bottom End)
        if (showDetails && !compactRating && (isInteractive || (performer.rating100 ?: 0) > 0)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(if (isInteractive) 16.dp else 12.dp)
            ) {
                val stars = (performer.rating100 ?: 0) / 20
                RatingBar(
                    rating = stars,
                    onRatingChanged = if (isInteractive) {
                        onRatingChange
                    } else {
                        null
                    },
                    starSize = 16.dp,
                    modifier = Modifier
                )
            }
        }

        if (showDetails) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(if (isInteractive) 16.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name
                Text(
                    text = performer.name ?: "Unknown",
                    style = metrics.nameStyle,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = if (compactRating) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Stats Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left Side: Stats
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(metrics.statsOuterSpacing)
                    ) {
                        // Age
                        val age = DateFormatters.calculateAge(performer.birthdate, performer.deathDate)
                        if (age != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(metrics.statsIconSize)
                                )
                                Spacer(modifier = Modifier.width(metrics.statsInnerSpacing))
                                Text(
                                    text = "$age",
                                    style = metrics.statsTextStyle,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = metrics.statsTextSize,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Country
                        if (showCountry && !performer.country.isNullOrEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(metrics.statsIconSize)
                                )
                                Spacer(modifier = Modifier.width(metrics.statsInnerSpacing))
                                Text(
                                    text = performer.country,
                                    style = metrics.statsTextStyle,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = metrics.statsTextSize,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Scene Count
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(metrics.statsIconSize)
                            )
                            Spacer(modifier = Modifier.width(metrics.statsInnerSpacing))
                            Text(
                                text = "${performer.sceneCount ?: 0}",
                                style = metrics.statsTextStyle,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = metrics.statsTextSize,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // O-Counter
                        val oCount = performer.oCounter ?: 0
                        if (oCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sweat_drops),
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(metrics.statsIconSize)
                                )
                                Spacer(modifier = Modifier.width(metrics.statsInnerSpacing))
                                Text(
                                    text = "$oCount",
                                    style = metrics.statsTextStyle,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = metrics.statsTextSize,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Right Side: Compact Rating (Icon + Score)
                    if (compactRating) {
                        val rating100 = performer.rating100
                        if (rating100 != null && rating100 > 0) {
                            val ratingValue = rating100 / 20.0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star_filled),
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(metrics.statsIconSize)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1g", ratingValue),
                                    style = metrics.statsTextStyle,
                                    color = Color(0xFFFFD700),
                                    fontSize = metrics.statsTextSize,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteIcon(
    isFavorite: Boolean,
    isInteractive: Boolean,
    size: Dp,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isInteractive) {
        val haptic = LocalHapticFeedback.current
        var isBouncing by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isBouncing) 1.25f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "HeartBounce",
            finishedListener = { if (isBouncing) isBouncing = false }
        )

        IconButton(
            onClick = {
                isBouncing = true
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onFavoriteClick()
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) Color(0xFFE53935) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(size)
                    .scale(scale)
            )
        }
    } else {
        if (isFavorite) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                tint = Color(0xFFE53935),
                modifier = modifier.size(size)
            )
        }
    }
}

private data class BannerMetrics(
    val favoriteSize: Dp,
    val nameStyle: androidx.compose.ui.text.TextStyle,
    val statsOuterSpacing: Dp,
    val statsInnerSpacing: Dp,
    val statsIconSize: Dp,
    val statsTextSize: androidx.compose.ui.unit.TextUnit,
    val statsTextStyle: androidx.compose.ui.text.TextStyle
)

private object BannerDefaults {
    @Composable
    fun getMetrics(useSmallFonts: Boolean, useTinyFonts: Boolean): BannerMetrics {
        return if (useTinyFonts) {
            BannerMetrics(
                favoriteSize = 18.dp,
                nameStyle = MaterialTheme.typography.labelSmall,
                statsOuterSpacing = 6.dp,
                statsInnerSpacing = 2.dp,
                statsIconSize = 10.dp,
                statsTextSize = 8.sp,
                statsTextStyle = MaterialTheme.typography.labelSmall
            )
        } else if (useSmallFonts) {
            BannerMetrics(
                favoriteSize = 24.dp,
                nameStyle = MaterialTheme.typography.titleSmall,
                statsOuterSpacing = 8.dp,
                statsInnerSpacing = 4.dp,
                statsIconSize = 12.dp,
                statsTextSize = 10.sp,
                statsTextStyle = MaterialTheme.typography.labelSmall
            )
        } else {
            BannerMetrics(
                favoriteSize = 28.dp,
                nameStyle = MaterialTheme.typography.headlineSmall,
                statsOuterSpacing = 10.dp,
                statsInnerSpacing = 6.dp,
                statsIconSize = 16.dp,
                statsTextSize = 14.sp,
                statsTextStyle = MaterialTheme.typography.titleMedium
            )
        }
    }
}
