package goonarr.stash.features.performers.edit.previews

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.features.components.SharedEditScaffold
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.features.performers.edit.EditPerformerContent
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

@Composable
fun EditPerformerPreviewContainer(content: @Composable () -> Unit) {
    StashTheme(darkTheme = true) {
        val dynamicColors = rememberSceneColors(StashBlue) // Default/mock colors
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            content()
        }
    }
}

@Preview(
    name = "Edit Performer - Success",
    heightDp = 2000,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditPerformerScreenSuccessPreview() {
    EditPerformerPreviewContainer {
        SharedEditScaffold(
            title = "Edit Performer",
            dynamicColors = LocalStashDynamicColors.current,
            onBackClick = {},
            onSave = {},
            onRetry = {},
            uiState = SharedEditUiState.Success
        ) {
            EditPerformerContent(
                name = MockData.fullPerformer.name ?: "",
                onNameChange = {},
                disambiguation = MockData.fullPerformer.disambiguation ?: "",
                onDisambiguationChange = {},
                gender = MockData.fullPerformer.gender ?: "",
                onGenderChange = {},
                birthdate = MockData.fullPerformer.birthdate ?: "",
                onBirthdateChange = {},
                deathDate = MockData.fullPerformer.deathDate ?: "",
                onDeathDateChange = {},
                country = MockData.fullPerformer.country ?: "",
                onCountryChange = {},
                ethnicity = MockData.fullPerformer.ethnicity ?: "",
                onEthnicityChange = {},
                heightCm = MockData.fullPerformer.heightCm?.toString() ?: "",
                onHeightCmChange = {},
                weight = MockData.fullPerformer.weight?.toString() ?: "",
                onWeightChange = {},
                measurements = MockData.fullPerformer.measurements ?: "",
                onMeasurementsChange = {},
                eyeColor = MockData.fullPerformer.eyeColor ?: "",
                onEyeColorChange = {},
                hairColor = MockData.fullPerformer.hairColor ?: "",
                onHairColorChange = {},
                fakeTits = MockData.fullPerformer.fakeTits ?: "",
                onFakeTitsChange = {},
                penisLength = MockData.fullPerformer.penisLength?.toString() ?: "",
                onPenisLengthChange = {},
                circumcised = MockData.fullPerformer.circumcised ?: "",
                onCircumcisedChange = {},
                careerLength = MockData.fullPerformer.careerLength ?: "",
                onCareerLengthChange = {},
                tattoos = MockData.fullPerformer.tattoos ?: "",
                onTattoosChange = {},
                piercings = MockData.fullPerformer.piercings ?: "",
                onPiercingsChange = {},
                details = MockData.fullPerformer.details ?: "",
                onDetailsChange = {},
                rating = MockData.fullPerformer.rating100,
                onRatingChange = {},
                favorite = MockData.fullPerformer.favorite == true,
                onFavoriteChange = {},
                aliases = MockData.fullPerformer.aliasList ?: emptyList(),
                onAliasesChange = {},
                urls = MockData.fullPerformer.urls ?: emptyList(),
                onUrlsChange = {},
                imageUrl = MockData.fullPerformer.imagePath,
                tags = MockData.tags.take(3),
                onRemoveTag = {},
                tagSearchText = "",
                onTagSearchTextChange = {},
                tagSearchResults = emptyList(),
                onAddTag = {},
                isSearchingTags = false
            )
        }
    }
}
