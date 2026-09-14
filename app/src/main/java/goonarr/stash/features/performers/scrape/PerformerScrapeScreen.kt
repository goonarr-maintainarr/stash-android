package goonarr.stash.features.performers.scrape

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.features.components.StaggeredEntry
import goonarr.stash.features.performers.scrape.components.PerformerResultCard
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformerScrapeScreen(
    performerId: String,
    onBackClick: () -> Unit,
    viewModel: PerformerScrapeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var heroColor by remember { mutableStateOf<Color?>(null) }
    val context = LocalContext.current
    val haptic = LocalStashHapticFeedback.current

    LaunchedEffect(performerId) {
        viewModel.loadStashBoxes()
        viewModel.loadPerformer(performerId)
    }

    LaunchedEffect(uiState.currentPerformer?.imagePath) {
        val imagePath = uiState.currentPerformer?.imagePath
        if (imagePath != null) {
            HeroAccentColor.extract(context, imagePath)?.let { heroColor = it }
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.stashBoxes.isNotEmpty()) {
            if (uiState.scrapeResults.isNotEmpty()) {
                haptic.perform(StashHapticFeedbackType.Success)
            } else if (uiState.query.isNotEmpty() && uiState.scrapeResults.isEmpty()) {
                haptic.perform(StashHapticFeedbackType.Error)
            }
        }
    }

    LaunchedEffect(uiState.applySuccess) {
        if (uiState.applySuccess) {
            haptic.perform(StashHapticFeedbackType.Success)
            viewModel.resetApplySuccess()
            onBackClick()
        }
    }

    val performerColors = rememberSceneColors(heroColor)
    val imageUrl = uiState.currentPerformer?.imagePath
    val listState = rememberLazyListState()

    // Intercept back press when a result is selected
    BackHandler(enabled = uiState.selectedResult != null) {
        viewModel.selectResult(null)
    }

    CompositionLocalProvider(LocalStashDynamicColors provides performerColors) {
        if (uiState.selectedResult != null) {
            PerformerScrapeResultReviewScreen(
                scrapedPerformer = uiState.selectedResult!!,
                currentPerformer = uiState.currentPerformer,
                selection = uiState.selection,
                onSelectionChanged = viewModel::updateSelection,
                isLoading = uiState.isLoading,
                onApply = { viewModel.applyScrapeResult() },
                onBack = { viewModel.selectResult(null) },
                remoteSiteEndpoint = uiState.selectedStashBox?.endpoint,
                scraperName = uiState.selectedStashBox?.name
            )
            return@CompositionLocalProvider
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Blurred Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
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

            PerformerScrapeContent(
                uiState = uiState,
                lazyListState = listState,
                onBackClick = onBackClick,
                onStashBoxSelect = viewModel::selectStashBox,
                onQueryChange = viewModel::updateQuery,
                onScrape = viewModel::scrape,
                onResultClick = viewModel::selectResult
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformerScrapeContent(
    uiState: PerformerScrapeUiState,
    lazyListState: LazyListState,
    onBackClick: () -> Unit,
    onStashBoxSelect: (StashBox) -> Unit,
    onQueryChange: (String) -> Unit,
    onScrape: () -> Unit,
    onResultClick: (ScrapedPerformer) -> Unit
) {
    val performerColors = StashTheme.colors
    val haptic = LocalStashHapticFeedback.current
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Scrape Performer") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = performerColors.complementary,
                    navigationIconContentColor = performerColors.complementary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Configuration
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // StashBox Selector (Radio Buttons)
                StaggeredEntry(visible = contentVisible, index = 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = performerColors.primary.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Scraper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = performerColors.primary
                            )

                            if (uiState.isLoading && uiState.stashBoxes.isEmpty()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = performerColors.primary
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
                                                selectedColor = performerColors.primary,
                                                unselectedColor = performerColors.complementary
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
                if (uiState.currentPerformer?.stashIds?.isNotEmpty() == true) {
                    StaggeredEntry(visible = contentVisible, index = 2) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Stash IDs",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StashTheme.colors.complementary
                            )
                            uiState.currentPerformer.stashIds.forEach { stashId ->
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
                                                tint = performerColors.complementary
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
                                focusedBorderColor = performerColors.primary,
                                focusedLabelColor = performerColors.primary,
                                cursorColor = performerColors.primary
                            )
                        )

                        Button(
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                onScrape()
                            },
                            enabled = !uiState.isLoading && uiState.selectedStashBox != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = performerColors.primary,
                                contentColor = performerColors.onPrimary
                            )
                        ) {
                            Text("Scrape Performer")
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.scrapeResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = performerColors.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scraping performer...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // Results List
                StaggeredEntry(visible = contentVisible, index = 4) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            top = 0.dp,
                            bottom = paddingValues.calculateBottomPadding() + 16.dp
                        )
                    ) {
                        items(uiState.scrapeResults) { result ->
                            PerformerResultCard(
                                performer = result,
                                onClick = { onResultClick(result) }
                            )
                        }
                    }
                }
            }
        }
    }
}
