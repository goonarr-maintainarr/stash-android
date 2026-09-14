package goonarr.stash.features.studios.list

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import goonarr.stash.features.components.rememberShimmerBrush

@Composable
fun StudioCompactRowShimmer(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Image placeholder (~40%)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )

            // Metadata placeholder (~60%)
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(top = 12.dp, end = 12.dp, bottom = 12.dp, start = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Name placeholder (1 line max)
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description placeholder (2 lines max)
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.8f)
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

                // Bottom row: Stats + Icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stats placeholders
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Scene count placeholder
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(brush)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .width(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(brush)
                            )
                        }

                        // Performer count placeholder
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(brush)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .width(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(brush)
                            )
                        }
                    }

                    // Rating/Favorite placeholders
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(brush)
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(brush)
                        )
                    }
                }
            }
        }
    }
}
