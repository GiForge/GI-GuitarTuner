package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldTune
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

@Composable
fun WaveformVisualizer(
    waveformBuffer: FloatArray,
    rmsLevel: Float,
    noiseThreshold: Float,
    isSignalAboveGate: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioCardElevated)
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        // Header with Live Waveform Title & RMS level meter: FIXED HEIGHT 22.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isSignalAboveGate) EmeraldTune else TextMuted)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "OSCILLOSCOPIO LIVE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.weight(1f))

            // Fixed width for RMS percentage to prevent text widening/jumping
            Text(
                text = String.format(Locale.US, "%3d%%", (rmsLevel * 100).toInt().coerceIn(0, 100)),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                color = if (isSignalAboveGate) EmeraldTune else TextMuted,
                modifier = Modifier.width(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Oscilloscope Screen Canvas: FIXED HEIGHT 64.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF090D15))
                .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(10.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize().testTag("waveform_canvas")) {
                val width = size.width
                val height = size.height
                val midY = height / 2f

                // Draw background oscilloscope grid lines
                val gridColor = Color(0xFF141C2B)
                // Center horizontal line
                drawLine(
                    color = Color(0xFF1E2A40),
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 1f
                )
                // Threshold lines (Noise Gate boundaries)
                val gateYOffset = (noiseThreshold * 8f * midY).coerceIn(3f, midY - 4f)
                drawLine(
                    color = Color(0x33FFB300),
                    start = Offset(0f, midY - gateYOffset),
                    end = Offset(width, midY - gateYOffset),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0x33FFB300),
                    start = Offset(0f, midY + gateYOffset),
                    end = Offset(width, midY + gateYOffset),
                    strokeWidth = 1f
                )

                // Vertical grid divisions
                val gridSteps = 8
                for (i in 1 until gridSteps) {
                    val x = width * i / gridSteps
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                }

                // If no buffer or silence, draw subtle center idle line
                if (waveformBuffer.isEmpty() || !isSignalAboveGate) {
                    drawLine(
                        color = ElectricCyan.copy(alpha = 0.25f),
                        start = Offset(0f, midY),
                        end = Offset(width, midY),
                        strokeWidth = 1.5f,
                        cap = StrokeCap.Round
                    )
                    return@Canvas
                }

                // Build oscilloscope curve path
                val path = Path()
                val stepX = width / (waveformBuffer.size - 1)
                val ampFactor = midY * 0.9f

                for (i in waveformBuffer.indices) {
                    val sample = waveformBuffer[i].coerceIn(-1f, 1f)
                    val x = i * stepX
                    val y = midY - sample * ampFactor

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevSample = waveformBuffer[i - 1].coerceIn(-1f, 1f)
                        val prevX = (i - 1) * stepX
                        val prevY = midY - prevSample * ampFactor
                        val cX = (prevX + x) / 2f
                        path.cubicTo(cX, prevY, cX, y, x, y)
                    }
                }

                // Draw outer glowing neon blur
                drawPath(
                    path = path,
                    color = ElectricCyan.copy(alpha = 0.25f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // Draw sharp foreground waveform
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.7f),
                            EmeraldTune,
                            ElectricCyan
                        )
                    ),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }
        }
    }
}
