package goonarr.stash.features.scenes.detail.components.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import goonarr.stash.features.components.rememberShimmerBrush

@Composable
fun SceneDetailShimmer(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Video Player Placeholder
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Header (Title, Date, Rating)
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )

            // Date & Studio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(height = 16.dp, width = 80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(height = 16.dp, width = 100.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(height = 16.dp, width = 80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }

            // Description lines
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tags Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(height = 14.dp, width = 60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(6) {
                    Box(
                        modifier = Modifier
                            .size(height = 32.dp, width = 80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brush)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Markers Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(height = 16.dp, width = 120.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            LazyRow(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(4) {
                    Column(
                        modifier = Modifier.width(160.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(brush)
                        )
                        Box(
                            modifier = Modifier
                                .size(height = 14.dp, width = 120.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                        Box(
                            modifier = Modifier
                                .size(height = 12.dp, width = 50.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Performers Section
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(height = 16.dp, width = 90.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            repeat(1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(brush)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // File Info Section
        ShimmerCard(brush, "FILE INFO", 4)

        Spacer(modifier = Modifier.height(20.dp))

        // History Sections
        ShimmerCard(brush, "O HISTORY", 2)
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerCard(brush, "PLAY HISTORY", 1)

        Spacer(modifier = Modifier.height(20.dp))

        // external IDs Section
        ShimmerCard(brush, "EXTERNAL IDS", 2)

        Spacer(modifier = Modifier.height(20.dp))

        // Details Section
        ShimmerCard(brush, "DETAILS", 5)

        Spacer(modifier = Modifier.height(40.dp))
    }
}
