package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.RecordCyan
import com.example.ui.theme.RecordRed
import com.example.ui.viewmodel.VideoDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    recordingId: Long,
    viewModel: VideoDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recording by viewModel.recording.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val trimStartMs by viewModel.trimStartMs.collectAsStateWithLifecycle()
    val trimEndMs by viewModel.trimEndMs.collectAsStateWithLifecycle()

    var showTrimEditor by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(recordingId) {
        viewModel.loadRecording(recordingId)
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val currentRec = recording

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentRec?.title ?: "Video Preview", maxLines = 1, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("player_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        renameInput = currentRec?.title ?: ""
                        showRenameDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename")
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/mp4"
                            putExtra(Intent.EXTRA_SUBJECT, currentRec?.title)
                            putExtra(Intent.EXTRA_TEXT, "Watch screen recording: ${currentRec?.title}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share recording"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RecordRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (currentRec == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading video...")
            }
        } else {
            val totalDurationMs = currentRec.durationMs.coerceAtLeast(1000L)
            val currentPosFormatted = formatMs(currentPositionMs)
            val totalPosFormatted = formatMs(totalDurationMs)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Video Player Viewport Container
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .testTag("video_player_viewport")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Player background aesthetic
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFF1E293B), Color(0xFF020617))
                                        )
                                    )
                            )

                            // Playback Center Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.seekTo((currentPositionMs - 10000).coerceAtLeast(0L))
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Replay10,
                                        contentDescription = "Rewind 10s",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = RecordRed,
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { viewModel.togglePlayPause() }
                                        .testTag("player_play_pause_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.seekTo((currentPositionMs + 10000).coerceAtMost(totalDurationMs))
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Forward10,
                                        contentDescription = "Forward 10s",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            // Resolution badge in corner
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = RecordCyan.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${currentRec.resolution} • ${currentRec.fps} FPS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Player Timeline Scrubber
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f),
                            onValueChange = { fraction ->
                                viewModel.seekTo((fraction * totalDurationMs).toLong())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = RecordRed,
                                activeTrackColor = RecordRed,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("player_timeline_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentPosFormatted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = totalPosFormatted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Video Trim Tool Toggle
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = RecordRed)
                                    Column {
                                        Text("Trim & Edit Video", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Cut unwanted start or end segments", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Button(
                                    onClick = { showTrimEditor = !showTrimEditor },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (showTrimEditor) RecordRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Text(
                                        text = if (showTrimEditor) "Hide" else "Open Editor",
                                        fontSize = 12.sp,
                                        color = if (showTrimEditor) RecordRed else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            AnimatedVisibility(visible = showTrimEditor) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    Text(
                                        text = "Trim Range: ${formatMs(trimStartMs)} – ${formatMs(trimEndMs)}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = RecordCyan
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Range Slider
                                    RangeSlider(
                                        value = (trimStartMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)..(trimEndMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f),
                                        onValueChange = { range ->
                                            viewModel.setTrimStart((range.start * totalDurationMs).toLong())
                                            viewModel.setTrimEnd((range.endInclusive * totalDurationMs).toLong())
                                        },
                                        colors = SliderDefaults.colors(
                                            thumbColor = RecordCyan,
                                            activeTrackColor = RecordCyan
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.applyTrim()
                                            showTrimEditor = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                                        modifier = Modifier.fillMaxWidth().testTag("apply_trim_button")
                                    ) {
                                        Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Export Trimmed Video", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Video File Specifications
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("File Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            DetailRow("Title", currentRec.title)
                            DetailRow("Duration", currentRec.formattedDuration)
                            DetailRow("Resolution", "${currentRec.resolution} (${currentRec.fps} FPS)")
                            DetailRow("File Size", currentRec.formattedSize)
                            DetailRow("Audio Source", currentRec.audioSource)
                            DetailRow(
                                "Recorded On",
                                SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault()).format(Date(currentRec.createdAt))
                            )
                            DetailRow("Storage Path", currentRec.filePath)
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Recording") },
            text = {
                TextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.rename(renameInput)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Recording") },
            text = { Text("Delete this recording permanently from device?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete { onBack() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
