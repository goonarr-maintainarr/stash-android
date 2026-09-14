package goonarr.stash.features.scenes.scenecard.shimmers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import goonarr.stash.features.components.rememberShimmerBrush

@Composable
fun SceneCompactRowShimmer(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
                    .background(brush)
            )
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Title placeholder (2 lines)
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.7f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Studio • Date placeholder
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.5f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description placeholder (2 lines)
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.6f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }

                // Bottom: Performers + Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(80.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}
