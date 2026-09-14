package goonarr.stash.features.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isSearching: Boolean,
    color: Color = StashTheme.colors.primary,
    modifier: Modifier = Modifier
) {
    val dynamicColors = StashTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = color.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = dynamicColors.tertiary
            )
        },
        trailingIcon = {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = color.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Invisible spacer to keep centering balanced
                Spacer(modifier = Modifier.size(24.dp))
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(StashTokens.Radius.Card),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = color.copy(alpha = 0.2f),
            unfocusedContainerColor = color.copy(alpha = 0.1f),
            focusedBorderColor = color.copy(alpha = 0.8f),
            unfocusedBorderColor = color.copy(alpha = 0.3f),
            focusedLabelColor = color,
            unfocusedLabelColor = color.copy(alpha = 0.5f),
            cursorColor = color
        )
    )
}
