package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.RecordAmber
import com.example.ui.theme.RecordCyan
import com.example.ui.theme.RecordGreen
import com.example.ui.theme.RecordRed
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val storageInfo by viewModel.storageInfo.collectAsStateWithLifecycle()

    var showResolutionDialog by remember { mutableStateOf(false) }
    var showFpsDialog by remember { mutableStateOf(false) }
    var showBitrateDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showCountdownDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Storage Info Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.SdCard, contentDescription = null, tint = RecordCyan)
                            Text("Storage Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { storageInfo.usagePercentage },
                            color = RecordRed,
                            trackColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Free: ${storageInfo.availableSpaceFormatted} / ${storageInfo.totalSpaceFormatted}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Recordings: ${storageInfo.appUsageFormatted}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RecordCyan
                            )
                        }
                    }
                }
            }

            // Section: Video & Audio Settings
            item {
                SettingsSectionHeader("VIDEO & AUDIO QUALITY")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Default.Hd,
                            title = "Resolution",
                            subtitle = config.resolution,
                            onClick = { showResolutionDialog = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsClickableRow(
                            icon = Icons.Default.Speed,
                            title = "Frame Rate (FPS)",
                            subtitle = "${config.fps} FPS",
                            onClick = { showFpsDialog = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsClickableRow(
                            icon = Icons.Default.Speed,
                            title = "Bitrate",
                            subtitle = config.bitrate,
                            onClick = { showBitrateDialog = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsClickableRow(
                            icon = Icons.Default.Audiotrack,
                            title = "Audio Source",
                            subtitle = config.audioSource,
                            onClick = { showAudioDialog = true }
                        )
                    }
                }
            }

            // Section: Controls & Features
            item {
                SettingsSectionHeader("RECORDING CONTROLS")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column {
                        SettingsClickableRow(
                            icon = Icons.Default.Timer,
                            title = "Countdown Before Start",
                            subtitle = if (config.countdownSeconds == 0) "Off" else "${config.countdownSeconds} seconds",
                            onClick = { showCountdownDialog = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsSwitchRow(
                            icon = Icons.Default.Widgets,
                            title = "Floating Controls Bubble",
                            subtitle = "Movable toolbar during screen recording",
                            checked = config.showFloatingControls,
                            onCheckedChange = { viewModel.toggleFloatingControls(it) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsSwitchRow(
                            icon = Icons.Default.CameraFront,
                            title = "Facecam PIP Overlay",
                            subtitle = "Show front camera reaction overlay",
                            checked = config.showFacecam,
                            onCheckedChange = { viewModel.toggleFacecam(it) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsSwitchRow(
                            icon = Icons.Default.TouchApp,
                            title = "Show Touches Feedback",
                            subtitle = "Visualize screen touches in tutorial recordings",
                            checked = config.showTouches,
                            onCheckedChange = { viewModel.toggleTouches(it) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsSwitchRow(
                            icon = Icons.Default.Vibration,
                            title = "Shake Device to Stop",
                            subtitle = "Stop recording by shaking your phone",
                            checked = config.shakeToStop,
                            onCheckedChange = { viewModel.toggleShakeToStop(it) }
                        )
                    }
                }
            }

            // Section: Appearance & About
            item {
                SettingsSectionHeader("PREFERENCES & ABOUT")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column {
                        SettingsSwitchRow(
                            icon = Icons.Default.DarkMode,
                            title = "Modern Dark Theme",
                            subtitle = "Optimal contrast for recording review",
                            checked = config.darkTheme,
                            onCheckedChange = { viewModel.toggleDarkTheme(it) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsClickableRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy & Permissions",
                            subtitle = "Media projection, Microphone, Storage",
                            onClick = { showPrivacyDialog = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsClickableRow(
                            icon = Icons.Default.HelpOutline,
                            title = "Recording Guide & Tips",
                            subtitle = "How to record games, tutorials & meetings",
                            onClick = { showHelpDialog = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsClickableRow(
                            icon = Icons.Default.Info,
                            title = "App Version",
                            subtitle = "v1.2.0 • Pro Build",
                            onClick = {}
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showResolutionDialog) {
        OptionChooserDialog(
            title = "Choose Resolution",
            options = listOf("480p", "720p", "1080p", "1440p"),
            selectedOption = config.resolution,
            onSelect = {
                viewModel.setResolution(it)
                showResolutionDialog = false
            },
            onDismiss = { showResolutionDialog = false }
        )
    }

    if (showFpsDialog) {
        OptionChooserDialog(
            title = "Choose Frame Rate (FPS)",
            options = listOf("24 FPS", "30 FPS", "60 FPS"),
            selectedOption = "${config.fps} FPS",
            onSelect = {
                viewModel.setFps(it.substringBefore(" ").toInt())
                showFpsDialog = false
            },
            onDismiss = { showFpsDialog = false }
        )
    }

    if (showBitrateDialog) {
        OptionChooserDialog(
            title = "Choose Bitrate",
            options = listOf("Auto", "6 Mbps", "12 Mbps", "16 Mbps"),
            selectedOption = config.bitrate,
            onSelect = {
                viewModel.setBitrate(it)
                showBitrateDialog = false
            },
            onDismiss = { showBitrateDialog = false }
        )
    }

    if (showAudioDialog) {
        OptionChooserDialog(
            title = "Choose Audio Source",
            options = listOf("No Audio", "Microphone", "Internal Audio", "Microphone + Internal"),
            selectedOption = config.audioSource,
            onSelect = {
                viewModel.setAudioSource(it)
                showAudioDialog = false
            },
            onDismiss = { showAudioDialog = false }
        )
    }

    if (showCountdownDialog) {
        OptionChooserDialog(
            title = "Countdown Before Start",
            options = listOf("Off", "3 seconds", "5 seconds", "10 seconds"),
            selectedOption = if (config.countdownSeconds == 0) "Off" else "${config.countdownSeconds} seconds",
            onSelect = {
                val sec = when (it) {
                    "Off" -> 0
                    "3 seconds" -> 3
                    "5 seconds" -> 5
                    "10 seconds" -> 10
                    else -> 3
                }
                viewModel.setCountdown(sec)
                showCountdownDialog = false
            },
            onDismiss = { showCountdownDialog = false }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Security") },
            text = {
                Text(
                    "Screen Recorder captures your screen and audio locally on your device using Android MediaProjection APIs. " +
                    "Your recordings are NEVER uploaded to any external server or shared without your explicit consent. " +
                    "Microphone and Camera (Facecam) permissions are only activated when initiated by you."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("OK") }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Recording Tips & Guide") },
            text = {
                Text(
                    "1. High FPS Gaming: Use 1080p @ 60 FPS with 'Internal Audio' for smooth gameplay clips.\n\n" +
                    "2. Tutorials & Meetings: Turn on 'Facecam PIP' and 'Microphone + Internal' for clear voice commentary.\n\n" +
                    "3. Floating Controls: Enable the floating bubble to easily pause, resume, or stop recording while in other apps without leaving them."
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("Got it") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RecordRed,
                checkedTrackColor = RecordRed.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun OptionChooserDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(opt) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == opt,
                            onClick = { onSelect(opt) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(opt, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
