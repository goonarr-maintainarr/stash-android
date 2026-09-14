package goonarr.stash.features.scenes.detail.components.description

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneDescriptionSectionPreview() {
    StashTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Short Description (No Truncation)", style = MaterialTheme.typography.titleMedium)
            SceneDescriptionSection(description = "This is a short description.")

            Text("Long Description (Truncated)", style = MaterialTheme.typography.titleMedium)
            SceneDescriptionSection(
                description = "This is a much longer description that should definitely exceed three " +
                    "lines of text in the preview so we can see the 'Read more' button and test the truncation logic correctly. " +
                    "Let's add even more text just to be sure it overflows. Specifically, we want to see if the button appears " +
                    "and if clicking it works as expected."
            )
        }
    }
}
