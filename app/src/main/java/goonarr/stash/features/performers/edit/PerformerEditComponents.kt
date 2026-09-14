package goonarr.stash.features.performers.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.EditSection
import goonarr.stash.features.components.EditSwitchRow
import goonarr.stash.features.components.EditTextField
import goonarr.stash.features.components.OutlinedCard
import goonarr.stash.features.components.RatingBar
import goonarr.stash.features.components.StringListEditor
import goonarr.stash.features.components.TagPicker
import goonarr.stash.features.components.ThumbnailSection

enum class PerformerGender(val value: String, val displayName: String) {
    MALE("MALE", "Male"),
    FEMALE("FEMALE", "Female"),
    TRANSGENDER_MALE("TRANSGENDER_MALE", "Transgender Male"),
    TRANSGENDER_FEMALE("TRANSGENDER_FEMALE", "Transgender Female"),
    INTERSEX("INTERSEX", "Intersex"),
    NON_BINARY("NON_BINARY", "Non-Binary");

    companion object {
        fun fromValue(value: String?): PerformerGender? {
            return entries.find { it.value == value }
        }
    }
}

@Composable
internal fun IdentitySection(
    name: String,
    onNameChange: (String) -> Unit,
    disambiguation: String,
    onDisambiguationChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    birthdate: String,
    onBirthdateChange: (String) -> Unit,
    country: String,
    onCountryChange: (String) -> Unit,
    ethnicity: String,
    onEthnicityChange: (String) -> Unit,
    imageUrl: String?
) {
    EditSection(title = "IDENTITY", iconContent = { Icon(Icons.Default.Person, null) }, showDivider = false) {
        if (imageUrl != null) {
            ThumbnailSection(imageUrl = imageUrl)
        }

        EditTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Name"
        )
        EditTextField(
            value = disambiguation,
            onValueChange = onDisambiguationChange,
            label = "Disambiguation"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
            GenderDropdown(
                selectedGender = PerformerGender.fromValue(gender),
                onGenderSelected = { onGenderChange(it.value) },
                modifier = Modifier.weight(1f)
            )
            EditTextField(
                value = birthdate,
                onValueChange = onBirthdateChange,
                label = "Birthdate",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
            EditTextField(
                value = country,
                onValueChange = onCountryChange,
                label = "Country",
                modifier = Modifier.weight(1f)
            )
            EditTextField(
                value = ethnicity,
                onValueChange = onEthnicityChange,
                label = "Ethnicity",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun RatingSection(
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    favorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
        OutlinedCard {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Rating", style = MaterialTheme.typography.bodyLarge)
                RatingBar(
                    rating = (rating ?: 0) / 20,
                    onRatingChanged = { stars ->
                        onRatingChange(if (stars == 0) null else stars * 20)
                    }
                )
            }
        }

        EditSwitchRow(
            label = "Favorite",
            checked = favorite,
            onCheckedChange = onFavoriteChange
        )
    }
}

@Composable
internal fun PhysicalSection(
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
    onCircumcisedChange: (String) -> Unit
) {
    EditSection(title = "PHYSICAL") {
        Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
            Row(horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
                EditTextField(
                    value = heightCm,
                    onValueChange = onHeightCmChange,
                    label = "Height (cm)",
                    modifier = Modifier.weight(1f)
                )
                EditTextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    label = "Weight (kg)",
                    modifier = Modifier.weight(1f)
                )
            }

            EditTextField(
                value = measurements,
                onValueChange = onMeasurementsChange,
                label = "Measurements"
            )
            EditTextField(
                value = eyeColor,
                onValueChange = onEyeColorChange,
                label = "Eye Color"
            )
            EditTextField(
                value = hairColor,
                onValueChange = onHairColorChange,
                label = "Hair Color"
            )
            EditTextField(
                value = fakeTits,
                onValueChange = onFakeTitsChange,
                label = "Fake Tits"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
                EditTextField(
                    value = penisLength,
                    onValueChange = onPenisLengthChange,
                    label = "Penis Length",
                    modifier = Modifier.weight(1f)
                )
                EditTextField(
                    value = circumcised,
                    onValueChange = onCircumcisedChange,
                    label = "Circumcised",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun CareerSection(
    careerLength: String,
    onCareerLengthChange: (String) -> Unit,
    aliases: List<String>,
    onAliasesChange: (List<String>) -> Unit,
    urls: List<String>,
    onUrlsChange: (List<String>) -> Unit
) {
    EditSection(title = "CAREER") {
        EditTextField(
            value = careerLength,
            onValueChange = onCareerLengthChange,
            label = "Career Length"
        )

        EditSection(title = "ALIASES", showDivider = false) {
            StringListEditor(
                values = aliases,
                onValueChange = onAliasesChange,
                label = "Alias",
                addItemLabel = "Add Alias"
            )
        }

        EditSection(title = "URLS", showDivider = false) {
            StringListEditor(
                values = urls,
                onValueChange = onUrlsChange,
                label = "URL",
                addItemLabel = "Add URL"
            )
        }
    }
}

@Composable
internal fun BodyModificationsSection(
    tattoos: String,
    onTattoosChange: (String) -> Unit,
    piercings: String,
    onPiercingsChange: (String) -> Unit
) {
    EditSection(title = "BODY MODIFICATIONS") {
        EditTextField(
            value = tattoos,
            onValueChange = onTattoosChange,
            label = "Tattoos",
            singleLine = false,
            minLines = 2
        )
        EditTextField(
            value = piercings,
            onValueChange = onPiercingsChange,
            label = "Piercings",
            singleLine = false,
            minLines = 2
        )
    }
}

@Composable
internal fun BiographySection(details: String, onDetailsChange: (String) -> Unit) {
    EditSection(title = "BIOGRAPHY") {
        EditTextField(
            value = details,
            onValueChange = onDetailsChange,
            label = "Details",
            singleLine = false,
            minLines = 5
        )
    }
}

@Composable
internal fun TagsSection(
    tags: List<Tag>,
    onRemoveTag: (String) -> Unit,
    tagSearchText: String,
    onTagSearchTextChange: (String) -> Unit,
    tagSearchResults: List<Tag>,
    onAddTag: (Tag) -> Unit,
    isSearchingTags: Boolean
) {
    EditSection(title = "TAGS") {
        TagPicker(
            selectedTags = tags,
            onRemoveTag = onRemoveTag,
            searchText = tagSearchText,
            onSearchTextChange = onTagSearchTextChange,
            searchResults = tagSearchResults,
            onAddTag = onAddTag,
            isSearching = isSearchingTags,
            placeholder = "Add Tag..."
        )
    }
}

@Composable
fun GenderDropdown(
    selectedGender: PerformerGender?,
    onGenderSelected: (PerformerGender) -> Unit,
    modifier: Modifier = Modifier
) {
    val dynamicColors = StashTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    ) {
        OutlinedTextField(
            value = selectedGender?.displayName ?: "",
            onValueChange = {},
            label = { Text("Gender") },
            readOnly = true,
            enabled = false,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select gender"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(StashTokens.Radius.Card),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = dynamicColors.primary.copy(alpha = 0.2f),
                unfocusedContainerColor = dynamicColors.primary.copy(alpha = 0.1f),
                disabledContainerColor = dynamicColors.primary.copy(alpha = 0.1f),
                focusedBorderColor = dynamicColors.primary,
                unfocusedBorderColor = dynamicColors.primary.copy(alpha = 0.5f),
                disabledBorderColor = dynamicColors.primary.copy(alpha = 0.5f),
                focusedLabelColor = dynamicColors.primary,
                unfocusedLabelColor = dynamicColors.primary.copy(alpha = 0.5f),
                disabledLabelColor = dynamicColors.primary.copy(alpha = 0.5f),
                disabledTextColor = dynamicColors.complementary
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            PerformerGender.entries.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(gender.displayName) },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    }
                )
            }
        }
    }
}
