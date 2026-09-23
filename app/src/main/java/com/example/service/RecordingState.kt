package com.example.service

sealed class RecordingStatus {
    object Idle : RecordingStatus()
    data class Countdown(val secondsRemaining: Int) : RecordingStatus()
    data class Recording(
        val durationMs: Long,
        val audioAmplitudes: List<Float> = emptyList()
    ) : RecordingStatus()
    data class Paused(val durationMs: Long) : RecordingStatus()
    data class Saving(val message: String = "Finalizing video...") : RecordingStatus()
}
