package goonarr.stash.features.scenes.scenecard.shimmers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
fun SceneCardShimmer(
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    isCarousel: Boolean = false
) {
    val brush = rememberShimmerBrush()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(brush)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                // Title shimmer
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(if (isCompact || isCarousel) 0.7f else 0.85f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Studio/Metadata shimmer (Studio • Date)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Studio shimmer
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(80.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Dot
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Date shimmer
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }

                if (!isCompact) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Description shimmer
                    if (isCarousel) {
                        // 1 line for carousel
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .fillMaxWidth(0.9f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(brush)
                        )
                    } else {
                        // 3 lines for full card
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .height(14.dp)
                                    .fillMaxWidth(if (index == 2) 0.6f else 1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(brush)
                            )
                            if (index < 2) Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Performers shimmer (just one name)
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(120.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}
