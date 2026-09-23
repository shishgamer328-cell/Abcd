package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val resolution: String = "1080p",
    val fps: Int = 60,
    val audioSource: String = "Microphone + Internal",
    val createdAt: Long = System.currentTimeMillis(),
    val thumbnailUri: String? = null,
    val isFavorite: Boolean = false,
    val isDemo: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            val mb = fileSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1000) {
                val gb = mb / 1024.0
                String.format("%.2f GB", gb)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}
