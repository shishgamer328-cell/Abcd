package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.RecordingEntity
import com.example.data.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GallerySort {
    NEWEST,
    OLDEST,
    DURATION_HIGH,
    SIZE_HIGH
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = RecordingRepository(database.recordingDao(), application)

    val searchQuery = MutableStateFlow("")
    val sortOption = MutableStateFlow(GallerySort.NEWEST)
    val isGridView = MutableStateFlow(true)
    val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val recordings: StateFlow<List<RecordingEntity>> = combine(
        repository.allRecordings,
        searchQuery,
        sortOption
    ) { all, query, sort ->
        var filtered = if (query.isBlank()) {
            all
        } else {
            all.filter { it.title.contains(query, ignoreCase = true) }
        }

        filtered = when (sort) {
            GallerySort.NEWEST -> filtered.sortedByDescending { it.createdAt }
            GallerySort.OLDEST -> filtered.sortedBy { it.createdAt }
            GallerySort.DURATION_HIGH -> filtered.sortedByDescending { it.durationMs }
            GallerySort.SIZE_HIGH -> filtered.sortedByDescending { it.fileSizeBytes }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSort(sort: GallerySort) {
        sortOption.value = sort
    }

    fun toggleViewMode() {
        isGridView.value = !isGridView.value
    }

    fun toggleSelection(id: Long) {
        val current = selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        selectedIds.value = current
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val ids = selectedIds.value.toList()
        viewModelScope.launch {
            repository.deleteByIds(ids)
            clearSelection()
        }
    }

    fun deleteRecording(recording: RecordingEntity) {
        viewModelScope.launch {
            repository.deleteRecording(recording)
        }
    }

    fun renameRecording(id: Long, newTitle: String) {
        viewModelScope.launch {
            repository.renameRecording(id, newTitle)
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }
}
