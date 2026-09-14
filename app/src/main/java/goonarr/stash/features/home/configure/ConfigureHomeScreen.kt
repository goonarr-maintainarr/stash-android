package goonarr.stash.features.home.configure

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.StashTokens
import goonarr.stash.core.model.HomeSectionIds
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureHomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTags: () -> Unit,
    onTagLongPress: (String) -> Unit,
    viewModel: ConfigureHomeViewModel = hiltViewModel()
) {
    val configItems: List<HomeConfigItem> by viewModel.items.collectAsState()
    val savedFilterSections: List<SavedFilterSection> by viewModel.savedFilterSections.collectAsState()
    val syncHomeLayout: Boolean by viewModel.syncHomeLayout.collectAsState()
    val isSyncing: Boolean by viewModel.isSyncing.collectAsState()
    val defaultRows: List<HomeConfigItem> by viewModel.defaultRows.collectAsState()
    val tagRows: List<HomeConfigItem> by viewModel.tagRows.collectAsState()
    val selectedTab: ConfigureHomeTab by viewModel.selectedTab.collectAsState()
    val scrollToItemId: String? by viewModel.scrollToItemId.collectAsState()
    val haptic = LocalStashHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        haptic.perform(StashHapticFeedbackType.Selection)
        viewModel.moveItem(from.index, to.index)
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        snackbarHost = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = navBarPadding + 16.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) { data ->
                    Snackbar(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(0.9f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(StashTokens.Radius.Card)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = data.visuals.message,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Change Rows • ${configItems.size}",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = { },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                PrimaryTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
                ) {
                    ConfigureHomeTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                viewModel.setTab(tab)
                            },
                            text = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                ConfigureHomeTab.OVERVIEW -> {
                    OverviewTabContent(
                        items = configItems,
                        onMoveItem = viewModel::moveItem,
                        onItemLongPress = viewModel::navigateToSourceTab,
                        topPadding = topPadding,
                        bottomPadding = navBarPadding
                    )
                }

                ConfigureHomeTab.DEFAULT_ROWS -> {
                    DefaultRowsTabContent(
                        defaultRows = defaultRows,
                        onToggleVisibility = viewModel::toggleVisibility,
                        onResetToDefault = {
                            viewModel.resetToDefaultLayout()
                            scope.launch {
                                snackbarHostState.showSnackbar("Home Page Restored to Default")
                            }
                        },
                        scrollToItemId = scrollToItemId,
                        onScrollComplete = viewModel::clearScrollToItem,
                        topPadding = topPadding,
                        bottomPadding = navBarPadding
                    )
                }

                ConfigureHomeTab.SAVED_FILTERS -> {
                    SavedFiltersTabContent(
                        sections = savedFilterSections,
                        isSyncing = isSyncing,
                        onUseStashHomePage = {
                            viewModel.useStashHomePage()
                            scope.launch {
                                snackbarHostState.showSnackbar("Home Page Updated to Match Stash")
                            }
                        },
                        onToggleSavedFilterVisibility = viewModel::toggleSavedFilterVisibility,
                        scrollToItemId = scrollToItemId,
                        onScrollComplete = viewModel::clearScrollToItem,
                        topPadding = topPadding,
                        bottomPadding = navBarPadding
                    )
                }

                ConfigureHomeTab.TAGS -> {
                    TagsTabContent(
                        tagRows = tagRows,
                        onToggleVisibility = viewModel::toggleVisibility,
                        scrollToItemId = scrollToItemId,
                        onScrollComplete = viewModel::clearScrollToItem,
                        onNavigateToTags = onNavigateToTags,
                        onTagLongPress = onTagLongPress,
                        topPadding = topPadding,
                        bottomPadding = navBarPadding
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OverviewTabContent(
    items: List<HomeConfigItem>,
    onMoveItem: (Int, Int) -> Unit,
    onItemLongPress: (HomeConfigItem) -> Unit,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val haptic = LocalStashHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Adjust for the description item at index 0
        if (from.index > 0 && to.index > 0) {
            haptic.perform(StashHapticFeedbackType.Selection)
            onMoveItem(from.index - 1, to.index - 1)
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(top = topPadding, bottom = 16.dp + bottomPadding),
        modifier = Modifier.fillMaxSize()
    ) {
        // Description at the top
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    "Customize the order of sections on your home screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    "Drag to reorder active sections",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 12.dp, end = 16.dp)
                )
            }
        }

        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
                val scale by animateFloatAsState(
                    targetValue = if (isDragging) 1.05f else 1f,
                    label = "scale"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            if (isDragging) {
                                shadowElevation = 8.dp.toPx()
                            }
                        }
                        .combinedClickable(
                            onClick = { /* Handle normal click if needed, or leave empty */ },
                            onLongClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                onItemLongPress(item)
                            }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDragging) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    shape = RoundedCornerShape(StashTokens.Radius.Card)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            modifier = Modifier
                                .draggableHandle(onDragStarted = { haptic.perform(StashHapticFeedbackType.Heavy) })
                                .padding(end = 12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val subtitle = when {
                                item.isDefault -> "Default row"
                                item.id.startsWith("SAVED_FILTER_") -> "Saved filter"
                                item.id.startsWith("TAG_") -> "Followed tag"
                                else -> null
                            }
                            if (subtitle != null) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Empty state when no items are available
        if (items.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(StashTokens.Radius.Card)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No rows available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enable default rows or saved filters in their respective tabs to add sections to your home screen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultRowsTabContent(
    defaultRows: List<HomeConfigItem>,
    onToggleVisibility: (String) -> Unit,
    onResetToDefault: () -> Unit,
    scrollToItemId: String?,
    onScrollComplete: () -> Unit,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val haptic = LocalStashHapticFeedback.current
    val lazyListState = rememberLazyListState()

    LaunchedEffect(scrollToItemId) {
        if (scrollToItemId != null) {
            val index = defaultRows.indexOfFirst { it.id == scrollToItemId }
            if (index != -1) {
                // Adjust for the header at index 0
                lazyListState.animateScrollToItem(index + 1)
                onScrollComplete()
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp + topPadding, end = 16.dp, bottom = 16.dp + bottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Select which standard rows to include on your home screen. " +
                    "Enabled rows will appear in the Overview tab for reordering.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedButton(
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Selection)
                    onResetToDefault()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(StashTokens.Radius.Button),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Reset to Defaults")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(defaultRows, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(StashTokens.Radius.Card)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = !item.isHidden,
                        onCheckedChange = {
                            haptic.perform(StashHapticFeedbackType.Selection)
                            onToggleVisibility(item.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedFiltersTabContent(
    sections: List<SavedFilterSection>,
    isSyncing: Boolean,
    onUseStashHomePage: () -> Unit,
    onToggleSavedFilterVisibility: (String) -> Unit,
    scrollToItemId: String?,
    onScrollComplete: () -> Unit,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val haptic = LocalStashHapticFeedback.current
    val lazyListState = rememberLazyListState()

    LaunchedEffect(scrollToItemId) {
        if (scrollToItemId != null) {
            var absoluteIndex = 1 // Start after the first item (description)
            var found = false
            for (section in sections) {
                if (found) break
                absoluteIndex++ // Section Header
                for (filter in section.filters) {
                    if (filter.filter.id == scrollToItemId) {
                        found = true
                        break
                    }
                    absoluteIndex++
                }
            }

            if (found) {
                lazyListState.animateScrollToItem(absoluteIndex)
                onScrollComplete()
            }
        }
    }

    if (sections.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )

                Text(
                    text = "You haven't created any saved filters yet. " +
                        "Create filters in Stash and they will appear here to be added to your home screen.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = {
                        haptic.perform(StashHapticFeedbackType.Selection)
                        onUseStashHomePage()
                    },
                    shape = RoundedCornerShape(StashTokens.Radius.Button),
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Sync from Stash")
                    }
                }
            }
        }
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp + topPadding, end = 16.dp, bottom = 16.dp + bottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Manage which saved filters are shown as rows on your home screen. " +
                            "Enabled filters will appear in the Overview tab for reordering.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Selection)
                                onUseStashHomePage()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(StashTokens.Radius.Button),
                            enabled = !isSyncing,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Use Stash Home Page")
                            }
                        }
                    }
                }
            }

            sections.forEach { section ->
                item(key = "header_${section.mode}") {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(section.filters, key = { it.filter.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(StashTokens.Radius.Card)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.filter.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Switch(
                                checked = item.isEnabled,
                                onCheckedChange = {
                                    haptic.perform(StashHapticFeedbackType.Selection)
                                    onToggleSavedFilterVisibility(item.filter.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagsTabContent(
    tagRows: List<HomeConfigItem>,
    onToggleVisibility: (String) -> Unit,
    scrollToItemId: String?,
    onScrollComplete: () -> Unit,
    onNavigateToTags: () -> Unit,
    onTagLongPress: (String) -> Unit,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val haptic = LocalStashHapticFeedback.current
    val lazyListState = rememberLazyListState()

    LaunchedEffect(scrollToItemId) {
        if (scrollToItemId != null) {
            val index = tagRows.indexOfFirst { it.id == scrollToItemId }
            if (index != -1) {
                // Adjust for the header at index 0
                lazyListState.animateScrollToItem(index + 1)
                onScrollComplete()
            }
        }
    }

    if (tagRows.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Text(
                    text = "You haven't followed any tags yet. Follow tags to show them as rows on your home screen.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        haptic.perform(StashHapticFeedbackType.Selection)
                        onNavigateToTags()
                    },
                    shape = RoundedCornerShape(StashTokens.Radius.Button)
                ) {
                    Text("Go to Tags")
                }
            }
        }
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp + topPadding, end = 16.dp, bottom = 16.dp + bottomPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Select followed tags to include on your home screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(tagRows, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { /* Checkbox/Switch handles interaction */ },
                            onLongClick = {
                                haptic.perform(StashHapticFeedbackType.Heavy)
                                onTagLongPress(HomeSectionIds.getTagIdFromSectionId(item.id))
                            }
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(StashTokens.Radius.Card)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Switch(
                            checked = !item.isHidden,
                            onCheckedChange = {
                                haptic.perform(StashHapticFeedbackType.Selection)
                                onToggleVisibility(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
