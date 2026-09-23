package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.RecordingEntity
import com.example.data.repository.RecordingRepository
import com.example.data.settings.RecordingConfig
import com.example.data.settings.SettingsManager
import com.example.service.RecorderController
import com.example.service.RecordingStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = RecordingRepository(database.recordingDao(), application)
    private val settingsManager = SettingsManager.getInstance(application)
    private val recorderController = RecorderController.getInstance(application, repository, settingsManager)

    val recordingStatus: StateFlow<RecordingStatus> = recorderController.status
    val recordingConfig: StateFlow<RecordingConfig> = settingsManager.config

    val recentRecordings: StateFlow<List<RecordingEntity>> = repository.recentRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _isFloatingOverlayVisible = MutableStateFlow(false)
    val isFloatingOverlayVisible: StateFlow<Boolean> = _isFloatingOverlayVisible.asStateFlow()

    private val _isFacecamActive = MutableStateFlow(false)
    val isFacecamActive: StateFlow<Boolean> = _isFacecamActive.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        viewModelScope.launch {
            recorderController.lastSavedRecording.collect { saved ->
                _userMessage.emit("Recording saved: ${saved.title}")
            }
        }
    }

    fun startRecording() {
        recorderController.startRecordingFlow()
    }

    fun pauseRecording() {
        recorderController.pauseRecording()
    }

    fun resumeRecording() {
        recorderController.resumeRecording()
    }

    fun stopRecording() {
        recorderController.stopRecording()
    }

    fun setResolution(res: String) {
        settingsManager.updateResolution(res)
    }

    fun setFps(fps: Int) {
        settingsManager.updateFps(fps)
    }

    fun setAudioSource(source: String) {
        settingsManager.updateAudioSource(source)
    }

    fun setOrientation(orientation: String) {
        settingsManager.updateOrientation(orientation)
    }

    fun toggleFloatingControls(show: Boolean) {
        _isFloatingOverlayVisible.value = show
        settingsManager.toggleFloatingControls(show)
    }

    fun toggleFacecam(show: Boolean) {
        _isFacecamActive.value = show
        settingsManager.toggleFacecam(show)
    }
}
