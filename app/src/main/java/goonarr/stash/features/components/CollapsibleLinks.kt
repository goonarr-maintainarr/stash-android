package goonarr.stash.features.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashTheme
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import java.net.URI

/**
 * A collapsible section for displaying a list of external URLs.
 * Displays a stack of favicon images when collapsed and a full list with labels when expanded.
 *
 * @param modifier Optional [Modifier].
 * @param urls The list of URL strings to display.
 * @param title The title of the section (e.g., "Links").
 * @param showBackground Whether to wrap the content in a themed background card.
 */
@Composable
fun CollapsibleLinks(
    modifier: Modifier = Modifier,
    urls: List<String>,
    title: String = "Links",
    showBackground: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 90f else 0f, label = "rotation")

    val containerModifier = if (showBackground) {
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    } else {
        modifier
    }

    val contentPadding = if (showBackground) 12.dp else 0.dp
    val verticalPadding = if (showBackground) 12.dp else 4.dp

    Column(modifier = containerModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = contentPadding, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: Title Only
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Match InfoRow label style if no background
                if (!showBackground) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        // Match InfoRow label width
                        modifier = Modifier.width(100.dp)
                    )
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right Side: Stacked Favicons + Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Show stack when collapsed OR when empty (though hidden if empty usually)
                if (!isExpanded && urls.isNotEmpty()) {
                    val stackUrls = urls.take(5)

                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            stackUrls.forEachIndexed { index, url ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(1.5.dp)
                                ) {
                                    FaviconImage(url = url, size = 24.dp)
                                }
                            }
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Staggered List
        Column(modifier = Modifier.padding(bottom = if (showBackground && isExpanded) 8.dp else 0.dp)) {
            urls.forEachIndexed { index, url ->
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = index * 30)) +
                        expandVertically(animationSpec = tween(durationMillis = 300, delayMillis = index * 30)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 200)) +
                        shrinkVertically(animationSpec = tween(durationMillis = 200))
                ) {
                    Column {
                        if (index > 0) {
                            HorizontalDivider(
                                // Adjust indent
                                modifier = Modifier.padding(start = if (showBackground) 44.dp else 100.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                        LinkRow(url = url, showBackground = showBackground)
                    }
                }
            }
        }
    }
}

/**
 * A single row within the expanded [CollapsibleLinks] list.
 */
@Composable
private fun LinkRow(url: String, showBackground: Boolean) {
    val uriHandler = LocalUriHandler.current
    val haptic = LocalStashHapticFeedback.current
    val horizontalPadding = if (showBackground) 12.dp else 0.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    haptic.perform(StashHapticFeedbackType.Light)
                    uriHandler.openUri(url)
                } catch (e: Exception) { }
            }
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indent content to align with value part if no background
        if (!showBackground) {
            Spacer(modifier = Modifier.width(100.dp)) // Offset to match label width
            Spacer(modifier = Modifier.width(16.dp)) // Extra spacing existing in header? No, header uses spacedBy(16.dp)
            // Wait, the header has [Label 100dp] [gap 16dp] [Content]
            // So here we should probably replicate that.
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            FaviconImage(url = url, size = 20.dp)
            Text(
                text = getLabelForUrl(url),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open Link",
            modifier = Modifier
                .size(12.dp)
                .rotate(-45f),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * Displays a favicon image for a given URL, falling back to a generic globe icon.
 * Fetches favicons using Google's favicon service.
 */
@Composable
private fun FaviconImage(url: String, size: Dp) {
    val host = try {
        URI(url).host
    } catch (e: Exception) {
        null
    }
    val faviconUrl = if (host != null) "https://www.google.com/s2/favicons?domain=$host&sz=64" else null

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        if (faviconUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(faviconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                error = rememberVectorPainter(Icons.Default.Public),
                placeholder = null
            )
        } else {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size / 4f),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CollapsibleLinksPreview() {
    val urls = listOf(
        "https://twitter.com/test",
        "https://instagram.com/test",
        "https://onlyfans.com/test",
        "https://stashdb.org/scenes/123",
        "https://theporndb.net/scenes/456"
    )
    StashTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Multiple Links (Background)", style = MaterialTheme.typography.titleMedium)
            CollapsibleLinks(urls = urls, showBackground = true)

            Text("Single Link (No Background)", style = MaterialTheme.typography.titleMedium)
            CollapsibleLinks(urls = listOf(urls.first()), showBackground = false)
        }
    }
}

/**
 * Generates a human-readable label for a given URL, prioritizing known social media domains.
 */
private fun getLabelForUrl(url: String): String {
    val lower = url.lowercase()
    return when {
        lower.contains("twitter.com") || lower.contains("x.com") -> "Twitter/X"
        lower.contains("instagram.com") -> "Instagram"
        lower.contains("onlyfans.com") -> "OnlyFans"
        lower.contains("pornhub.com") -> "Pornhub"
        lower.contains("reddit.com") -> "Reddit"
        lower.contains("tiktok.com") -> "TikTok"
        else -> try {
            URI(url).host?.replace("www.", "") ?: url
        } catch (e: Exception) {
            url
        }
    }
}
