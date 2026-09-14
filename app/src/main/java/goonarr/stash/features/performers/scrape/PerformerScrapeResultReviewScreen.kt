package goonarr.stash.features.performers.scrape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.features.performers.scrape.components.ImageCarouselSection
import goonarr.stash.features.performers.scrape.components.SectionCard
import goonarr.stash.features.performers.scrape.components.SelectionRow
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformerScrapeResultReviewScreen(
    scrapedPerformer: ScrapedPerformer,
    currentPerformer: Performer?,
    selection: PerformerScrapeSelection,
    onSelectionChanged: (PerformerScrapeSelection) -> Unit,
    isLoading: Boolean,
    onApply: () -> Unit,
    onBack: () -> Unit,
    remoteSiteEndpoint: String? = null,
    scraperName: String? = null
) {
    val colors = StashTheme.colors
    val haptic = LocalStashHapticFeedback.current
    val currentImageUrl = currentPerformer?.imagePath
    val scrapedImages = scrapedPerformer.images ?: emptyList()

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
                // Image Carousel Section
                if (scrapedImages.isNotEmpty()) {
                    ImageCarouselSection(
                        currentImageUrl = currentImageUrl,
                        scrapedImages = scrapedImages,
                        selectedImageIndex = selection.selectedImageIndex,
                        useImage = selection.useImage,
                        onImageSelected = { index ->
                            onSelectionChanged(selection.copy(selectedImageIndex = index, useImage = true))
                        },
                        onUseImageChanged = { use ->
                            onSelectionChanged(selection.copy(useImage = use))
                        }
                    )
                }

                // Basic Info Section
                // Basic Info Section
                val showName = scrapedPerformer.name != currentPerformer?.name
                val showDisambiguation = !scrapedPerformer.disambiguation.isNullOrBlank()
                val showGender = !scrapedPerformer.gender.isNullOrBlank() && scrapedPerformer.gender != currentPerformer?.gender

                if (showName || showDisambiguation || showGender) {
                    SectionCard(title = "Basic Info") {
                        if (showName) {
                            SelectionRow(
                                label = "Name",
                                currentValue = currentPerformer?.name,
                                scrapedValue = scrapedPerformer.name,
                                isSelected = selection.useName,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useName = it)) }
                            )
                        }
                        if (showDisambiguation) {
                            SelectionRow(
                                label = "Disambiguation",
                                currentValue = null,
                                scrapedValue = scrapedPerformer.disambiguation,
                                isSelected = selection.useDisambiguation,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useDisambiguation = it)) }
                            )
                        }
                        if (showGender) {
                            SelectionRow(
                                label = "Gender",
                                currentValue = currentPerformer?.gender,
                                scrapedValue = scrapedPerformer.gender,
                                isSelected = selection.useGender,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useGender = it)) }
                            )
                        }
                    }
                }

                // Dates Section
                // Dates Section
                val showBirthdate = !scrapedPerformer.birthdate.isNullOrBlank() && scrapedPerformer.birthdate != currentPerformer?.birthdate
                val showDeathDate = !scrapedPerformer.deathDate.isNullOrBlank() && scrapedPerformer.deathDate != currentPerformer?.deathDate

                if (showBirthdate || showDeathDate) {
                    SectionCard(title = "Dates") {
                        if (showBirthdate) {
                            SelectionRow(
                                label = "Birthdate",
                                currentValue = currentPerformer?.birthdate,
                                scrapedValue = scrapedPerformer.birthdate,
                                isSelected = selection.useBirthdate,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useBirthdate = it)) }
                            )
                        }
                        if (showDeathDate) {
                            SelectionRow(
                                label = "Death Date",
                                currentValue = currentPerformer?.deathDate,
                                scrapedValue = scrapedPerformer.deathDate,
                                isSelected = selection.useDeathDate,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useDeathDate = it)) }
                            )
                        }
                    }
                }

                // Location Section
                // Location Section
                val showCountry = !scrapedPerformer.country.isNullOrBlank() && scrapedPerformer.country != currentPerformer?.country
                val showEthnicity = !scrapedPerformer.ethnicity.isNullOrBlank() && scrapedPerformer.ethnicity != currentPerformer?.ethnicity

                if (showCountry || showEthnicity) {
                    SectionCard(title = "Location & Ethnicity") {
                        if (showCountry) {
                            SelectionRow(
                                label = "Country",
                                currentValue = currentPerformer?.country,
                                scrapedValue = scrapedPerformer.country,
                                isSelected = selection.useCountry,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useCountry = it)) }
                            )
                        }
                        if (showEthnicity) {
                            SelectionRow(
                                label = "Ethnicity",
                                currentValue = currentPerformer?.ethnicity,
                                scrapedValue = scrapedPerformer.ethnicity,
                                isSelected = selection.useEthnicity,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useEthnicity = it)) }
                            )
                        }
                    }
                }

                // Physical Attributes Section
                // Physical Attributes Section
                val showHeight = !scrapedPerformer.height
                    .isNullOrBlank() && scrapedPerformer.height != currentPerformer?.heightCm?.toString()
                val showWeight = !scrapedPerformer.weight
                    .isNullOrBlank() && scrapedPerformer.weight != currentPerformer?.weight?.toString()
                val showMeasurements = !scrapedPerformer.measurements
                    .isNullOrBlank() && scrapedPerformer.measurements != currentPerformer?.measurements
                val showEyeColor = !scrapedPerformer.eyeColor
                    .isNullOrBlank() && scrapedPerformer.eyeColor != currentPerformer?.eyeColor
                val showHairColor = !scrapedPerformer.hairColor
                    .isNullOrBlank() && scrapedPerformer.hairColor != currentPerformer?.hairColor

                if (showHeight || showWeight || showMeasurements || showEyeColor || showHairColor) {
                    SectionCard(title = "Physical Attributes") {
                        if (showHeight) {
                            SelectionRow(
                                label = "Height",
                                currentValue = currentPerformer?.heightCm?.toString(),
                                scrapedValue = scrapedPerformer.height,
                                isSelected = selection.useHeight,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useHeight = it)) }
                            )
                        }
                        if (showWeight) {
                            SelectionRow(
                                label = "Weight",
                                currentValue = currentPerformer?.weight?.toString(),
                                scrapedValue = scrapedPerformer.weight,
                                isSelected = selection.useWeight,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useWeight = it)) }
                            )
                        }
                        if (showMeasurements) {
                            SelectionRow(
                                label = "Measurements",
                                currentValue = currentPerformer?.measurements,
                                scrapedValue = scrapedPerformer.measurements,
                                isSelected = selection.useMeasurements,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useMeasurements = it)) }
                            )
                        }
                        if (showEyeColor) {
                            SelectionRow(
                                label = "Eye Color",
                                currentValue = currentPerformer?.eyeColor,
                                scrapedValue = scrapedPerformer.eyeColor,
                                isSelected = selection.useEyeColor,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useEyeColor = it)) }
                            )
                        }
                        if (showHairColor) {
                            SelectionRow(
                                label = "Hair Color",
                                currentValue = currentPerformer?.hairColor,
                                scrapedValue = scrapedPerformer.hairColor,
                                isSelected = selection.useHairColor,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useHairColor = it)) }
                            )
                        }
                    }
                }

                // Body Modifications Section
                // Body Modifications Section
                val showFakeTits = !scrapedPerformer.fakeTits
                    .isNullOrBlank() && scrapedPerformer.fakeTits != currentPerformer?.fakeTits
                val showPenisLength = !scrapedPerformer.penisLength
                    .isNullOrBlank() && scrapedPerformer.penisLength != currentPerformer?.penisLength?.toString()
                val showCircumcised = !scrapedPerformer.circumcised
                    .isNullOrBlank() && scrapedPerformer.circumcised != currentPerformer?.circumcised
                val showTattoos = !scrapedPerformer.tattoos
                    .isNullOrBlank() && scrapedPerformer.tattoos != currentPerformer?.tattoos
                val showPiercings = !scrapedPerformer.piercings
                    .isNullOrBlank() && scrapedPerformer.piercings != currentPerformer?.piercings

                if (showFakeTits || showPenisLength || showCircumcised || showTattoos || showPiercings) {
                    SectionCard(title = "Body Modifications") {
                        if (showFakeTits) {
                            SelectionRow(
                                label = "Fake Tits",
                                currentValue = currentPerformer?.fakeTits,
                                scrapedValue = scrapedPerformer.fakeTits,
                                isSelected = selection.useFakeTits,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useFakeTits = it)) }
                            )
                        }
                        if (showPenisLength) {
                            SelectionRow(
                                label = "Penis Length",
                                currentValue = currentPerformer?.penisLength?.toString(),
                                scrapedValue = scrapedPerformer.penisLength,
                                isSelected = selection.usePenisLength,
                                onSelectionChanged = { onSelectionChanged(selection.copy(usePenisLength = it)) }
                            )
                        }
                        if (showCircumcised) {
                            SelectionRow(
                                label = "Circumcised",
                                currentValue = currentPerformer?.circumcised,
                                scrapedValue = scrapedPerformer.circumcised,
                                isSelected = selection.useCircumcised,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useCircumcised = it)) }
                            )
                        }
                        if (showTattoos) {
                            SelectionRow(
                                label = "Tattoos",
                                currentValue = currentPerformer?.tattoos,
                                scrapedValue = scrapedPerformer.tattoos,
                                isSelected = selection.useTattoos,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useTattoos = it)) }
                            )
                        }
                        if (showPiercings) {
                            SelectionRow(
                                label = "Piercings",
                                currentValue = currentPerformer?.piercings,
                                scrapedValue = scrapedPerformer.piercings,
                                isSelected = selection.usePiercings,
                                onSelectionChanged = { onSelectionChanged(selection.copy(usePiercings = it)) }
                            )
                        }
                    }
                }

                // Career Section
                val showCareerLength = !scrapedPerformer.careerLength
                    .isNullOrBlank() && scrapedPerformer.careerLength != currentPerformer?.careerLength

                // Parse scraped aliases and compare as sets to handle different ordering
                val scrapedAliasesList = scrapedPerformer.aliases
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.toSet() ?: emptySet()
                val currentAliasesList = currentPerformer?.aliasList?.toSet() ?: emptySet()
                val showAliases = scrapedAliasesList.isNotEmpty() && scrapedAliasesList != currentAliasesList

                if (showCareerLength || showAliases) {
                    SectionCard(title = "Career") {
                        if (showCareerLength) {
                            SelectionRow(
                                label = "Career Length",
                                currentValue = currentPerformer?.careerLength,
                                scrapedValue = scrapedPerformer.careerLength,
                                isSelected = selection.useCareerLength,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useCareerLength = it)) }
                            )
                        }
                        if (showAliases) {
                            SelectionRow(
                                label = "Aliases",
                                currentValue = currentPerformer?.aliasList?.joinToString(", "),
                                scrapedValue = scrapedPerformer.aliases,
                                isSelected = selection.useAliases,
                                onSelectionChanged = { onSelectionChanged(selection.copy(useAliases = it)) }
                            )
                        }
                    }
                }

                // Details Section
                if (!scrapedPerformer.details.isNullOrBlank() && scrapedPerformer.details != currentPerformer?.details) {
                    SectionCard(title = "Details") {
                        SelectionRow(
                            label = "Bio/Details",
                            currentValue = currentPerformer?.details?.take(100)?.let { "$it..." },
                            scrapedValue = scrapedPerformer.details.take(100) + "...",
                            isSelected = selection.useDetails,
                            onSelectionChanged = { onSelectionChanged(selection.copy(useDetails = it)) }
                        )
                    }
                }

                // URLs Section
                if (selection.urls.isNotEmpty()) {
                    SectionCard(title = "URLs") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "SCRAPED URLS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Checkbox(
                                    checked = selection.useUrls,
                                    onCheckedChange = {
                                        haptic.perform(StashHapticFeedbackType.Light)
                                        onSelectionChanged(selection.copy(useUrls = it))
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = StashTheme.colors.primary)
                                )
                            }

                            selection.urls.forEachIndexed { index, url ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = url,
                                        onValueChange = { newUrl ->
                                            val newUrls = selection.urls.toMutableList()
                                            newUrls[index] = newUrl
                                            onSelectionChanged(selection.copy(urls = newUrls, useUrls = true))
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("URL") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = StashTheme.colors.primary,
                                            focusedLabelColor = StashTheme.colors.primary,
                                            cursorColor = StashTheme.colors.primary
                                        )
                                    )
                                    IconButton(
                                        onClick = {
                                            haptic.perform(StashHapticFeedbackType.Medium)
                                            val newUrls = selection.urls.toMutableList()
                                            newUrls.removeAt(index)
                                            onSelectionChanged(selection.copy(urls = newUrls))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove URL",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tags Section
                if (!scrapedPerformer.tags.isNullOrEmpty()) {
                    SectionCard(title = "Tags") {
                        SelectionRow(
                            label = "Tags",
                            currentValue = null,
                            scrapedValue = scrapedPerformer.tags.joinToString(", ") { it.name },
                            isSelected = selection.useTags,
                            onSelectionChanged = { onSelectionChanged(selection.copy(useTags = it)) }
                        )
                    }
                }

                // Stash ID Section
                val existingStashId = currentPerformer?.stashIds?.find { it.endpoint == remoteSiteEndpoint }?.stashId
                val showStashId = !scrapedPerformer.remoteSiteId.isNullOrBlank() && (scrapedPerformer.remoteSiteId != existingStashId)

                if (showStashId) {
                    SectionCard(title = "Stash ID") {
                        SelectionRow(
                            label = scraperName ?: "Remote Site ID",
                            currentValue = existingStashId,
                            scrapedValue = scrapedPerformer.remoteSiteId,
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
