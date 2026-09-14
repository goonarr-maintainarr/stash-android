package goonarr.stash.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme

@Preview
@Composable
fun RatingBarPreviews() {
    StashTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Interactive Rating Bar", style = MaterialTheme.typography.titleMedium)
                var rating by remember { mutableIntStateOf(3) }
                RatingBar(
                    rating = rating,
                    onRatingChanged = { rating = it }
                )
                Text("Current Rating: $rating")

                Spacer(modifier = Modifier.height(24.dp))

                Text("Read-Only: 0 Stars", style = MaterialTheme.typography.titleMedium)
                RatingBar(rating = 0, onRatingChanged = null)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Read-Only: 5 Stars", style = MaterialTheme.typography.titleMedium)
                RatingBar(rating = 5, onRatingChanged = null)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Small Setup (16.dp)", style = MaterialTheme.typography.titleMedium)
                RatingBar(
                    rating = 4,
                    onRatingChanged = null,
                    starSize = 16.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Dark Background", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(8.dp)
                ) {
                    RatingBar(
                        rating = 2,
                        onRatingChanged = null
                    )
                }
            }
        }
    }
}
