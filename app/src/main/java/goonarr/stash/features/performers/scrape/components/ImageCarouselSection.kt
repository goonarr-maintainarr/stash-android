package goonarr.stash.features.performers.scrape.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashTheme
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun ImageCarouselSection(
    currentImageUrl: String?,
    scrapedImages: List<String>,
    selectedImageIndex: Int,
    useImage: Boolean,
    onImageSelected: (Int) -> Unit,
    onUseImageChanged: (Boolean) -> Unit
) {
    val colors = StashTheme.colors
    val haptic = LocalStashHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Image",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Checkbox(
                    checked = useImage,
                    onCheckedChange = {
                        haptic.perform(StashHapticFeedbackType.Light)
                        onUseImageChanged(it)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.primary,
                        uncheckedColor = colors.complementary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Image
            if (currentImageUrl != null) {
                Text(
                    text = "Current",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.complementary
                )
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Current image",
                    alignment = Alignment.TopCenter,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Scraped Images Carousel
            Text(
                text = "Scraped Images (${scrapedImages.size})",
                style = MaterialTheme.typography.labelSmall,
                color = colors.complementary
            )
            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                itemsIndexed(scrapedImages) { index, imageUrl ->
                    val isSelected = index == selectedImageIndex && useImage
                    val context = LocalContext.current
                    var extractedColor by remember { mutableStateOf<Color?>(null) }

                    LaunchedEffect(imageUrl) {
                        extractedColor = HeroAccentColor.extract(context, imageUrl)
                    }

                    // Bounce animation when selected
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "imageBounce"
                    )

                    // Create gradient colors for border
                    val gradientColors = extractedColor?.let { baseColor ->
                        listOf(
                            baseColor,
                            baseColor.copy(
                                red = (baseColor.red + 0.2f).coerceIn(0f, 1f),
                                green = (baseColor.green - 0.1f).coerceIn(0f, 1f),
                                blue = (baseColor.blue + 0.15f).coerceIn(0f, 1f)
                            ),
                            baseColor.copy(
                                red = (baseColor.red - 0.1f).coerceIn(0f, 1f),
                                green = (baseColor.green + 0.15f).coerceIn(0f, 1f),
                                blue = (baseColor.blue + 0.2f).coerceIn(0f, 1f)
                            ),
                            baseColor
                        )
                    } ?: listOf(
                        colors.primary,
                        colors.primary.copy(alpha = 0.7f),
                        colors.complementary.copy(alpha = 0.5f),
                        colors.primary
                    )

                    val borderWidth = if (isSelected) 3.dp else 2.dp

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(3f / 4f)
                            .scale(scale)
                            .clip(RoundedCornerShape(8.dp))
                            .drawBehind {
                                val strokeWidth = borderWidth.toPx()
                                drawRoundRect(
                                    brush = Brush.sweepGradient(
                                        colors = gradientColors,
                                        center = Offset(size.width / 2, size.height / 2)
                                    ),
                                    size = size,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                                )
                                // Inner rect to create border effect
                                drawRoundRect(
                                    color = Color.Black,
                                    topLeft = Offset(strokeWidth, strokeWidth),
                                    size = androidx.compose.ui.geometry.Size(
                                        size.width - strokeWidth * 2,
                                        size.height - strokeWidth * 2
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius((8.dp - borderWidth).toPx())
                                )
                            }
                            .clickable {
                                haptic.perform(StashHapticFeedbackType.Selection)
                                onImageSelected(index)
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Scraped image ${index + 1}",
                            alignment = Alignment.TopCenter,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isSelected) 3.dp else 2.dp)
                                .clip(RoundedCornerShape((8.dp - borderWidth).coerceAtLeast(4.dp)))
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .background(colors.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
