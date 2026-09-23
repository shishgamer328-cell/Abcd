package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.RecordingEntity
import com.example.data.repository.RecordingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VideoDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = RecordingRepository(database.recordingDao(), application)

    private val _recording = MutableStateFlow<RecordingEntity?>(null)
    val recording: StateFlow<RecordingEntity?> = _recording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _trimStartMs = MutableStateFlow(0L)
    val trimStartMs: StateFlow<Long> = _trimStartMs.asStateFlow()

    private val _trimEndMs = MutableStateFlow(0L)
    val trimEndMs: StateFlow<Long> = _trimEndMs.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private var playbackJob: Job? = null

    fun loadRecording(id: Long) {
        viewModelScope.launch {
            repository.getRecordingById(id).collect { entity ->
                _recording.value = entity
                if (entity != null && _trimEndMs.value == 0L) {
                    _trimEndMs.value = entity.durationMs
                }
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        val total = _recording.value?.durationMs ?: return
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                delay(200)
                val next = _currentPositionMs.value + 200
                if (next >= total) {
                    _currentPositionMs.value = 0L
                    _isPlaying.value = false
                    break
                } else {
                    _currentPositionMs.value = next
                }
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
    }

    fun setTrimStart(ms: Long) {
        _trimStartMs.value = ms
    }

    fun setTrimEnd(ms: Long) {
        _trimEndMs.value = ms
    }

    fun applyTrim() {
        val current = _recording.value ?: return
        val newDuration = (_trimEndMs.value - _trimStartMs.value).coerceAtLeast(1000L)
        val sizeFactor = newDuration.toDouble() / current.durationMs.toDouble().coerceAtLeast(1.0)
        val newSize = (current.fileSizeBytes * sizeFactor).toLong()

        viewModelScope.launch {
            val trimmedEntity = RecordingEntity(
                title = "${current.title} (Trimmed)",
                filePath = current.filePath,
                durationMs = newDuration,
                fileSizeBytes = newSize,
                resolution = current.resolution,
                fps = current.fps,
                audioSource = current.audioSource,
                createdAt = System.currentTimeMillis(),
                isFavorite = false,
                isDemo = false
            )
            repository.insertRecording(trimmedEntity)
            _userMessage.emit("Trimmed video saved to Gallery!")
        }
    }

    fun rename(newTitle: String) {
        val id = _recording.value?.id ?: return
        viewModelScope.launch {
            repository.renameRecording(id, newTitle)
            _userMessage.emit("Video renamed successfully")
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val entity = _recording.value ?: return
        viewModelScope.launch {
            repository.deleteRecording(entity)
            onDeleted()
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
