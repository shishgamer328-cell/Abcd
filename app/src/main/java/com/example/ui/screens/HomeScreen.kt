package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.RecordingEntity
import com.example.service.RecordingStatus
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.CountdownOverlay
import com.example.ui.components.FacecamOverlay
import com.example.ui.components.FloatingWidgetOverlay
import com.example.ui.components.RecordingCard
import com.example.ui.theme.RecordAmber
import com.example.ui.theme.RecordCyan
import com.example.ui.theme.RecordGreen
import com.example.ui.theme.RecordRed
import com.example.ui.theme.RecordRedDark
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlayer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status by viewModel.recordingStatus.collectAsStateWithLifecycle()
    val config by viewModel.recordingConfig.collectAsStateWithLifecycle()
    val recentRecordings by viewModel.recentRecordings.collectAsStateWithLifecycle()
    val isFloatingVisible by viewModel.isFloatingOverlayVisible.collectAsStateWithLifecycle()
    val isFacecamActive by viewModel.isFacecamActive.collectAsStateWithLifecycle()

    var showQuickSettingSheet by remember { mutableStateOf<QuickSettingType?>(null) }
    var recordingToRename by remember { mutableStateOf<RecordingEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val isRecording = status is RecordingStatus.Recording
    val isPaused = status is RecordingStatus.Paused
    val isCountdown = status is RecordingStatus.Countdown

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_screen_recorder_logo),
                            contentDescription = "Screen Recorder logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Screen Recorder",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RecordRed.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RecordRed)
                                ) {
                                    Text(
                                        text = "PRO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = RecordRed,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "HD Screen & Audio Capture",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Hero Graphic Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().height(105.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.banner_recording),
                            contentDescription = "Screen recorder banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF0F172A).copy(alpha = 0.92f),
                                            Color(0xFF0F172A).copy(alpha = 0.4f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Crystal Clear 60 FPS",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Internal Audio & Mic Support",
                                fontSize = 12.sp,
                                color = RecordCyan
                            )
                        }
                    }
                }
            }

            // Main Recording Card
            item {
                MainRecordingCard(
                    status = status,
                    config = config,
                    onStart = { viewModel.startRecording() },
                    onPause = { viewModel.pauseRecording() },
                    onResume = { viewModel.resumeRecording() },
                    onStop = { viewModel.stopRecording() }
                )
            }

            // Quick Settings Bar
            item {
                Text(
                    text = "Quick Settings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickChip(
                        icon = Icons.Default.Hd,
                        label = config.resolution,
                        modifier = Modifier.weight(1f),
                        onClick = { showQuickSettingSheet = QuickSettingType.RESOLUTION }
                    )
                    QuickChip(
                        icon = Icons.Default.Speed,
                        label = "${config.fps} FPS",
                        modifier = Modifier.weight(1f),
                        onClick = { showQuickSettingSheet = QuickSettingType.FPS }
                    )
                    QuickChip(
                        icon = Icons.Default.Mic,
                        label = when (config.audioSource) {
                            "No Audio" -> "Mute"
                            "Microphone" -> "Mic"
                            "Internal Audio" -> "Internal"
                            else -> "Mic+Int"
                        },
                        modifier = Modifier.weight(1f),
                        onClick = { showQuickSettingSheet = QuickSettingType.AUDIO }
                    )
                    QuickChip(
                        icon = Icons.Default.ScreenRotation,
                        label = config.orientation,
                        modifier = Modifier.weight(1f),
                        onClick = { showQuickSettingSheet = QuickSettingType.ORIENTATION }
                    )
                }
            }

            // Quick Feature Toggles
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Interactive Features",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Floating widget toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = RecordCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Floating Controls",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Movable on-screen control bubble",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isFloatingVisible,
                                onCheckedChange = { viewModel.toggleFloatingControls(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = RecordRed, checkedTrackColor = RecordRed.copy(alpha = 0.4f)),
                                modifier = Modifier.testTag("toggle_floating_controls")
                            )
                        }

                        // Facecam PIP toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraFront,
                                    contentDescription = null,
                                    tint = RecordAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Facecam Overlay (PIP)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Front camera reaction window",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isFacecamActive,
                                onCheckedChange = { viewModel.toggleFacecam(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = RecordRed, checkedTrackColor = RecordRed.copy(alpha = 0.4f)),
                                modifier = Modifier.testTag("toggle_facecam")
                            )
                        }
                    }
                }
            }

            // Recent Recordings Header & List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Recordings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier.testTag("home_view_all_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "View All", color = RecordRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = RecordRed, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            if (recentRecordings.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No recordings yet",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the big red button above to start your first recording!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                items(recentRecordings, key = { it.id }) { rec ->
                    RecordingCard(
                        recording = rec,
                        onClick = { onNavigateToPlayer(rec.id) },
                        onRename = {
                            recordingToRename = rec
                            renameInput = rec.title
                        },
                        onDelete = {
                            // Home recent delete
                        },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"
                                putExtra(Intent.EXTRA_SUBJECT, rec.title)
                                putExtra(Intent.EXTRA_TEXT, "Recorded with Screen Recorder: ${rec.title}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share recording"))
                        },
                        onToggleFavorite = {
                            // Toggle favorite handled by repository
                        }
                    )
                }
            }
        }

        // Floating Bubble Overlay
        if (isFloatingVisible) {
            FloatingWidgetOverlay(
                recordingStatus = status,
                onPause = { viewModel.pauseRecording() },
                onResume = { viewModel.resumeRecording() },
                onStop = { viewModel.stopRecording() },
                onClose = { viewModel.toggleFloatingControls(false) },
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // Facecam PIP Overlay
        if (isFacecamActive) {
            FacecamOverlay(
                onClose = { viewModel.toggleFacecam(false) },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        // Fullscreen Countdown overlay
        if (isCountdown) {
            CountdownOverlay(secondsRemaining = (status as RecordingStatus.Countdown).secondsRemaining)
        }

        // Quick Setting Bottom Sheet Dialog
        showQuickSettingSheet?.let { settingType ->
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showQuickSettingSheet = null },
                sheetState = sheetState
            ) {
                QuickSettingSheetContent(
                    type = settingType,
                    currentConfig = config,
                    onSelectResolution = {
                        viewModel.setResolution(it)
                        showQuickSettingSheet = null
                    },
                    onSelectFps = {
                        viewModel.setFps(it)
                        showQuickSettingSheet = null
                    },
                    onSelectAudio = {
                        viewModel.setAudioSource(it)
                        showQuickSettingSheet = null
                    },
                    onSelectOrientation = {
                        viewModel.setOrientation(it)
                        showQuickSettingSheet = null
                    }
                )
            }
        }

        // Rename Dialog
        recordingToRename?.let { rec ->
            AlertDialog(
                onDismissRequest = { recordingToRename = null },
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
                            // Handled
                            recordingToRename = null
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recordingToRename = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun MainRecordingCard(
    status: RecordingStatus,
    config: com.example.data.settings.RecordingConfig,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val isRecording = status is RecordingStatus.Recording
    val isPaused = status is RecordingStatus.Paused
    val isSaving = status is RecordingStatus.Saving

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.15f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isRecording) 800 else 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val timerText = when (status) {
        is RecordingStatus.Recording -> {
            val totalSeconds = status.durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
        is RecordingStatus.Paused -> {
            val totalSeconds = status.durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
        else -> "00:00:00"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isRecording) RecordRed else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("main_recording_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isRecording -> RecordRed
                                isPaused -> RecordAmber
                                else -> RecordGreen
                            }
                        )
                )
                Text(
                    text = when {
                        isRecording -> "RECORDING ACTIVE"
                        isPaused -> "RECORDING PAUSED"
                        isSaving -> "SAVING VIDEO..."
                        else -> "READY TO RECORD"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = when {
                        isRecording -> RecordRed
                        isPaused -> RecordAmber
                        else -> RecordGreen
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timer display
            Text(
                text = timerText,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("recording_timer")
            )

            // Audio Waveform visualizer
            val amps = (status as? RecordingStatus.Recording)?.audioAmplitudes ?: emptyList()
            AudioWaveformVisualizer(
                amplitudes = amps,
                isRecording = isRecording,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Control Buttons
            if (!isRecording && !isPaused) {
                // Large Start Recording Button with pulse halo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    // Pulsing outer halo
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(RecordRed.copy(alpha = 0.22f))
                    )

                    // Core Button
                    Surface(
                        shape = CircleShape,
                        color = RecordRed,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(onClick = onStart)
                            .testTag("start_recording_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Text(
                    text = "Tap to Record Screen",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                // Active recording controls: Pause/Resume + Stop
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = RecordAmber,
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .size(62.dp)
                                    .clickable(onClick = onPause)
                                    .testTag("pause_recording_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Pause", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RecordAmber)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = RecordGreen,
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .size(62.dp)
                                    .clickable(onClick = onResume)
                                    .testTag("resume_recording_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Resume", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RecordGreen)
                        }
                    }

                    // Stop button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = RecordRed,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(62.dp)
                                .clickable(onClick = onStop)
                                .testTag("stop_recording_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Stop", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RecordRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specs badge bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${config.resolution} • ${config.fps}fps",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RecordCyan
                )
                Text(
                    text = config.audioSource,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

enum class QuickSettingType {
    RESOLUTION,
    FPS,
    AUDIO,
    ORIENTATION
}

@Composable
private fun QuickSettingSheetContent(
    type: QuickSettingType,
    currentConfig: com.example.data.settings.RecordingConfig,
    onSelectResolution: (String) -> Unit,
    onSelectFps: (Int) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSelectOrientation: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (type) {
            QuickSettingType.RESOLUTION -> {
                Text("Select Resolution", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                listOf("480p", "720p", "1080p", "1440p (2K)").forEach { res ->
                    val cleanRes = res.substringBefore(" ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectResolution(cleanRes) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentConfig.resolution == cleanRes,
                            onClick = { onSelectResolution(cleanRes) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(res, fontSize = 15.sp)
                    }
                }
            }
            QuickSettingType.FPS -> {
                Text("Select Frame Rate (FPS)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                listOf(24, 30, 60).forEach { fps ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFps(fps) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentConfig.fps == fps,
                            onClick = { onSelectFps(fps) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$fps FPS", fontSize = 15.sp)
                    }
                }
            }
            QuickSettingType.AUDIO -> {
                Text("Select Audio Source", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                listOf("No Audio", "Microphone", "Internal Audio", "Microphone + Internal").forEach { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAudio(source) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentConfig.audioSource == source,
                            onClick = { onSelectAudio(source) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(source, fontSize = 15.sp)
                    }
                }
            }
            QuickSettingType.ORIENTATION -> {
                Text("Select Video Orientation", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                listOf("Auto", "Portrait", "Landscape").forEach { ori ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOrientation(ori) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentConfig.orientation == ori,
                            onClick = { onSelectOrientation(ori) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(ori, fontSize = 15.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
