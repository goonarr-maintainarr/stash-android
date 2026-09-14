package goonarr.stash.features.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Stats
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.StringFormatters
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBackClick: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Library Stats") }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            })
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is StatsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is StatsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadStats() }) {
                            Text("Retry")
                        }
                    }
                }

                is StatsUiState.Success -> {
                    StatsContent(state.stats)
                }
            }
        }
    }
}

@Composable
fun StatsContent(stats: Stats) {
    val layoutDirection = LocalLayoutDirection.current
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 16.dp + navBarPadding.calculateStartPadding(layoutDirection),
            top = 16.dp,
            end = 16.dp + navBarPadding.calculateEndPadding(layoutDirection),
            bottom = 16.dp + navBarPadding.calculateBottomPadding()
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Library Overview
        item(span = { GridItemSpan(2) }) {
            SectionHeader("Library", Icons.Default.Inventory)
        }
        item { StatCard("Scenes", formatNumber(stats.sceneCount), Icons.Default.Movie, Color(0xFF2196F3)) }
        item { StatCard("Performers", formatNumber(stats.performerCount), Icons.Default.Person, Color(0xFFE91E63)) }
        item { StatCard("Studios", formatNumber(stats.studioCount), Icons.Default.Business, Color(0xFF9C27B0)) }
        item { StatCard("Tags", formatNumber(stats.tagCount), Icons.Default.Label, Color(0xFFFF9800)) }

        // Storage
        item(span = { GridItemSpan(2) }) {
            SectionHeader("Storage", Icons.Default.Storage)
        }
        item {
            StatCard(
                "Total Size",
                StringFormatters.formatFileSize(stats.scenesSize),
                Icons.Default.SdStorage,
                Color(0xFF4CAF50)
            )
        }
        item {
            StatCard(
                "Duration",
                StringFormatters.formatDurationText(stats.scenesDuration),
                Icons.Default.Schedule,
                Color(0xFF00BCD4)
            )
        }

        val avgDuration = if (stats.sceneCount > 0) stats.scenesDuration / stats.sceneCount else 0.0
        item {
            StatCard(
                title = "Avg Length",
                value = DateFormatters.formatDuration(avgDuration),
                icon = Icons.Default.Timer,
                color = Color(0xFF009688)
            )
        }
        item {
            StatCard(
                "Groups", value = formatNumber(stats.groupCount), icon = Icons.Default.Folder, color = Color(0xFF3F51B5)
            )
        }

        // Activity
        item(
            span = {
                GridItemSpan(2)
            }
        ) {
            SectionHeader("Activity", Icons.Default.Equalizer)
        }
        item { StatCard("O-Counter", formatNumber(stats.totalOCount), Icons.Default.Favorite, Color(0xFFF44336)) }
        item { StatCard("Total Plays", formatNumber(stats.totalPlayCount), Icons.Default.PlayArrow, Color(0xFF4CAF50)) }
        item {
            StatCard(
                "Scenes Played", formatNumber(stats.scenesPlayed), Icons.Default.CheckCircle, Color(0xFF2196F3)
            )
        }
        item {
            StatCard(
                "Play Time",
                StringFormatters.formatDurationText(stats.totalPlayDuration),
                Icons.Default.HourglassEmpty,
                Color(0xFFFF9800)
            )
        }

        // Progress
        item(span = { GridItemSpan(2) }) {
            ProgressCard(stats)
        }

        // Bottom Spacer
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProgressCard(stats: Stats) {
    val percent = if (stats.sceneCount > 0) (stats.scenesPlayed.toFloat() / stats.sceneCount.toFloat()) * 100 else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Scenes Watched",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${String.format(Locale.US, "%.1f", percent)}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${formatNumber(stats.scenesPlayed)} of ${formatNumber(stats.sceneCount)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun formatNumber(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val mockStats = Stats(
        sceneCount = 1234,
        // 50 GB
        scenesSize = 1024.0 * 1024 * 1024 * 50,
        // 10 Hours
        scenesDuration = 3600.0 * 10,
        imageCount = 500,
        // 500 MB
        imagesSize = 1024.0 * 1024 * 500,
        galleryCount = 50,
        performerCount = 100,
        studioCount = 20,
        tagCount = 300,
        groupCount = 5,
        movieCount = 10,
        totalOCount = 1000,
        // 5 Hours
        totalPlayDuration = 3600.0 * 5,
        totalPlayCount = 200,
        scenesPlayed = 150
    )

    StashTheme {
        StatsContent(stats = mockStats)
    }
}
