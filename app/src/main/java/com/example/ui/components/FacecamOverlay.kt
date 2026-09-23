package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RecordCyan
import com.example.ui.theme.RecordRed
import kotlin.math.roundToInt

@Composable
fun FacecamOverlay(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(40f) }
    var offsetY by remember { mutableFloatStateOf(100f) }
    var isFrontCam by remember { mutableStateOf(true) }

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
            .testTag("facecam_overlay")
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF2C3E50), Color(0xFF1A1A24))
                    )
                )
                .border(2.5.dp, RecordCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Facecam video",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(54.dp)
            )

            // Live tag
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .clip(CircleShape)
                    .background(RecordRed.copy(alpha = 0.9f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isFrontCam) "FRONT CAM" else "BACK CAM",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.7f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Facecam",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        // Switch camera button
        IconButton(
            onClick = { isFrontCam = !isFrontCam },
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.7f))
        ) {
            Icon(
                imageVector = Icons.Default.SwitchCamera,
                contentDescription = "Switch Camera",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
