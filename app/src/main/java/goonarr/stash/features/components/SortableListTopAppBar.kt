package goonarr.stash.features.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTokens
import goonarr.stash.tonal
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Interface for sort options that can be displayed in the top app bar.
 */
interface SortOption {
    val displayName: String
    val icon: ImageVector?
    val iconRes: Int?
}

/**
 * Interface for layout options that can be displayed in the top app bar.
 */
interface LayoutOption {
    val displayName: String
    val icon: ImageVector
}

/**
 * A reusable top app bar for list screens with sorting and search functionality.
 *
 * @param title The title displayed in the app bar
 * @param sortOptions List of available sort options
 * @param currentSortType Flow of the currently selected sort option
 * @param currentSortDirection Flow of the current sort direction ("ASC" or "DESC")
 * @param defaultSortType The default sort option to show before flow emits
 * @param onUpdateSortType Callback when sort type is changed
 * @param onUpdateSortDirection Callback when sort direction is changed
 * @param searchQuery Current search query (null hides search)
 * @param onSearchQueryChange Callback when search query changes (null hides search)
 * @param searchPlaceholder Placeholder text for search field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : SortOption, L : LayoutOption> SortableListTopAppBar(
    title: String,
    count: Int? = null,
    sortOptions: List<T>,
    currentSortType: Flow<T>,
    currentSortDirection: Flow<String>,
    defaultSortType: T,
    onUpdateSortType: (T) -> Unit,
    onUpdateSortDirection: (String) -> Unit,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    searchPlaceholder: String = "Search...",
    // Layout options (optional)
    layoutOptions: List<L>? = null,
    currentLayoutType: Flow<L>? = null,
    defaultLayoutType: L? = null,
    onUpdateLayoutType: ((L) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    val sortType by currentSortType.collectAsState(initial = defaultSortType)
    val sortDirection by currentSortDirection.collectAsState(initial = "DESC")
    val layoutType by (currentLayoutType ?: flowOf(defaultLayoutType)).collectAsState(initial = defaultLayoutType)
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val overlappedFraction = scrollBehavior?.state?.overlappedFraction ?: 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .onGloballyPositioned { coords ->
                scrollBehavior?.state?.heightOffsetLimit = -coords.size.height.toFloat()
            }
            .padding(top = statusBarPadding)
    ) {
        // Create dynamic search placeholder: "Search 123 Studios"
        val dynamicPlaceholder = remember(title, count) {
            buildString {
                append("Search ")
                if (count != null) {
                    append(count)
                    append(" ")
                }
                append(title)
            }
        }

        // Single-Row Compact Header
        FilterRow(
            onBackClick = onBackClick,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            searchPlaceholder = dynamicPlaceholder,
            sortType = sortType,
            sortDirection = sortDirection,
            sortOptions = sortOptions,
            onUpdateSortType = onUpdateSortType,
            onUpdateSortDirection = onUpdateSortDirection,
            layoutType = layoutType,
            layoutOptions = layoutOptions,
            onUpdateLayoutType = onUpdateLayoutType,
            overlappedFraction = overlappedFraction,
            actions = actions
        )
    }
}

// TitleRow removed as it's now merged into FilterRow for ultra-compact design.

@Composable
internal fun <T : SortOption, L : LayoutOption> FilterRow(
    onBackClick: (() -> Unit)?,
    searchQuery: String,
    onSearchQueryChange: ((String) -> Unit)?,
    searchPlaceholder: String,
    sortType: T,
    sortDirection: String,
    sortOptions: List<T>,
    onUpdateSortType: (T) -> Unit,
    onUpdateSortDirection: (String) -> Unit,
    layoutType: L?,
    layoutOptions: List<L>?,
    onUpdateLayoutType: ((L) -> Unit)?,
    overlappedFraction: Float,
    actions: @Composable RowScope.() -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showLayoutMenu by remember { mutableStateOf(false) }
    val haptic = LocalStashHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                // Match content horizontal padding (16.dp) for alignment
                start = 16.dp,
                end = 16.dp,
                // Small padding from status bar
                top = 8.dp,
                bottom = 8.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button (if provided)
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        // Sort Chip
        Box {
            FilterChip(
                label = sortType.displayName,
                icon = if (sortDirection == "ASC") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                isExpanded = showSortMenu,
                onClick = { showSortMenu = true },
                overlappedFraction = overlappedFraction
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(StashTokens.Radius.Card)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Sort By",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = { },
                    enabled = false
                )

                sortOptions.forEach { option ->
                    val isSelected = sortType == option
                    DropdownMenuItem(
                        text = {
                            Text(
                                option.displayName,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Unspecified
                            )
                        },
                        leadingIcon = {
                            val tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                            if (option.icon != null) {
                                Icon(option.icon!!, null, tint = tint)
                            } else if (option.iconRes != null) {
                                Icon(painterResource(option.iconRes!!), null, tint = tint)
                            }
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        },
                        onClick = {
                            haptic.perform(StashHapticFeedbackType.Selection)
                            onUpdateSortType(option)
                            showSortMenu = false
                        }
                    )
                }

                HorizontalDivider()

                DropdownMenuItem(
                    text = {
                        Text(
                            "Sort Direction",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = { },
                    enabled = false
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Ascending",
                            color = if (sortDirection == "ASC") MaterialTheme.colorScheme.secondary else Color.Unspecified
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ArrowUpward,
                            null,
                            tint = if (sortDirection == "ASC") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = {
                        if (sortDirection == "ASC") {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    onClick = {
                        haptic.perform(StashHapticFeedbackType.Selection)
                        onUpdateSortDirection("ASC")
                        showSortMenu = false
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Descending",
                            color = if (sortDirection == "DESC") MaterialTheme.colorScheme.secondary else Color.Unspecified
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ArrowDownward,
                            null,
                            tint = if (sortDirection == "DESC") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = {
                        if (sortDirection == "DESC") {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    onClick = {
                        haptic.perform(StashHapticFeedbackType.Selection)
                        onUpdateSortDirection("DESC")
                        showSortMenu = false
                    }
                )
            }
        }

        // Search Pill (always visible)
        if (onSearchQueryChange != null) {
            SearchPill(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = searchPlaceholder,
                modifier = Modifier.weight(1f),
                overlappedFraction = overlappedFraction
            )
        }

        // Layout Chip (if layout options provided)
        if (layoutOptions != null && layoutType != null && onUpdateLayoutType != null) {
            Box {
                FilterChip(
                    // Icon only for layout
                    label = null,
                    icon = layoutType.icon,
                    isExpanded = showLayoutMenu,
                    onClick = { showLayoutMenu = true },
                    overlappedFraction = overlappedFraction
                )

                DropdownMenu(
                    expanded = showLayoutMenu,
                    onDismissRequest = { showLayoutMenu = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(StashTokens.Radius.Card)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Layout",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = { },
                        enabled = false
                    )

                    layoutOptions.forEach { option ->
                        val isSelected = layoutType == option
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.displayName,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Unspecified
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    option.icon,
                                    null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            },
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Selection)
                                onUpdateLayoutType(option)
                                showLayoutMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Custom actions slot
        Row { actions() }
    }
}

@Composable
internal fun SearchPill(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    overlappedFraction: Float = 0f
) {
    val haptic = LocalStashHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }

    // Border color changes to solid primary when focused, faded primary when unfocused
    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    }

    val backgroundColor = MaterialTheme.colorScheme.surface.tonal(
        fraction = (overlappedFraction * 0.20f).coerceIn(0f, 1f),
        towards = MaterialTheme.colorScheme.primary
    )

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .height(44.dp)
            .background(
                color = backgroundColor,
                // Fully rounded pill
                shape = RoundedCornerShape(StashTokens.Radius.Button)
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(StashTokens.Radius.Button)
            )
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused && !isFocused) {
                    haptic.perform(StashHapticFeedbackType.Selection)
                }
                isFocused = it.isFocused
            },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = StashTokens.Alpha.TextSecondary
                    ),
                    modifier = Modifier.size(18.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = StashTokens.Alpha.TextSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }

                // Clear button (animated)
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    )
}
