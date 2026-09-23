package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.RecordingStatus
import com.example.ui.theme.RecordAmber
import com.example.ui.theme.RecordGreen
import com.example.ui.theme.RecordRed
import kotlin.math.roundToInt

@Composable
fun FloatingWidgetOverlay(
    recordingStatus: RecordingStatus,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(60f) }
    var offsetY by remember { mutableFloatStateOf(200f) }
    var isExpanded by remember { mutableStateOf(true) }

    val isRecording = recordingStatus is RecordingStatus.Recording
    val isPaused = recordingStatus is RecordingStatus.Paused

    val durationText = when (recordingStatus) {
        is RecordingStatus.Recording -> {
            val totalSec = recordingStatus.durationMs / 1000
            String.format("%02d:%02d", (totalSec % 3600) / 60, totalSec % 60)
        }
        is RecordingStatus.Paused -> {
            val totalSec = recordingStatus.durationMs / 1000
            String.format("%02d:%02d", (totalSec % 3600) / 60, totalSec % 60)
        }
        else -> "00:00"
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isRecording) RecordRed else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.testTag("floating_widget")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Record dot / Collapse toggle
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) RecordRed else if (isPaused) RecordAmber else RecordGreen)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = durationText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        if (isRecording) {
                            IconButton(
                                onClick = onPause,
                                modifier = Modifier.size(36.dp).testTag("floating_pause")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = RecordAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else if (isPaused) {
                            IconButton(
                                onClick = onResume,
                                modifier = Modifier.size(36.dp).testTag("floating_resume")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = RecordGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onStop,
                            modifier = Modifier.size(36.dp).testTag("floating_stop")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = RecordRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close floating widget",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
