package goonarr.stash.features.tags.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.SearchTopAppBar
import goonarr.stash.features.tags.card.TagRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagListScreen(
    navController: NavController,
    onTagClick: (String) -> Unit,
    onTagsChanged: () -> Unit = {},
    resetSearch: Boolean = false,
    onResetSearchConsumed: () -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    viewModel: TagListViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val tags by viewModel.tags.collectAsState()
    val followedTags by viewModel.followedTags.collectAsState(initial = emptyList())
    val favoriteTags by viewModel.favoriteTags.collectAsState()
    val followedTagIds by viewModel.followedTagIds.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Reset search query when returning from tag deletion
    LaunchedEffect(resetSearch) {
        if (resetSearch) {
            viewModel.onSearchQueryChanged("")
            onResetSearchConsumed()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            SearchTopAppBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onBack = {
                    onTagsChanged()
                    navController.popBackStack()
                },
                onClear = { viewModel.onSearchQueryChanged("") },
                title = "Tags",
                count = tags.size,
                scrollBehavior = scrollBehavior,
                shouldCloseSearch = resetSearch,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            TagListContent(
                tags = tags,
                followedTags = followedTags,
                favoriteTags = favoriteTags,
                followedTagIds = followedTagIds.toSet(),
                onTagClick = { tag -> onTagClick(tag.id) },
                onToggleFollow = { tag -> viewModel.toggleFollow(tag) },
                isSearchActive = searchQuery.isNotEmpty(),
                listState = listState,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
                contentPadding = paddingValues
            )
        }
    }
}

@Composable
fun TagListContent(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    followedTags: List<Tag>,
    favoriteTags: List<Tag>,
    followedTagIds: Set<String>,
    onTagClick: (Tag) -> Unit,
    onToggleFollow: (Tag) -> Unit,
    isSearchActive: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Followed Tags Section - always visible if not searching
        if (followedTags.isNotEmpty() && !isSearchActive) {
            item {
                Text(
                    text = "Tags Added to Home Screen",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(followedTags, key = { "followed_${it.id}" }) { tag ->
                TagRow(
                    tag = tag,
                    onClick = { onTagClick(tag) },
                    onUnfollow = { onToggleFollow(tag) },
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedTransitionScope = sharedTransitionScope
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Favorite Tags Section - visible if there are favorites and not searching
        if (favoriteTags.isNotEmpty() && !isSearchActive) {
            item {
                Text(
                    text = "Favorite Tags",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(favoriteTags, key = { "favorite_${it.id}" }) { tag ->
                TagRow(
                    tag = tag,
                    onClick = { onTagClick(tag) },
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedTransitionScope = sharedTransitionScope
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // All Tags Section
        item {
            Text(
                text = if (isSearchActive) "Search Results" else "All Tags",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(tags, key = { "all_${it.id}" }) { tag ->
            TagRow(
                tag = tag,
                onClick = { onTagClick(tag) },
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope
            )
        }
    }
}
