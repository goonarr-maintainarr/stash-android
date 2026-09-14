package goonarr.stash.features.performers.scrape.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme

@Composable
private fun SectionCardPreviewContainer(
    content: @Composable () -> Unit
) {
    StashTheme(darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Preview(name = "Section Card", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SectionCardPreview() {
    SectionCardPreviewContainer {
        SectionCard(title = "Metadata") {
            Text(
                text = "This is some content inside a SectionCard. It usually contains multiple SelectionRow items.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
