package com.example.data.repository

import android.content.Context
import com.example.data.local.RecordingDao
import com.example.data.model.RecordingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RecordingRepository(
    private val recordingDao: RecordingDao,
    private val context: Context
) {
    val allRecordings: Flow<List<RecordingEntity>> = recordingDao.getAllRecordings()
    val recentRecordings: Flow<List<RecordingEntity>> = recordingDao.getRecentRecordings()

    fun searchRecordings(query: String): Flow<List<RecordingEntity>> =
        recordingDao.searchRecordings(query)

    fun getRecordingById(id: Long): Flow<RecordingEntity?> =
        recordingDao.getRecordingById(id)

    suspend fun getRecordingByIdSync(id: Long): RecordingEntity? = withContext(Dispatchers.IO) {
        recordingDao.getRecordingByIdSync(id)
    }

    suspend fun insertRecording(recording: RecordingEntity): Long = withContext(Dispatchers.IO) {
        recordingDao.insertRecording(recording)
    }

    suspend fun renameRecording(id: Long, newTitle: String) = withContext(Dispatchers.IO) {
        recordingDao.renameRecording(id, newTitle)
    }

    suspend fun toggleFavorite(id: Long) = withContext(Dispatchers.IO) {
        recordingDao.toggleFavorite(id)
    }

    suspend fun deleteRecording(recording: RecordingEntity) = withContext(Dispatchers.IO) {
        recordingDao.deleteRecording(recording)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        recordingDao.deleteById(id)
    }

    suspend fun deleteByIds(ids: List<Long>) = withContext(Dispatchers.IO) {
        recordingDao.deleteByIds(ids)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = recordingDao.getCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val sampleRecordings = listOf(
                RecordingEntity(
                    title = "Battle Royale Gameplay - Clutch Win",
                    filePath = "sample://video_gameplay.mp4",
                    durationMs = 184000L, // 3m 04s
                    fileSizeBytes = 78_400_000L, // ~78 MB
                    resolution = "1080p",
                    fps = 60,
                    audioSource = "Microphone + Internal",
                    createdAt = now - (2 * 60 * 60 * 1000L), // 2 hours ago
                    isFavorite = true,
                    isDemo = true
                ),
                RecordingEntity(
                    title = "Android App Navigation Tutorial",
                    filePath = "sample://video_tutorial.mp4",
                    durationMs = 125000L, // 2m 05s
                    fileSizeBytes = 42_100_000L, // ~42 MB
                    resolution = "1080p",
                    fps = 60,
                    audioSource = "Microphone",
                    createdAt = now - (24 * 60 * 60 * 1000L), // 1 day ago
                    isFavorite = false,
                    isDemo = true
                ),
                RecordingEntity(
                    title = "Speed Test & Benchmark Run",
                    filePath = "sample://video_benchmark.mp4",
                    durationMs = 45000L, // 45s
                    fileSizeBytes = 18_900_000L, // ~18 MB
                    resolution = "720p",
                    fps = 30,
                    audioSource = "Internal Audio",
                    createdAt = now - (2 * 24 * 60 * 60 * 1000L), // 2 days ago
                    isFavorite = false,
                    isDemo = true
                ),
                RecordingEntity(
                    title = "Instagram Story Walkthrough",
                    filePath = "sample://video_walkthrough.mp4",
                    durationMs = 64000L, // 1m 04s
                    fileSizeBytes = 25_600_000L,
                    resolution = "1080p",
                    fps = 60,
                    audioSource = "Microphone + Internal",
                    createdAt = now - (4 * 24 * 60 * 60 * 1000L),
                    isFavorite = true,
                    isDemo = true
                )
            )
            recordingDao.insertAll(sampleRecordings)
        }
    }
}
