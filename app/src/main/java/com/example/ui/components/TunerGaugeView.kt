package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.PitchResult
import com.example.ui.theme.AmberPitch
import com.example.ui.theme.CoralFlat
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldTune
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TunerGaugeView(
    pitchResult: PitchResult,
    isSignalActive: Boolean,
    modifier: Modifier = Modifier
) {
    val targetCents = if (pitchResult.isValid && isSignalActive) {
        pitchResult.centsDeviation.toFloat().coerceIn(-50f, 50f)
    } else {
        0f
    }

    val animatedCents by animateFloatAsState(
        targetValue = targetCents,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cents_needle"
    )

    val isInTune = pitchResult.isValid && isSignalActive && pitchResult.isInTune
    val statusColor by animateColorAsState(
        targetValue = when {
            !isSignalActive || !pitchResult.isValid -> TextMuted
            isInTune -> EmeraldTune
            animatedCents < -6 -> CoralFlat
            animatedCents > 6 -> AmberPitch
            else -> ElectricCyan
        },
        label = "status_color"
    )

    // Strict fixed-height container to eliminate resizing and widget jumping
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(StudioCardElevated)
            .border(1.dp, StudioBorder, RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top status pill & Cents readout: FIXED HEIGHT 26.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            !isSignalActive -> "In attesa segnale"
                            isInTune -> "ACCORDATO PERFETTO"
                            animatedCents < -3 -> "CALANTE (Basso)"
                            else -> "CRESCENTE (Alto)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Fixed-width Cents readout so text length changes don't shift any other widget
            Text(
                text = if (pitchResult.isValid && isSignalActive) {
                    String.format(Locale.US, "%+5.1f ¢", animatedCents)
                } else {
                    "  0.0 ¢"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                color = if (isInTune) EmeraldTune else TextPrimary,
                modifier = Modifier.width(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Arc Needle Gauge Canvas: FIXED HEIGHT 135.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().testTag("tuner_gauge_canvas")) {
                val centerOffset = Offset(size.width / 2f, size.height * 0.94f)
                val radius = size.height * 0.82f

                // Base Arc: from 200° to 340° (sweep 140°)
                val startAngle = 200f
                val sweepAngle = 140f

                // Background track arc
                drawArc(
                    color = Color(0xFF243045),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                )

                // Perfect tune center zone (±3 cents -> center ±4 degrees)
                val centerZoneStart = startAngle + sweepAngle / 2f - 4f
                drawArc(
                    color = EmeraldTune.copy(alpha = 0.65f),
                    startAngle = centerZoneStart,
                    sweepAngle = 8f,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )

                // Draw tick marks (-50 to +50 cents in increments of 5)
                for (cents in -50..50 step 5) {
                    val progress = (cents + 50f) / 100f
                    val angleDeg = startAngle + progress * sweepAngle
                    val angleRad = angleDeg * PI / 180.0

                    val isMajor = cents % 10 == 0
                    val isCenter = cents == 0
                    val tickLen = if (isCenter) 18f else if (isMajor) 12f else 6f
                    val tickColor = if (isCenter) EmeraldTune else if (isMajor) TextSecondary else Color(0xFF334155)

                    val startX = centerOffset.x + (radius - tickLen) * cos(angleRad).toFloat()
                    val startY = centerOffset.y + (radius - tickLen) * sin(angleRad).toFloat()
                    val endX = centerOffset.x + (radius + 2f) * cos(angleRad).toFloat()
                    val endY = centerOffset.y + (radius + 2f) * sin(angleRad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isCenter) 3.5f else if (isMajor) 2f else 1f,
                        cap = StrokeCap.Round
                    )
                }

                // Needle calculation: maps cents (-50..50) to startAngle..(startAngle+sweepAngle)
                val needleProgress = ((animatedCents + 50f) / 100f).coerceIn(0f, 1f)
                val needleAngleDeg = startAngle + needleProgress * sweepAngle
                val needleAngleRad = needleAngleDeg * PI / 180.0

                val needleLength = radius * 0.96f
                val tipX = centerOffset.x + needleLength * cos(needleAngleRad).toFloat()
                val tipY = centerOffset.y + needleLength * sin(needleAngleRad).toFloat()

                // Needle glow shadow
                drawLine(
                    color = statusColor.copy(alpha = 0.35f),
                    start = centerOffset,
                    end = Offset(tipX, tipY),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )

                // Main needle
                drawLine(
                    color = statusColor,
                    start = centerOffset,
                    end = Offset(tipX, tipY),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )

                // Needle pivot circle
                drawCircle(
                    color = statusColor,
                    radius = 8f,
                    center = centerOffset
                )
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = 3.5f,
                    center = centerOffset
                )
            }

            // Big Central Note Display: STRICT FIXED HEIGHT (72.dp)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 10.dp)
                    .height(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Fixed height note row (48.dp)
                Row(
                    modifier = Modifier.height(48.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val noteDisplay = if (pitchResult.isValid && isSignalActive && pitchResult.note != null) {
                        pitchResult.note.name
                    } else {
                        "--"
                    }
                    val octaveDisplay = if (pitchResult.isValid && isSignalActive && pitchResult.note != null) {
                        "${pitchResult.note.octave}"
                    } else {
                        ""
                    }

                    Text(
                        text = noteDisplay,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        lineHeight = 48.sp,
                        textAlign = TextAlign.Center
                    )
                    if (octaveDisplay.isNotEmpty()) {
                        Text(
                            text = octaveDisplay,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                        )
                    }
                }

                // Fixed height note subtitle (18.dp)
                Box(
                    modifier = Modifier.height(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val subText = if (pitchResult.isValid && isSignalActive && pitchResult.note != null) {
                        "(${pitchResult.note.italianName})"
                    } else {
                        "Suona una corda"
                    }
                    Text(
                        text = subText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSignalActive) TextSecondary else TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Fine Tuning Strobe / Bar Indicator: FIXED HEIGHT 20.dp
        FineTuningStrobeBar(
            cents = animatedCents,
            isInTune = isInTune,
            isSignalActive = isSignalActive
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Frequency readout stats: FIXED HEIGHT 44.dp with rigid column widths
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF111723))
                .padding(vertical = 4.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Rilevata", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = if (pitchResult.isValid && isSignalActive) {
                        String.format(Locale.US, "%5.1f Hz", pitchResult.detectedFrequency)
                    } else "---.- Hz",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            }

            Box(modifier = Modifier.width(1.dp).height(20.dp).background(StudioBorder))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Target", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = if (pitchResult.isValid && isSignalActive) {
                        String.format(Locale.US, "%5.1f Hz", pitchResult.targetFrequency)
                    } else "---.- Hz",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ElectricCyan
                )
            }

            Box(modifier = Modifier.width(1.dp).height(20.dp).background(StudioBorder))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Scostamento", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = if (pitchResult.isValid && isSignalActive) {
                        val deltaHz = pitchResult.detectedFrequency - pitchResult.targetFrequency
                        String.format(Locale.US, "%+4.1f Hz", deltaHz)
                    } else "---.- Hz",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isInTune) EmeraldTune else AmberPitch
                )
            }
        }
    }
}

@Composable
fun FineTuningStrobeBar(
    cents: Float,
    isInTune: Boolean,
    isSignalActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val totalSegments = 21 // 10 on left, 1 center, 10 on right
        for (i in 0 until totalSegments) {
            val segmentCents = (i - 10) * 5f
            val isActive = if (!isSignalActive) {
                false
            } else if (isInTune) {
                i in 9..11
            } else if (cents < 0) {
                segmentCents in cents..0f
            } else {
                segmentCents in 0f..cents
            }

            val segColor = when {
                i == 10 && isInTune -> EmeraldTune
                i < 10 && isActive -> CoralFlat
                i > 10 && isActive -> AmberPitch
                i == 10 -> ElectricCyan.copy(alpha = 0.5f)
                else -> Color(0xFF1E293B)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (i == 10) 14.dp else 9.dp)
                    .padding(horizontal = 1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(segColor)
            )
        }
    }
}
