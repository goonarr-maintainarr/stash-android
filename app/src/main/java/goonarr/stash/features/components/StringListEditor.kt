package goonarr.stash.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme

/**
 * A component to edit a list of strings (e.g. URLs).
 * Allows adding, removing, and editing individual items.
 *
 * @param values The current list of strings.
 * @param onValueChange Callback with the updated list.
 * @param label Label for the text fields.
 * @param addItemLabel Label for the add button.
 * @param modifier Optional modifier.
 */
@Composable
fun StringListEditor(
    values: List<String>,
    onValueChange: (List<String>) -> Unit,
    label: String,
    addItemLabel: String = "Add Item",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StringListTextField(
                    value = item,
                    onValueChange = { newValue ->
                        val newList = values.toMutableList()
                        newList[index] = newValue
                        onValueChange(newList)
                    },
                    label = "$label ${index + 1}",
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val newList = values.toMutableList()
                        newList.removeAt(index)
                        onValueChange(newList)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Button(
            onClick = {
                val newList = values.toMutableList()
                newList.add("")
                onValueChange(newList)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = StashTheme.colors.primary.copy(alpha = 0.1f),
                contentColor = StashTheme.colors.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(addItemLabel)
        }
    }
}

@Composable
private fun StringListTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val dynamicColors = StashTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = dynamicColors.primary.copy(alpha = 0.2f),
            unfocusedContainerColor = dynamicColors.primary.copy(alpha = 0.1f),
            focusedBorderColor = dynamicColors.primary,
            unfocusedBorderColor = dynamicColors.primary.copy(alpha = 0.5f),
            focusedLabelColor = dynamicColors.primary,
            unfocusedLabelColor = dynamicColors.primary.copy(alpha = 0.5f),
            cursorColor = dynamicColors.primary
        )
    )
}
