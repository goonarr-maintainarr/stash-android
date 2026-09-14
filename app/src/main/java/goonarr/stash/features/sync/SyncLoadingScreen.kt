package goonarr.stash.features.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SyncLoadingScreen(
    viewModel: SyncViewModel = hiltViewModel(),
    onSyncComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Check for completion
    if (uiState.isComplete) {
        onSyncComplete()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Syncing your Stash...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                SyncCategoryRow(
                    name = "Studios",
                    progress = uiState.studios
                )

                SyncCategoryRow(
                    name = "Tags",
                    progress = uiState.tags
                )

                SyncCategoryRow(
                    name = "Performers",
                    progress = uiState.performers
                )

                SyncCategoryRow(
                    name = "Scenes",
                    progress = uiState.scenes
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Overall progress text
                val totalItems = uiState.studios.total + uiState.tags.total +
                    uiState.performers.total + uiState.scenes.total
                val syncedItems = uiState.studios.synced + uiState.tags.synced +
                    uiState.performers.synced + uiState.scenes.synced

                if (totalItems > 0) {
                    Text(
                        text = "$syncedItems / $totalItems items synced",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncCategoryRow(
    name: String,
    progress: SyncCategoryProgress
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progress,
        label = "progress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = progress.isComplete,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Complete",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = !progress.isComplete && progress.total > 0,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (progress.total > 0) {
                Text(
                    text = "${progress.synced} / ${progress.total}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = if (progress.isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SyncLoadingScreenPreview() {
    goonarr.stash.StashTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Syncing your Stash...",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SyncCategoryRow(
                        name = "Studios",
                        progress = SyncCategoryProgress(total = 120, synced = 120, isComplete = true)
                    )

                    SyncCategoryRow(
                        name = "Tags",
                        progress = SyncCategoryProgress(total = 85, synced = 85, isComplete = true)
                    )

                    SyncCategoryRow(
                        name = "Performers",
                        progress = SyncCategoryProgress(total = 350, synced = 210, isComplete = false)
                    )

                    SyncCategoryRow(
                        name = "Scenes",
                        progress = SyncCategoryProgress(total = 0, synced = 0, isComplete = false)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "415 / 555 items synced",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SyncCategoryRowPreview() {
    goonarr.stash.StashTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SyncCategoryRow(
                    name = "Studios",
                    progress = SyncCategoryProgress(total = 120, synced = 120, isComplete = true)
                )
                SyncCategoryRow(
                    name = "Performers",
                    progress = SyncCategoryProgress(total = 350, synced = 175, isComplete = false)
                )
                SyncCategoryRow(
                    name = "Scenes",
                    progress = SyncCategoryProgress(total = 0, synced = 0, isComplete = false)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Just Started",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SyncLoadingScreenStartingPreview() {
    goonarr.stash.StashTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Syncing your Stash...",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SyncCategoryRow(
                        name = "Studios",
                        progress = SyncCategoryProgress(total = 120, synced = 45, isComplete = false)
                    )

                    SyncCategoryRow(
                        name = "Tags",
                        progress = SyncCategoryProgress(total = 0, synced = 0, isComplete = false)
                    )

                    SyncCategoryRow(
                        name = "Performers",
                        progress = SyncCategoryProgress(total = 0, synced = 0, isComplete = false)
                    )

                    SyncCategoryRow(
                        name = "Scenes",
                        progress = SyncCategoryProgress(total = 0, synced = 0, isComplete = false)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "45 / 120 items synced",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
