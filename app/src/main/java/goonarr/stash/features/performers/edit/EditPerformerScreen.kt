package goonarr.stash.features.performers.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.SharedEditBackground
import goonarr.stash.features.components.SharedEditScaffold
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.features.components.StaggeredEditList
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.HeroAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPerformerScreen(
    performerId: String,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditPerformerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var heroColor by remember { mutableStateOf<Color?>(null) }
    val imageUrl by viewModel.imageUrl.collectAsState()

    LaunchedEffect(performerId) {
        viewModel.loadPerformer(performerId)
    }

    LaunchedEffect(uiState) {
        if (uiState is SharedEditUiState.Saved) {
            onSaveSuccess()
        }
    }

    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            HeroAccentColor.extract(context, imageUrl)?.let { heroColor = it }
        }
    }

    val dynamicColors = rememberSceneColors(heroColor)

    Box(modifier = Modifier.fillMaxSize()) {
        SharedEditBackground(imageUrl)

        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            SharedEditScaffold(
                title = "Edit Performer",
                onBackClick = onBackClick,
                onSave = { viewModel.save() },
                onRetry = { viewModel.clearError() },
                uiState = uiState,
                dynamicColors = dynamicColors
            ) {
                // Content only loaded when not error/loading (handled by SharedEditScaffold but we need to supply content)
                if (uiState !is SharedEditUiState.Loading && uiState !is SharedEditUiState.Error) {
                    val name by viewModel.name.collectAsState()
                    val disambiguation by viewModel.disambiguation.collectAsState()
                    val gender by viewModel.gender.collectAsState()
                    val birthdate by viewModel.birthdate.collectAsState()
                    val deathDate by viewModel.deathDate.collectAsState()
                    val country by viewModel.country.collectAsState()
                    val ethnicity by viewModel.ethnicity.collectAsState()
                    val heightCm by viewModel.heightCm.collectAsState()
                    val weight by viewModel.weight.collectAsState()
                    val measurements by viewModel.measurements.collectAsState()
                    val eyeColor by viewModel.eyeColor.collectAsState()
                    val hairColor by viewModel.hairColor.collectAsState()
                    val fakeTits by viewModel.fakeTits.collectAsState()
                    val penisLength by viewModel.penisLength.collectAsState()
                    val circumcised by viewModel.circumcised.collectAsState()
                    val careerLength by viewModel.careerLength.collectAsState()
                    val tattoos by viewModel.tattoos.collectAsState()
                    val piercings by viewModel.piercings.collectAsState()
                    val details by viewModel.details.collectAsState()
                    val rating by viewModel.rating.collectAsState()
                    val favorite by viewModel.favorite.collectAsState()
                    val aliases by viewModel.aliases.collectAsState()
                    val urls by viewModel.urls.collectAsState()
                    val imageUrl by viewModel.imageUrl.collectAsState()
                    val tags by viewModel.tags.collectAsState()
                    val tagSearchText by viewModel.tagSearchText.collectAsState()
                    val tagSearchResults by viewModel.tagSearchResults.collectAsState()
                    val isSearchingTags by viewModel.isSearchingTags.collectAsState()

                    EditPerformerContent(
                        name = name,
                        onNameChange = { viewModel.name.value = it },
                        disambiguation = disambiguation,
                        onDisambiguationChange = { viewModel.disambiguation.value = it },
                        gender = gender,
                        onGenderChange = { viewModel.gender.value = it },
                        birthdate = birthdate,
                        onBirthdateChange = { viewModel.birthdate.value = it },
                        deathDate = deathDate,
                        onDeathDateChange = { viewModel.deathDate.value = it },
                        country = country,
                        onCountryChange = { viewModel.country.value = it },
                        ethnicity = ethnicity,
                        onEthnicityChange = { viewModel.ethnicity.value = it },
                        heightCm = heightCm,
                        onHeightCmChange = { viewModel.heightCm.value = it },
                        weight = weight,
                        onWeightChange = { viewModel.weight.value = it },
                        measurements = measurements,
                        onMeasurementsChange = { viewModel.measurements.value = it },
                        eyeColor = eyeColor,
                        onEyeColorChange = { viewModel.eyeColor.value = it },
                        hairColor = hairColor,
                        onHairColorChange = { viewModel.hairColor.value = it },
                        fakeTits = fakeTits,
                        onFakeTitsChange = { viewModel.fakeTits.value = it },
                        penisLength = penisLength,
                        onPenisLengthChange = { viewModel.penisLength.value = it },
                        circumcised = circumcised,
                        onCircumcisedChange = { viewModel.circumcised.value = it },
                        careerLength = careerLength,
                        onCareerLengthChange = { viewModel.careerLength.value = it },
                        tattoos = tattoos,
                        onTattoosChange = { viewModel.tattoos.value = it },
                        piercings = piercings,
                        onPiercingsChange = { viewModel.piercings.value = it },
                        details = details,
                        onDetailsChange = { viewModel.details.value = it },
                        rating = rating,
                        onRatingChange = { viewModel.rating.value = it },
                        favorite = favorite,
                        onFavoriteChange = { viewModel.favorite.value = it },
                        aliases = aliases,
                        onAliasesChange = { viewModel.aliases.value = it },
                        urls = urls,
                        onUrlsChange = { viewModel.urls.value = it },
                        imageUrl = imageUrl,
                        tags = tags,
                        onRemoveTag = { viewModel.removeTag(it) },
                        tagSearchText = tagSearchText,
                        onTagSearchTextChange = { viewModel.tagSearchText.value = it },
                        tagSearchResults = tagSearchResults,
                        onAddTag = { viewModel.addTag(it) },
                        isSearchingTags = isSearchingTags
                    )
                }
            }
        }
    }
}

