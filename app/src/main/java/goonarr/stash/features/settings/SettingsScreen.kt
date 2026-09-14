package goonarr.stash.features.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.LocalScaffoldPadding
import goonarr.stash.StashTokens
import goonarr.stash.core.model.GenerationOptions
import goonarr.stash.core.model.Job
import goonarr.stash.core.model.ScanOptions
import goonarr.stash.core.network.WebSocketState
import goonarr.stash.features.components.EditSection
import goonarr.stash.features.components.EditTextField
import goonarr.stash.features.components.StaggeredEntry
import goonarr.stash.features.settings.components.DescriptionToggleRow
import goonarr.stash.features.settings.components.JobQueueSection
import goonarr.stash.features.settings.components.JobQueueSectionPreview
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

/**
 * The main settings screen of the application.
 *
 * This screen allows the user to:
 * - Configure the Stash server connection (URL and API Key).
 * - Monitor the job queue.
 * - Manually trigger library scans and content generation.
 * - Access detailed scan and generation options.
 *
 * @param viewModel The view model managing the settings state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val haptic = LocalStashHapticFeedback.current
    val serverUrl by viewModel.serverUrl.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val isTesting by viewModel.isTestingConnection.collectAsState()
    val testMessage by viewModel.connectionTestMessage.collectAsState()
    val testSuccess by viewModel.connectionTestSuccess.collectAsState()
    val scanOptions by viewModel.scanOptions.collectAsState(initial = ScanOptions())
    val generationOptions by viewModel.generationOptions.collectAsState(initial = GenerationOptions())
    val hideBottomBarLabels by viewModel.hideBottomBarLabels.collectAsState(initial = false)
    val theme by viewModel.theme.collectAsState(initial = "goonarr")

    // StashDB state
    val stashDBApiKey by viewModel.stashDBApiKey.collectAsState()
    val isTestingStashDB by viewModel.isTestingStashDB.collectAsState()
    val stashDBTestMessage by viewModel.stashDBTestMessage.collectAsState()
    val stashDBTestSuccess by viewModel.stashDBTestSuccess.collectAsState()
    val excludeVRFromStashDB by viewModel.excludeVRFromStashDB.collectAsState(initial = false)
    val excludeCompilationsFromStashDB by viewModel.excludeCompilationsFromStashDB.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = LocalScaffoldPadding.current.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Task Queue - pass the flows directly to isolate recomposition
            StaggeredEntry(visible = true, index = 0) {
                JobQueueSection(
                    jobsFlow = viewModel.jobs,
                    webSocketStateFlow = viewModel.webSocketState,
                    onStopJob = viewModel::stopJob,
                    onStopAllJobs = viewModel::stopAllJobs
                )
            }

            // Library Management
            StaggeredEntry(visible = true, index = 1) {
                var scanOptionsExpanded by remember { mutableStateOf(false) }
                var generationOptionsExpanded by remember { mutableStateOf(false) }

                EditSection(title = "Library Tasks") {
                    LibraryTaskRow(
                        buttonLabel = "Scan",
                        expanded = scanOptionsExpanded,
                        onExpandedChange = { scanOptionsExpanded = it },
                        onButtonClick = viewModel::triggerScan,
                        optionsContent = {
                            ScanOptionsContent(
                                options = scanOptions,
                                onUpdateOptions = { viewModel.updateScanOptions(scanOptions.it()) }
                            )
                        }
                    )

                    LibraryTaskRow(
                        buttonLabel = "Generate",
                        expanded = generationOptionsExpanded,
                        onExpandedChange = { generationOptionsExpanded = it },
                        onButtonClick = viewModel::triggerGeneration,
                        optionsContent = {
                            GenerationOptionsContent(
                                options = generationOptions,
                                onUpdateOptions = { viewModel.updateGenerationOptions(generationOptions.it()) }
                            )
                        }
                    )
                }
            }

            // Server Connection
            StaggeredEntry(visible = true, index = 2) {
                // Managed by direct visibility now

                EditSection(
                    title = "Server Connection"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        EditTextField(
                            value = serverUrl,
                            onValueChange = viewModel::updateServerUrl,
                            label = "Server URL"
                        )

                        EditTextField(
                            value = apiKey,
                            onValueChange = viewModel::updateApiKey,
                            label = "API Key"
                        )

                        Button(
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                viewModel.testConnection()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            enabled = !isTesting
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .height(16.dp)
                                        .width(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("Testing...")
                            } else {
                                Text("Test Connection")
                            }
                        }

                        testMessage?.let { message ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (testSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (testSuccess) Color.Green else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (testSuccess) Color.Green else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Appearance
            StaggeredEntry(visible = true, index = 3) {
                EditSection(title = "Appearance") {
                    ThemeSelectionRow(
                        selectedTheme = theme,
                        onThemeSelected = viewModel::setTheme
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DescriptionToggleRow(
                        title = "Hide Bottom Bar Labels",
                        description = "Show only icons in the bottom navigation bar",
                        isOn = hideBottomBarLabels,
                        onToggle = viewModel::setHideBottomBarLabels
                    )
                }
            }

            // StashDB Integration
            StaggeredEntry(visible = true, index = 4) {
                EditSection(title = "StashDB Integration") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        EditTextField(
                            value = stashDBApiKey,
                            onValueChange = viewModel::updateStashDBApiKey,
                            label = "StashDB API Key"
                        )

                        Button(
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                viewModel.testStashDBConnection()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            enabled = !isTestingStashDB
                        ) {
                            if (isTestingStashDB) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .height(16.dp)
                                        .width(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("Testing...")
                            } else {
                                Text("Test StashDB Connection")
                            }
                        }

                        stashDBTestMessage?.let { message ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (stashDBTestSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (stashDBTestSuccess) Color.Green else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (stashDBTestSuccess) Color.Green else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Filtering Options",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        DescriptionToggleRow(
                            title = "Exclude VR Content",
                            description = "Hide VR scenes from StashDB favorites",
                            isOn = excludeVRFromStashDB,
                            onToggle = viewModel::setExcludeVRFromStashDB
                        )

                        DescriptionToggleRow(
                            title = "Exclude Compilations",
                            description = "Hide compilation scenes from StashDB favorites",
                            isOn = excludeCompilationsFromStashDB,
                            onToggle = viewModel::setExcludeCompilationsFromStashDB
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Get your API key from stashdb.org after logging in. " +
                                "Your favorites on StashDB will be displayed on the home screen.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Stateless version of [SettingsScreen] for previewing and testing purposes.
 *
 * @param serverUrl The current server URL.
 * @param apiKey The current API key.
 * @param isTesting Whether a connection test is currently in progress.
 * @param testMessage Optional message to display after a connection test.
 * @param testSuccess Whether the last connection test was successful.
 * @param jobs The list of current and recent jobs.
 * @param webSocketState The current state of the WebSocket connection.
 * @param onServerUrlChange Callback when the server URL changes.
 * @param onApiKeyChange Callback when the API key changes.
 * @param onTestConnection Callback to trigger a connection test.
 * @param onTriggerScan Callback to trigger a library scan.
 * @param onNavigateToScanOptions Callback to navigate to scan options.
 * @param onTriggerGeneration Callback to trigger content generation.
 * @param onNavigateToGenerationOptions Callback to navigate to generation options.
 * @param onStopJob Callback to stop a specific job.
 * @param onStopAllJobs Callback to stop all active jobs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenContent(
    serverUrl: String = "",
    apiKey: String = "",
    isTesting: Boolean = false,
    testMessage: String? = null,
    testSuccess: Boolean = false,
    jobs: List<Job> = emptyList(),
    webSocketState: WebSocketState = WebSocketState.Connected,
    scanOptions: ScanOptions = ScanOptions(),
    generationOptions: GenerationOptions = GenerationOptions(),
    scanOptionsExpanded: Boolean = false,
    onScanOptionsExpandedChange: (Boolean) -> Unit = {},
    generationOptionsExpanded: Boolean = false,
    onGenerationOptionsExpandedChange: (Boolean) -> Unit = {},
    onServerUrlChange: (String) -> Unit = {},
    onApiKeyChange: (String) -> Unit = {},
    onTestConnection: () -> Unit = {},
    onTriggerScan: () -> Unit = {},
    onUpdateScanOptions: (ScanOptions.() -> ScanOptions) -> Unit = {},
    onTriggerGeneration: () -> Unit = {},
    onUpdateGenerationOptions: (GenerationOptions.() -> GenerationOptions) -> Unit = {},
    onStopJob: (String) -> Unit = {},
    onStopAllJobs: () -> Unit = {}
) {
    val haptic = LocalStashHapticFeedback.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = LocalScaffoldPadding.current.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Task Queue
            StaggeredEntry(visible = true, index = 0) {
                JobQueueSectionPreview(
                    jobs = jobs,
                    webSocketState = webSocketState,
                    onStopJob = onStopJob,
                    onStopAllJobs = onStopAllJobs
                )
            }

            // Library Management
            StaggeredEntry(visible = true, index = 1) {
                EditSection(title = "Library Tasks") {
                    LibraryTaskRow(
                        buttonLabel = "Scan",
                        expanded = scanOptionsExpanded,
                        onExpandedChange = onScanOptionsExpandedChange,
                        onButtonClick = onTriggerScan,
                        optionsContent = {
                            ScanOptionsContent(
                                options = scanOptions,
                                onUpdateOptions = onUpdateScanOptions
                            )
                        }
                    )

                    LibraryTaskRow(
                        buttonLabel = "Generate",
                        expanded = generationOptionsExpanded,
                        onExpandedChange = onGenerationOptionsExpandedChange,
                        onButtonClick = onTriggerGeneration,
                        optionsContent = {
                            GenerationOptionsContent(
                                options = generationOptions,
                                onUpdateOptions = onUpdateGenerationOptions
                            )
                        }
                    )
                }
            }

            // Server Connection
            StaggeredEntry(visible = true, index = 2) {
                EditSection(
                    title = "Server Connection"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        EditTextField(
                            value = serverUrl,
                            onValueChange = onServerUrlChange,
                            label = "Server URL"
                        )

                        EditTextField(
                            value = apiKey,
                            onValueChange = onApiKeyChange,
                            label = "API Key"
                        )

                        Button(
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                onTestConnection()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            enabled = !isTesting
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .height(16.dp)
                                        .width(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("Testing...")
                            } else {
                                Text("Test Connection")
                            }
                        }

                        testMessage?.let { message ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (testSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (testSuccess) Color.Green else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (testSuccess) Color.Green else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Components

/**
 * A reusable row for library tasks with an action button and collapsible options.
 */
