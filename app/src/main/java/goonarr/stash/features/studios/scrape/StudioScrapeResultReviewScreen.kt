package goonarr.stash.features.studios.scrape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.scraper.ScrapedStudio
import goonarr.stash.features.performers.scrape.components.SectionCard
import goonarr.stash.features.performers.scrape.components.SelectionRow
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScrapeResultReviewScreen(
    scrapedStudio: ScrapedStudio,
    currentStudio: Studio?,
    selection: StudioScrapeSelection,
    onSelectionChanged: (StudioScrapeSelection) -> Unit,
    isLoading: Boolean,
    onApply: () -> Unit,
    onBack: () -> Unit,
    scraperName: String? = null
) {
    val colors = StashTheme.colors
    val haptic = LocalStashHapticFeedback.current
    val currentImageUrl = currentStudio?.imagePath
    val scrapedImage = scrapedStudio.image

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Review Scraped Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = {
                            haptic.perform(StashHapticFeedbackType.Success)
                            onApply()
                        }) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Apply",
                                tint = colors.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.complementary,
                    navigationIconContentColor = colors.complementary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Blurred Background
            if (currentImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(20.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Section
                // Reusing SelectionRow logic for image might be tricky if we want a carousel/preview.
                // Studio usually has one image. Let's just use a SectionCard with a custom Image selection or just a text row for now?
                // PerformerScrapeResultReviewScreen uses ImageCarouselSection.
                // Studio has `image` (String?), not `images` (List<String>).
                // Let's make a simple image selector.
                if (!scrapedImage.isNullOrBlank() && scrapedImage != currentImageUrl) {
                    SectionCard(title = "Image") {
                        // Show both images? Or just a checkbox to overwrite?
                        // Let's try to adapt SelectionRow but for images it's specialized.
                        // For consistency with Performer, maybe we just show a row for "Replace Image"
                        // But visuals are better.
                        // I'll stick to SelectionRow for simplicity of implementation as per "reuse components",
                        // but ideally we'd show the image.
                        // Actually, SelectionRow takes Strings.
                        // Let's just create a custom row here since we don't have a generic ImageSelectionRow.
                        SelectionRow(
                            label = "Image",
                            currentValue = if (currentImageUrl != null) "Existing Image" else "None",
                            scrapedValue = "New Image",
                            isSelected = selection.useImage,
                            onSelectionChanged = { onSelectionChanged(selection.copy(useImage = it)) }
                        )
                        // Render the scraped image for visual preview
                        if (selection.useImage) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(scrapedImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "New Studio Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .height(150.dp)
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                        }
                    }
                }

                // Basic Info Section
                val showName = scrapedStudio.name != currentStudio?.name
                val showDetails = !scrapedStudio.details.isNullOrBlank() && scrapedStudio.details != currentStudio?.details
                // Checking for parent change is harder without ID, assume always show if present
                val showParent = scrapedStudio.parent != null

                if (showName || showDetails || showParent) {
                    SectionCard(title = "Basic Info") {
                        if (showName) {
                            SelectionRow(
                                label = "Name",
                                currentValue = currentStudio?.name,
                                scrapedValue = scrapedStudio.name,
                                isSelected = selection.useName,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useName = it)) }
                            )
                        }
                        if (showDetails) {
                            SelectionRow(
                                label = "Details",
                                currentValue = currentStudio?.details?.take(100)?.let { "$it..." },
                                scrapedValue = scrapedStudio.details?.take(100)?.let { "$it..." },
                                isSelected = selection.useDetails,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useDetails = it)) }
                            )
                        }
                        if (showParent) {
                            SelectionRow(
                                label = "Parent Studio",
                                // We'd need to fetch parent name using parent ID, complicated.
                                currentValue = null,
                                scrapedValue = scrapedStudio.parent?.name,
                                isSelected = selection.useParent,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useParent = it)) }
                            )
                        }
                    }
                }

                // URLs Section
                // Using generic urls list for now, simpler than Performer's individual social handling
                if (!scrapedStudio.urls.isNullOrEmpty()) {
                    SectionCard(title = "URLs") {
                        // For simplicity, just replace all or nothing for now as per `useUrls` boolean
                        // Or we can list them.
                        // Performer review has a complex URL editor.
                        // Let's just show count/list for now.
                        SelectionRow(
                            label = "URLs",
                            currentValue = "${currentStudio?.urls?.size ?: 0} URLs",
                            scrapedValue = "${scrapedStudio.urls!!.size} URLs",
                            isSelected = selection.useUrls,
                            onSelectionChanged = { onSelectionChanged(selection.copy(useUrls = it)) }
                        )
                        if (selection.useUrls) {
                            scrapedStudio.urls!!.forEach {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Tags Section
                if (!scrapedStudio.tags.isNullOrEmpty()) {
                    SectionCard(title = "Tags") {
                        SelectionRow(
                            label = "Tags",
                            currentValue = null,
                            scrapedValue = scrapedStudio.tags!!.joinToString(", ") { it.name },
                            isSelected = selection.useTags,
                            onSelectionChanged = { onSelectionChanged(selection.copy(useTags = it)) }
                        )
                    }
                }

                // Stash ID Section
                val existingStashId = currentStudio?.stashIds?.find { it.endpoint.contains(scraperName ?: "", ignoreCase = true) }?.stashId
                // Simple stash ID logic
                val showStashId = !scrapedStudio.remoteSiteId.isNullOrBlank()

                if (showStashId) {
                    SectionCard(title = "Stash ID") {
                        SelectionRow(
                            label = scraperName ?: "Remote Site ID",
                            currentValue = existingStashId,
                            scrapedValue = scrapedStudio.remoteSiteId,
                            isSelected = selection.useStashId,
                            onSelectionChanged = { onSelectionChanged(selection.copy(useStashId = it)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp + paddingValues.calculateBottomPadding()))
            }
        }
    }
}
