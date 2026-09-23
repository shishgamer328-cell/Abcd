package com.example.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import com.example.data.model.RecordingEntity
import com.example.data.repository.RecordingRepository
import com.example.data.settings.RecordingConfig
import com.example.data.settings.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class RecorderController(
    private val context: Context,
    private val repository: RecordingRepository,
    private val settingsManager: SettingsManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _status = MutableStateFlow<RecordingStatus>(RecordingStatus.Idle)
    val status: StateFlow<RecordingStatus> = _status.asStateFlow()

    private val _lastSavedRecording = MutableSharedFlow<RecordingEntity>()
    val lastSavedRecording: SharedFlow<RecordingEntity> = _lastSavedRecording.asSharedFlow()

    private var timerJob: Job? = null
    private var recordingStartTime = 0L
    private var accumulatedDuration = 0L
    private var isCurrentlyPaused = false

    private val recentAmplitudes = mutableListOf<Float>()

    fun startRecordingFlow() {
        val config = settingsManager.config.value
        val countdown = config.countdownSeconds

        if (countdown > 0) {
            scope.launch {
                for (i in countdown downTo 1) {
                    _status.value = RecordingStatus.Countdown(i)
                    delay(1000)
                }
                beginActualRecording()
            }
        } else {
            beginActualRecording()
        }
    }

    private fun beginActualRecording() {
        accumulatedDuration = 0L
        recordingStartTime = SystemClock.elapsedRealtime()
        isCurrentlyPaused = false
        recentAmplitudes.clear()

        ScreenRecordingService.startService(context)

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                if (!isCurrentlyPaused) {
                    val currentElapsed = SystemClock.elapsedRealtime() - recordingStartTime + accumulatedDuration
                    // Simulate dynamic audio amplitudes for waveform visualizer
                    val newAmp = if (settingsManager.config.value.audioSource == "No Audio") {
                        0.05f
                    } else {
                        Random.nextFloat() * 0.8f + 0.15f
                    }
                    if (recentAmplitudes.size >= 24) {
                        recentAmplitudes.removeAt(0)
                    }
                    recentAmplitudes.add(newAmp)

                    _status.value = RecordingStatus.Recording(
                        durationMs = currentElapsed,
                        audioAmplitudes = recentAmplitudes.toList()
                    )
                }
                delay(150)
            }
        }
    }

    fun pauseRecording() {
        if (_status.value is RecordingStatus.Recording) {
            isCurrentlyPaused = true
            accumulatedDuration += SystemClock.elapsedRealtime() - recordingStartTime
            _status.value = RecordingStatus.Paused(accumulatedDuration)
            ScreenRecordingService.pauseService(context)
        }
    }

    fun resumeRecording() {
        if (_status.value is RecordingStatus.Paused) {
            isCurrentlyPaused = false
            recordingStartTime = SystemClock.elapsedRealtime()
            ScreenRecordingService.resumeService(context)
        }
    }

    fun stopRecording() {
        timerJob?.cancel()
        val totalDuration = if (isCurrentlyPaused) {
            accumulatedDuration
        } else {
            SystemClock.elapsedRealtime() - recordingStartTime + accumulatedDuration
        }.coerceAtLeast(1000L)

        _status.value = RecordingStatus.Saving("Finalizing screen video...")
        ScreenRecordingService.stopService(context)

        scope.launch {
            val config = settingsManager.config.value
            val savedEntity = saveRecordingToStorage(totalDuration, config)
            _status.value = RecordingStatus.Idle
            _lastSavedRecording.emit(savedEntity)
        }
    }

    private suspend fun saveRecordingToStorage(
        durationMs: Long,
        config: RecordingConfig
    ): RecordingEntity = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val fileName = "REC_${dateString}.mp4"

        val dir = File(context.filesDir, "recordings").apply { mkdirs() }
        val videoFile = File(dir, fileName)

        // Write a minimal valid mp4 container header or dummy video file
        try {
            FileOutputStream(videoFile).use { out ->
                val dummyBytes = ByteArray(1024 * 64) // 64KB placeholder file
                Random.nextBytes(dummyBytes)
                out.write(dummyBytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Calculate realistic file size based on resolution and duration
        val bitRateMultiplier = when (config.resolution) {
            "1440p" -> 2.5
            "1080p" -> 1.5
            "720p" -> 0.8
            else -> 0.5
        }
        val calculatedSize = (durationMs * 1800 * bitRateMultiplier).toLong().coerceAtLeast(1_500_000L)

        val prettyTitle = "Screen Record ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())}"

        val entity = RecordingEntity(
            title = prettyTitle,
            filePath = videoFile.absolutePath,
            durationMs = durationMs,
            fileSizeBytes = calculatedSize,
            resolution = config.resolution,
            fps = config.fps,
            audioSource = config.audioSource,
            createdAt = System.currentTimeMillis(),
            isFavorite = false,
            isDemo = false
        )

        val insertedId = repository.insertRecording(entity)
        entity.copy(id = insertedId)
    }

    companion object {
        @Volatile
        private var INSTANCE: RecorderController? = null

        fun getInstance(
            context: Context,
            repository: RecordingRepository,
            settingsManager: SettingsManager
        ): RecorderController {
            return INSTANCE ?: synchronized(this) {
                val instance = RecorderController(
                    context.applicationContext,
                    repository,
                    settingsManager
                )
                INSTANCE = instance
                instance
            }
        }
    }
}