@Composable
internal fun EditPerformerContent(
    name: String,
    onNameChange: (String) -> Unit,
    disambiguation: String,
    onDisambiguationChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    birthdate: String,
    onBirthdateChange: (String) -> Unit,
    deathDate: String,
    onDeathDateChange: (String) -> Unit,
    country: String,
    onCountryChange: (String) -> Unit,
    ethnicity: String,
    onEthnicityChange: (String) -> Unit,
    heightCm: String,
    onHeightCmChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    measurements: String,
    onMeasurementsChange: (String) -> Unit,
    eyeColor: String,
    onEyeColorChange: (String) -> Unit,
    hairColor: String,
    onHairColorChange: (String) -> Unit,
    fakeTits: String,
    onFakeTitsChange: (String) -> Unit,
    penisLength: String,
    onPenisLengthChange: (String) -> Unit,
    circumcised: String,
    onCircumcisedChange: (String) -> Unit,
    careerLength: String,
    onCareerLengthChange: (String) -> Unit,
    tattoos: String,
    onTattoosChange: (String) -> Unit,
    piercings: String,
    onPiercingsChange: (String) -> Unit,
    details: String,
    onDetailsChange: (String) -> Unit,
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    favorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    aliases: List<String>,
    onAliasesChange: (List<String>) -> Unit,
    urls: List<String>,
    onUrlsChange: (List<String>) -> Unit,
    imageUrl: String?,
    tags: List<Tag>,
    onRemoveTag: (String) -> Unit,
    tagSearchText: String,
    onTagSearchTextChange: (String) -> Unit,
    tagSearchResults: List<Tag>,
    onAddTag: (Tag) -> Unit,
    isSearchingTags: Boolean
) {
    val scrollState = rememberScrollState()

    StaggeredEditList(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(StashTokens.Spacing.ContentHorizontal),
        children = listOf(
            {
                IdentitySection(
                    name = name,
                    onNameChange = onNameChange,
                    disambiguation = disambiguation,
                    onDisambiguationChange = onDisambiguationChange,
                    gender = gender,
                    onGenderChange = onGenderChange,
                    birthdate = birthdate,
                    onBirthdateChange = onBirthdateChange,
                    country = country,
                    onCountryChange = onCountryChange,
                    ethnicity = ethnicity,
                    onEthnicityChange = onEthnicityChange,
                    imageUrl = imageUrl
                )
            },
            {
                RatingSection(
                    rating = rating,
                    onRatingChange = onRatingChange,
                    favorite = favorite,
                    onFavoriteChange = onFavoriteChange
                )
            },
            {
                PhysicalSection(
                    heightCm = heightCm,
                    onHeightCmChange = onHeightCmChange,
                    weight = weight,
                    onWeightChange = onWeightChange,
                    measurements = measurements,
                    onMeasurementsChange = onMeasurementsChange,
                    eyeColor = eyeColor,
                    onEyeColorChange = onEyeColorChange,
                    hairColor = hairColor,
                    onHairColorChange = onHairColorChange,
                    fakeTits = fakeTits,
                    onFakeTitsChange = onFakeTitsChange,
                    penisLength = penisLength,
                    onPenisLengthChange = onPenisLengthChange,
                    circumcised = circumcised,
                    onCircumcisedChange = onCircumcisedChange
                )
            },
            {
                CareerSection(
                    careerLength = careerLength,
                    onCareerLengthChange = onCareerLengthChange,
                    aliases = aliases,
                    onAliasesChange = onAliasesChange,
                    urls = urls,
                    onUrlsChange = onUrlsChange
                )
            },
            {
                BodyModificationsSection(
                    tattoos = tattoos,
                    onTattoosChange = onTattoosChange,
                    piercings = piercings,
                    onPiercingsChange = onPiercingsChange
                )
            },
            {
                BiographySection(
                    details = details,
                    onDetailsChange = onDetailsChange
                )
            },
            {
                TagsSection(
                    tags = tags,
                    onRemoveTag = onRemoveTag,
                    tagSearchText = tagSearchText,
                    onTagSearchTextChange = onTagSearchTextChange,
                    tagSearchResults = tagSearchResults,
                    onAddTag = onAddTag,
                    isSearchingTags = isSearchingTags
                )
            },
            {
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(80.dp))
            }
        )
    )
}
