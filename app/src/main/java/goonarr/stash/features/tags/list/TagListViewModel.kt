package goonarr.stash.features.tags.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Tag
import goonarr.stash.repositories.TagRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TagListViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // All tags loaded once
    private val _allTags = MutableStateFlow<List<Tag>>(emptyList())

    // Filtered tags based on search query
    val tags: StateFlow<List<Tag>> = combine(_allTags, _searchQuery) { allTags, query ->
        if (query.isEmpty()) {
            allTags
        } else {
            allTags.filter { tag ->
                tag.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Followed tags from repository
    @OptIn(ExperimentalCoroutinesApi::class)
    val followedTags: Flow<List<Tag>> = tagRepository.followedTagIds.flatMapLatest { ids ->
        if (ids.isEmpty()) {
            flowOf(emptyList())
        } else {
            tagRepository.getTags(ids)
        }
    }

    val followedTagIds: Flow<List<String>> = tagRepository.followedTagIds

    // Favorite tags - tags where favorite == true
    val favoriteTags: StateFlow<List<Tag>> = _allTags.map { allTags ->
        allTags.filter { it.favorite == true }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadAllTags()
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allTags = tagRepository.getAllTags()
                _allTags.value = allTags
            } catch (e: Exception) {
                // Handle error - could add error state if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFollow(tag: Tag) {
        viewModelScope.launch {
            val followedIds = tagRepository.followedTagIds.first()
            if (followedIds.contains(tag.id)) {
                tagRepository.removeFollowedTag(tag.id)
            } else {
                tagRepository.addFollowedTag(tag.id)
            }
        }
    }

    fun isFollowed(tagId: String): Flow<Boolean> {
        return tagRepository.followedTagIds.map { it.contains(tagId) }
    }
}
