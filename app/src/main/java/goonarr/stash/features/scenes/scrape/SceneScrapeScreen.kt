package goonarr.stash.features.scenes.scrape

import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.ScenePaths
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.core.model.scraper.ParseMode
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.core.model.scraper.TagOperation
import goonarr.stash.core.model.scraper.TaggerConfig
import goonarr.stash.features.components.StaggeredEntry
import goonarr.stash.features.scenes.scenecard.SceneCompactRow
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneScrapeScreen(
    sceneId: String,
    onBackClick: () -> Unit,
    viewModel: SceneScrapeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfigDialog by remember { mutableStateOf(false) }
    var heroColor by remember { mutableStateOf<Color?>(null) }
    val context = LocalContext.current
    val haptic = LocalStashHapticFeedback.current

    LaunchedEffect(sceneId) {
        viewModel.loadStashBoxes()
        viewModel.loadScene(sceneId)
    }

    LaunchedEffect(uiState.currentScene?.paths?.screenshot) {
        val screenshot = uiState.currentScene?.paths?.screenshot
        if (screenshot != null) {
            HeroAccentColor.extract(context, screenshot)?.let { heroColor = it }
        }
    }

    LaunchedEffect(uiState.isScraping) {
        if (!uiState.isScraping && uiState.stashBoxes.isNotEmpty()) {
            // Only trigger if we were actually scraping (not just initial load)
            if (uiState.scrapedResults.isNotEmpty()) {
                haptic.perform(StashHapticFeedbackType.Success)
            } else if (uiState.query.isNotEmpty()) {
                // If searching and no results, could be error or just empty
                haptic.perform(StashHapticFeedbackType.Error)
            }
        }
    }

    val sceneColors = rememberSceneColors(heroColor)
    val screenshotUrl = uiState.currentScene?.paths?.screenshot

    CompositionLocalProvider(LocalStashDynamicColors provides sceneColors) {
        if (uiState.selectedResult != null) {
            ScrapeResultReviewScreen(
                scrapedScene = uiState.selectedResult!!,
                currentScene = uiState.currentScene,
                taggerConfig = uiState.taggerConfig,
                onApply = { selection -> viewModel.applyScrapeResult(uiState.selectedResult!!, selection) },
                onBack = { viewModel.selectResult(null) },
                viewModel = viewModel
            )
            return@CompositionLocalProvider
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Blurred Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (screenshotUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(screenshotUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                    )
                }
            }

            SceneScrapeContent(
                uiState = uiState,
                onBackClick = onBackClick,
                onSettingsClick = { showConfigDialog = true },
                onStashBoxSelect = viewModel::selectStashBox,
                onQueryChange = viewModel::updateQuery,
                onScrape = viewModel::scrape,
                onScrapeByFragment = viewModel::scrapeByFragment,
                onResultClick = viewModel::selectResult
            )
        }
    }

    // Tagger Config Dialog
    if (showConfigDialog) {
        TaggerConfigDialog(
            config = uiState.taggerConfig,
            onConfigChanged = { viewModel.updateConfig(it) },
            onDismiss = {
                viewModel.saveConfig()
                showConfigDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneScrapeContent(
    uiState: SceneScrapeUiState,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStashBoxSelect: (StashBox) -> Unit,
    onQueryChange: (String) -> Unit,
    onScrape: () -> Unit,
    onScrapeByFragment: () -> Unit,
    onResultClick: (ScrapedScene) -> Unit
) {
    val sceneColors = StashTheme.colors
    val haptic = LocalStashHapticFeedback.current
    // Visibility state for staggered entry
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Scrape Scene") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuration")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = sceneColors.complementary,
                    navigationIconContentColor = sceneColors.complementary,
                    actionIconContentColor = sceneColors.complementary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Configuration
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // StashBox Selector (Radio Buttons)
                StaggeredEntry(visible = contentVisible, index = 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = sceneColors.primary.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Scraper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = sceneColors.primary
                            )

                            if (uiState.isLoading && uiState.stashBoxes.isEmpty()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = sceneColors.primary
                                )
                            } else {
                                uiState.stashBoxes.forEach { box ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.perform(StashHapticFeedbackType.Selection)
                                                onStashBoxSelect(box)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = box == uiState.selectedStashBox,
                                            onClick = { onStashBoxSelect(box) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = sceneColors.primary,
                                                unselectedColor = sceneColors.complementary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = box.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = box.endpoint,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Stash IDs Section
                if (uiState.currentScene?.stashIds?.isNotEmpty() == true) {
                    StaggeredEntry(visible = contentVisible, index = 2) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Stash IDs",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StashTheme.colors.complementary
                            )
                            uiState.currentScene.stashIds.forEach { stashId ->
                                Surface(
                                    color = StashTheme.colors.primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stashId.endpoint,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = stashId.stashId,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        IconButton(
                                            onClick = { onQueryChange(stashId.stashId) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy to Query",
                                                modifier = Modifier.size(18.dp),
                                                tint = sceneColors.complementary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Query Input & Buttons
                StaggeredEntry(visible = contentVisible, index = 3) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = onQueryChange,
                            label = { Text("Search Query") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = sceneColors.primary,
                                focusedLabelColor = sceneColors.primary,
                                cursorColor = sceneColors.primary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    onScrape()
                                },
                                enabled = !uiState.isScraping && uiState.selectedStashBox != null,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = sceneColors.primary,
                                    contentColor = sceneColors.onPrimary
                                )
                            ) {
                                Text("Search")
                            }

                            Button(
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    onScrapeByFragment()
                                },
                                enabled = !uiState.isScraping && uiState.selectedStashBox != null,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = sceneColors.complementary,
                                    contentColor = sceneColors.onPrimary
                                )
                            ) {
                                Text("Scrape By Fragment")
                            }
                        }
                    }
                }
            }

            if (uiState.isScraping && uiState.scrapedResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = sceneColors.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scraping scene...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // Results List
                StaggeredEntry(visible = contentVisible, index = 4) {
                    LazyColumn(
                        // Using fillMaxSize instead of weight since it's the last item
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.scrapedResults) { result ->
                            SceneCompactRow(
                                scene = result.toScene(),
                                onClick = { onResultClick(result) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaggerConfigDialog(config: TaggerConfig, onConfigChanged: (TaggerConfig) -> Unit, onDismiss: () -> Unit) {
    var showBlacklist by remember { mutableStateOf(false) }
    var showGenderFilter by remember { mutableStateOf(false) }
    var showExcludedPerformerFields by remember { mutableStateOf(false) }
    var showExcludedStudioFields by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tagger Configuration") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Query Section
                ConfigSection(title = "Search Query") {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text("Parse Mode", modifier = Modifier.weight(1f))
                            ParseModeSelector(
                                selectedMode = config.mode,
                                onModeSelected = { onConfigChanged(config.copy(mode = it)) }
                            )
                        }

                        ListItem(
                            title = "Blacklist",
                            subtitle = "${config.blacklist.size} patterns",
                            onClick = { showBlacklist = true }
                        )
                    }
                }

                // Tagger Settings Section
                ConfigSection(title = "Tagger Settings") {
                    Column {
                        ConfigToggle(
                            title = "Set Tags",
                            subtitle = "Attach tags to scene, either by overwriting or merging with existing tags on scene.",
                            checked = config.setTags,
                            onCheckedChange = { onConfigChanged(config.copy(setTags = it)) }
                        )
                        if (config.setTags) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text("Operation", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                TagOperationSelector(
                                    selectedOp = config.tagOperation,
                                    onOpSelected = { onConfigChanged(config.copy(tagOperation = it)) }
                                )
                            }
                        }

                        ConfigToggle(
                            title = "Set Cover Image",
                            subtitle = "Replace the scene cover if one is found.",
                            checked = config.setCoverImage,
                            onCheckedChange = { onConfigChanged(config.copy(setCoverImage = it)) }
                        )
                        ConfigToggle(
                            title = "Mark as Organized",
                            subtitle = "Immediately mark the scene as Organized after the Save button is clicked.",
                            checked = config.markSceneAsOrganizedOnSave,
                            onCheckedChange = { onConfigChanged(config.copy(markSceneAsOrganizedOnSave = it)) }
                        )
                        ConfigToggle(
                            title = "Create Parent Studios",
                            subtitle = "Create parent studio hierarchy if missing.",
                            checked = config.createParentStudios,
                            onCheckedChange = { onConfigChanged(config.copy(createParentStudios = it)) }
                        )
                    }
                }

                // Performer Filters
                ConfigSection(title = "Performer Filters") {
                    Column {
                        ListItem(
                            title = "Genders",
                            subtitle = config.performerGenders?.let { "${it.size} selected" } ?: "All",
                            onClick = { showGenderFilter = true }
                        )
                        ListItem(
                            title = "Excluded Fields",
                            subtitle = "${config.excludedPerformerFields.size} excluded",
                            onClick = { showExcludedPerformerFields = true }
                        )
                    }
                }

                // Studio Filters
                ConfigSection(title = "Studio Filters") {
                    Column {
                        ListItem(
                            title = "Excluded Fields",
                            subtitle = "${config.excludedStudioFields.size} excluded",
                            onClick = { showExcludedStudioFields = true }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = StashTheme.colors.primary)
            ) {
                Text("Done")
            }
        }
    )

    if (showBlacklist) {
        BlacklistDialog(
            blacklist = config.blacklist,
            onBlacklistChanged = { onConfigChanged(config.copy(blacklist = it)) },
            onDismiss = { showBlacklist = false }
        )
    }

    if (showGenderFilter) {
        MultiSelectDialog(
            title = "Gender Filter",
            options = listOf("Female", "Male", "Transgender Female", "Transgender Male", "Non-binary", "Other"),
            selectedOptions = config.performerGenders ?: emptyList(),
            onSelectionChanged = { onConfigChanged(config.copy(performerGenders = it.takeIf { it.isNotEmpty() })) },
            onDismiss = { showGenderFilter = false }
        )
    }

    if (showExcludedPerformerFields) {
        MultiSelectDialog(
            title = "Excluded Performer Fields",
            options = TaggerConfig.allPerformerFields,
            selectedOptions = config.excludedPerformerFields,
            onSelectionChanged = { onConfigChanged(config.copy(excludedPerformerFields = it)) },
            onDismiss = { showExcludedPerformerFields = false }
        )
    }

    if (showExcludedStudioFields) {
        MultiSelectDialog(
            title = "Excluded Studio Fields",
            options = TaggerConfig.allStudioFields,
            selectedOptions = config.excludedStudioFields,
            onSelectionChanged = { onConfigChanged(config.copy(excludedStudioFields = it)) },
            onDismiss = { showExcludedStudioFields = false }
        )
    }
}

@Composable
fun ConfigSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = StashTheme.colors.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
    }
}

