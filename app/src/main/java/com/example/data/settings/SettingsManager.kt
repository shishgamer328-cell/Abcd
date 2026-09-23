package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RecordingConfig(
    val resolution: String = "1080p",
    val fps: Int = 60,
    val bitrate: String = "12 Mbps",
    val audioSource: String = "Microphone + Internal",
    val orientation: String = "Auto",
    val countdownSeconds: Int = 3,
    val showFloatingControls: Boolean = true,
    val showTouches: Boolean = false,
    val showFacecam: Boolean = false,
    val shakeToStop: Boolean = false,
    val darkTheme: Boolean = true
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("screen_recorder_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<RecordingConfig> = _config.asStateFlow()

    private fun loadConfig(): RecordingConfig {
        return RecordingConfig(
            resolution = prefs.getString("resolution", "1080p") ?: "1080p",
            fps = prefs.getInt("fps", 60),
            bitrate = prefs.getString("bitrate", "12 Mbps") ?: "12 Mbps",
            audioSource = prefs.getString("audio_source", "Microphone + Internal") ?: "Microphone + Internal",
            orientation = prefs.getString("orientation", "Auto") ?: "Auto",
            countdownSeconds = prefs.getInt("countdown_seconds", 3),
            showFloatingControls = prefs.getBoolean("show_floating", true),
            showTouches = prefs.getBoolean("show_touches", false),
            showFacecam = prefs.getBoolean("show_facecam", false),
            shakeToStop = prefs.getBoolean("shake_to_stop", false),
            darkTheme = prefs.getBoolean("dark_theme", true)
        )
    }

    fun updateResolution(resolution: String) {
        prefs.edit().putString("resolution", resolution).apply()
        _config.value = _config.value.copy(resolution = resolution)
    }

    fun updateFps(fps: Int) {
        prefs.edit().putInt("fps", fps).apply()
        _config.value = _config.value.copy(fps = fps)
    }

    fun updateBitrate(bitrate: String) {
        prefs.edit().putString("bitrate", bitrate).apply()
        _config.value = _config.value.copy(bitrate = bitrate)
    }

    fun updateAudioSource(source: String) {
        prefs.edit().putString("audio_source", source).apply()
        _config.value = _config.value.copy(audioSource = source)
    }

    fun updateOrientation(orientation: String) {
        prefs.edit().putString("orientation", orientation).apply()
        _config.value = _config.value.copy(orientation = orientation)
    }

    fun updateCountdown(seconds: Int) {
        prefs.edit().putInt("countdown_seconds", seconds).apply()
        _config.value = _config.value.copy(countdownSeconds = seconds)
    }

    fun toggleFloatingControls(enabled: Boolean) {
        prefs.edit().putBoolean("show_floating", enabled).apply()
        _config.value = _config.value.copy(showFloatingControls = enabled)
    }

    fun toggleTouches(enabled: Boolean) {
        prefs.edit().putBoolean("show_touches", enabled).apply()
        _config.value = _config.value.copy(showTouches = enabled)
    }

    fun toggleFacecam(enabled: Boolean) {
        prefs.edit().putBoolean("show_facecam", enabled).apply()
        _config.value = _config.value.copy(showFacecam = enabled)
    }

    fun toggleShakeToStop(enabled: Boolean) {
        prefs.edit().putBoolean("shake_to_stop", enabled).apply()
        _config.value = _config.value.copy(shakeToStop = enabled)
    }

    fun toggleDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
        _config.value = _config.value.copy(darkTheme = isDark)
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