@Composable
internal fun LibraryTaskRow(
    buttonLabel: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onButtonClick: () -> Unit,
    optionsContent: @Composable () -> Unit
) {
    val haptic = LocalStashHapticFeedback.current
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = {
                haptic.perform(StashHapticFeedbackType.Medium)
                onButtonClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonLabel)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.perform(StashHapticFeedbackType.Light)
                    onExpandedChange(!expanded)
                }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (expanded) "Hide Options" else "Show Options",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                optionsContent()
            }
        }
    }
}

@Composable
internal fun ScanOptionsContent(
    options: ScanOptions,
    onUpdateOptions: (ScanOptions.() -> ScanOptions) -> Unit
) {
    Column {
        Text(
            text = "Scan Behavior",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        DescriptionToggleRow(
            title = "Rescan All Files",
            description = "Force rescan of all files, not just new ones",
            isOn = options.rescan,
            onToggle = { onUpdateOptions { copy(rescan = it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Generation Options",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        DescriptionToggleRow(
            title = "Generate Covers",
            description = "Generate cover images (screenshots) for scenes",
            isOn = options.scanGenerateCovers,
            onToggle = { onUpdateOptions { copy(scanGenerateCovers = it) } }
        )
        DescriptionToggleRow(
            title = "Generate Previews",
            description = "Generate preview videos (short clips from the scene)",
            isOn = options.scanGeneratePreviews,
            onToggle = { onUpdateOptions { copy(scanGeneratePreviews = it) } }
        )
        DescriptionToggleRow(
            title = "Generate Animated Image Previews",
            description = "Generate animated image previews (webp/gif)",
            isOn = options.scanGenerateImagePreviews,
            onToggle = { onUpdateOptions { copy(scanGenerateImagePreviews = it) } }
        )
        DescriptionToggleRow(
            title = "Generate Scrubber Sprites",
            description = "Generate sprite sheets for timeline scrubbing",
            isOn = options.scanGenerateSprites,
            onToggle = { onUpdateOptions { copy(scanGenerateSprites = it) } }
        )
        DescriptionToggleRow(
            title = "Generate Perceptual Hashes",
            description = "Generate perceptual hashes for duplicate detection",
            isOn = options.scanGeneratePhashes,
            onToggle = { onUpdateOptions { copy(scanGeneratePhashes = it) } }
        )
        DescriptionToggleRow(
            title = "Generate Thumbnails",
            description = "Generate thumbnails for images",
            isOn = options.scanGenerateThumbnails,
            onToggle = { onUpdateOptions { copy(scanGenerateThumbnails = it) } }
        )
        DescriptionToggleRow(
            title = "Generate Clip Previews",
            description = "Generate preview clips",
            isOn = options.scanGenerateClipPreviews,
            onToggle = { onUpdateOptions { copy(scanGenerateClipPreviews = it) } }
        )
    }
}

@Composable
internal fun GenerationOptionsContent(
    options: GenerationOptions,
    onUpdateOptions: (GenerationOptions.() -> GenerationOptions) -> Unit
) {
    Column {
        Text(
            text = "Capture Types",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        DescriptionToggleRow(
            title = "Covers",
            description = "Generate cover images (screenshots) for scenes",
            isOn = options.covers,
            onToggle = { onUpdateOptions { copy(covers = it) } }
        )
        DescriptionToggleRow(
            title = "Sprites",
            description = "Generate sprite sheets for timeline scrubbing",
            isOn = options.sprites,
            onToggle = { onUpdateOptions { copy(sprites = it) } }
        )
        DescriptionToggleRow(
            title = "Previews",
            description = "Generate preview videos (short clips from the scene)",
            isOn = options.previews,
            onToggle = { onUpdateOptions { copy(previews = it) } }
        )
        DescriptionToggleRow(
            title = "Image Previews",
            description = "Generate animated image previews (webp/gif)",
            isOn = options.imagePreviews,
            onToggle = { onUpdateOptions { copy(imagePreviews = it) } }
        )
        DescriptionToggleRow(
            title = "Markers",
            description = "Generate default screenshots for markers",
            isOn = options.markers,
            onToggle = { onUpdateOptions { copy(markers = it) } }
        )
        DescriptionToggleRow(
            title = "Marker Image Previews",
            description = "Generate animated previews for markers",
            isOn = options.markerImagePreviews,
            onToggle = { onUpdateOptions { copy(markerImagePreviews = it) } }
        )
        DescriptionToggleRow(
            title = "Marker Screenshots",
            description = "Generate screenshots for markers",
            isOn = options.markerScreenshots,
            onToggle = { onUpdateOptions { copy(markerScreenshots = it) } }
        )
        DescriptionToggleRow(
            title = "Transcodes",
            description = "Generate transcoded versions of videos",
            isOn = options.transcodes,
            onToggle = { onUpdateOptions { copy(transcodes = it) } }
        )
        DescriptionToggleRow(
            title = "Force Transcodes",
            description = "Generate transcodes even if not required",
            isOn = options.forceTranscodes,
            onToggle = { onUpdateOptions { copy(forceTranscodes = it) } }
        )
        DescriptionToggleRow(
            title = "Phashes",
            description = "Generate perceptual hashes for duplicate detection",
            isOn = options.phashes,
            onToggle = { onUpdateOptions { copy(phashes = it) } }
        )
        DescriptionToggleRow(
            title = "Interactive Heatmaps & Speeds",
            description = "Generate interactive heatmaps for funscript files",
            isOn = options.interactiveHeatmapsSpeeds,
            onToggle = { onUpdateOptions { copy(interactiveHeatmapsSpeeds = it) } }
        )
        DescriptionToggleRow(
            title = "Image Thumbnails",
            description = "Generate thumbnails for images",
            isOn = options.imageThumbnails,
            onToggle = { onUpdateOptions { copy(imageThumbnails = it) } }
        )
        DescriptionToggleRow(
            title = "Clip Previews",
            description = "Generate preview clips",
            isOn = options.clipPreviews,
            onToggle = { onUpdateOptions { copy(clipPreviews = it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "⚠️ Danger Zone",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        DescriptionToggleRow(
            title = "Overwrite Existing Files",
            description = "Regenerate and overwrite existing generated content",
            isOn = options.overwrite,
            onToggle = { onUpdateOptions { copy(overwrite = it) } }
        )
    }
}

/**
 * Theme selection row with three button options (Goonarr, Stash, and Material You).
 */
@Composable
internal fun ThemeSelectionRow(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val haptic = LocalStashHapticFeedback.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Defined identity colors
        val goonarrColor = Color(0xFF0D1426)
        val stashColor = Color(0xFF137CBD)

        val materialPaletteBrush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFE46962),
                Color(0xFFF2A43E),
                Color(0xFFF2C94C),
                Color(0xFF81C995),
                Color(0xFF8AB4F8),
                Color(0xFFC68AFF),
                Color(0xFFE46962)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOptionButton(
                label = "Goonarr",
                isSelected = selectedTheme == "goonarr",
                themeBrush = SolidColor(goonarrColor),
                themeColor = goonarrColor,
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onThemeSelected("goonarr")
                },
                modifier = Modifier.weight(1f)
            )

            ThemeOptionButton(
                label = "Stash",
                isSelected = selectedTheme == "stash",
                themeBrush = SolidColor(stashColor),
                themeColor = stashColor,
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onThemeSelected("stash")
                },
                modifier = Modifier.weight(1f)
            )

            ThemeOptionButton(
                label = "Dynamic",
                isSelected = selectedTheme == "material_you",
                themeBrush = materialPaletteBrush,
                themeColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onThemeSelected("material_you")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = when (selectedTheme) {
                "stash" -> "Blueprint.js-inspired theme with blue accent"
                "material_you" -> "Adapts to your system colors and wallpaper (Android 12+)"
                else -> "Default theme with amber accent"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Individual theme option button with selection indicator.
 */
@Composable
internal fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    themeBrush: Brush,
    themeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectionColor = Color(0xFFFFBA33)
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(StashTokens.Radius.Card),
        color = if (isSelected) {
            selectionColor.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 3.dp else 2.dp,
            brush = if (isSelected) SolidColor(selectionColor) else themeBrush
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(bottom = 4.dp),
                    tint = selectionColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) selectionColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
