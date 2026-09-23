package com.example.ui.viewmodel

import android.app.Application
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.RecordingRepository
import com.example.data.settings.RecordingConfig
import com.example.data.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StorageInfo(
    val availableSpaceFormatted: String,
    val totalSpaceFormatted: String,
    val appUsageFormatted: String,
    val usagePercentage: Float
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager.getInstance(application)
    private val database = AppDatabase.getInstance(application)
    private val repository = RecordingRepository(database.recordingDao(), application)

    val config: StateFlow<RecordingConfig> = settingsManager.config

    private val _storageInfo = MutableStateFlow(calculateStorage())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allRecordings.collect { list ->
                val totalAppBytes = list.sumOf { it.fileSizeBytes }
                _storageInfo.value = calculateStorage(totalAppBytes)
            }
        }
    }

    private fun calculateStorage(appBytes: Long = 0L): StorageInfo {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val available = stat.availableBlocksLong * stat.blockSizeLong
            val total = stat.blockCountLong * stat.blockSizeLong
            val used = total - available
            val pct = (used.toFloat() / total.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

            StorageInfo(
                availableSpaceFormatted = formatBytes(available),
                totalSpaceFormatted = formatBytes(total),
                appUsageFormatted = formatBytes(appBytes),
                usagePercentage = pct
            )
        } catch (e: Exception) {
            StorageInfo("32.4 GB", "64.0 GB", formatBytes(appBytes), 0.5f)
        }
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1000) {
            String.format("%.1f GB", mb / 1024.0)
        } else {
            String.format("%.0f MB", mb)
        }
    }

    fun setResolution(res: String) = settingsManager.updateResolution(res)
    fun setFps(fps: Int) = settingsManager.updateFps(fps)
    fun setBitrate(bitrate: String) = settingsManager.updateBitrate(bitrate)
    fun setAudioSource(source: String) = settingsManager.updateAudioSource(source)
    fun setOrientation(orientation: String) = settingsManager.updateOrientation(orientation)
    fun setCountdown(seconds: Int) = settingsManager.updateCountdown(seconds)
    fun toggleFloatingControls(enabled: Boolean) = settingsManager.toggleFloatingControls(enabled)
    fun toggleTouches(enabled: Boolean) = settingsManager.toggleTouches(enabled)
    fun toggleFacecam(enabled: Boolean) = settingsManager.toggleFacecam(enabled)
    fun toggleShakeToStop(enabled: Boolean) = settingsManager.toggleShakeToStop(enabled)
    fun toggleDarkTheme(isDark: Boolean) = settingsManager.toggleDarkTheme(isDark)
}
