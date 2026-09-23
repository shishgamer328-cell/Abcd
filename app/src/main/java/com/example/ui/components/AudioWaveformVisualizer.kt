package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RecordRed

@Composable
fun AudioWaveformVisualizer(
    amplitudes: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = RecordRed
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayList = if (amplitudes.isEmpty() || !isRecording) {
            List(20) { 0.15f }
        } else {
            amplitudes.takeLast(20)
        }

        displayList.forEach { amp ->
            val animatedHeight by animateFloatAsState(
                targetValue = if (isRecording) (amp.coerceIn(0.1f, 1f) * 32.dp.value) else 6f,
                animationSpec = tween(durationMillis = 120),
                label = "waveform_bar"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isRecording) barColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