@Composable
fun ConfigToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, subtitle: String? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ListItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ParseModeSelector(selectedMode: ParseMode, onModeSelected: (ParseMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(contentColor = StashTheme.colors.primary)
        ) {
            Text(selectedMode.displayName)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(StashTokens.Radius.Card)
        ) {
            ParseMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.displayName) },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TagOperationSelector(selectedOp: TagOperation, onOpSelected: (TagOperation) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(contentColor = StashTheme.colors.primary)
        ) {
            Text(selectedOp.displayName, style = MaterialTheme.typography.bodySmall)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(StashTokens.Radius.Card)
        ) {
            TagOperation.entries.forEach { op ->
                DropdownMenuItem(
                    text = { Text(op.displayName) },
                    onClick = {
                        onOpSelected(op)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BlacklistDialog(blacklist: List<String>, onBlacklistChanged: (List<String>) -> Unit, onDismiss: () -> Unit) {
    var newPattern by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Blacklist Patterns") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newPattern,
                        onValueChange = { newPattern = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Regex pattern...") },
                        label = { Text("Add Pattern") }
                    )
                    IconButton(onClick = {
                        if (newPattern.isNotBlank()) {
                            onBlacklistChanged(blacklist + newPattern)
                            newPattern = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }

                Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    blacklist.forEach { pattern ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(pattern, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { onBlacklistChanged(blacklist - pattern) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(18.dp), tint = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = StashTheme.colors.primary)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun MultiSelectDialog(
    title: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    val isSelected = selectedOptions.contains(option)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    onSelectionChanged(selectedOptions - option)
                                } else {
                                    onSelectionChanged(selectedOptions + option)
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(checked = isSelected, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = StashTheme.colors.primary)
            ) {
                Text("Done")
            }
        }
    )
}

// Mapper extension: Converts a ScrapedScene to a Scene for display in SceneCard
private fun ScrapedScene.toScene(): Scene {
    // Convert image to data: URI format if it's Base64
    val fixedImage = when {
        this.image == null -> null
        this.image.startsWith("http") -> this.image
        else -> {
            val base64Data = if (this.image.startsWith("data:")) {
                this.image.substringAfter("base64,")
            } else {
                this.image
            }
            val cleanBase64 = base64Data.replace("\\s".toRegex(), "")
            try {
                val decoded = Base64.decode(cleanBase64, Base64.DEFAULT)
                if (decoded.isNotEmpty()) "data:image/jpeg;base64,$cleanBase64" else null
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    return Scene(
        id = "scraped_result",
        title = this.title,
        details = this.details,
        date = this.date,
        paths = ScenePaths(
            screenshot = fixedImage,
            preview = null,
            stream = null,
            sprite = null,
            vtt = null
        ),
        oCounter = null,
        studio = this.studio?.let {
            Studio(id = "s", name = it.name, imagePath = null, parentStudio = null)
        },
        performers = this.performers?.map {
            Performer(
                id = "p",
                name = it.name,
                imagePath = it.images?.firstOrNull(),
                gender = it.gender
            )
        } ?: emptyList(),
        tags = this.tags?.map {
            Tag(id = "t", name = it.name)
        } ?: emptyList()
    )
}
