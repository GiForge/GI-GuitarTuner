package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drums.DrumStyle
import com.example.drums.MetronomeState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldTune
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MetronomeScreen(
    metronomeState: MetronomeState,
    onTogglePlay: () -> Unit,
    onSetBpm: (Int) -> Unit,
    onSetBeatsPerBar: (Int) -> Unit,
    onSetDrumStyle: (DrumStyle) -> Unit,
    onTapTempo: () -> Unit,
    onToggleHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Big Metronome Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header & Time signature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "METRONOMO DIGITALE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    // Time signatures buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(2, 3, 4, 6).forEach { beats ->
                            val isSelected = metronomeState.beatsPerBar == beats
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else StudioCardElevated)
                                    .border(1.dp, if (isSelected) ElectricCyan else StudioBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSetBeatsPerBar(beats) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("time_sig_$beats")
                            ) {
                                Text(
                                    text = "$beats/4",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ElectricCyan else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsing Beat Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until metronomeState.beatsPerBar) {
                        val isCurrentBeat = metronomeState.isPlaying && metronomeState.currentBeat == i
                        val isDownbeat = i == 0

                        val dotSize by animateFloatAsState(
                            targetValue = if (isCurrentBeat) 24f else 14f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "dot_size_$i"
                        )

                        val dotColor by animateColorAsState(
                            targetValue = when {
                                isCurrentBeat && isDownbeat -> GoldAccent
                                isCurrentBeat -> EmeraldTune
                                isDownbeat -> Color(0xFF475569)
                                else -> Color(0xFF1E293B)
                            },
                            label = "dot_color_$i"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(dotSize.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    1.dp,
                                    if (isCurrentBeat) Color.White.copy(alpha = 0.6f) else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // BPM Big Display & Steppers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Minus 5 / Minus 1
                    Row {
                        IconButton(
                            onClick = { onSetBpm(metronomeState.bpm - 5) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Text("-5", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                        IconButton(
                            onClick = { onSetBpm(metronomeState.bpm - 1) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-1", tint = ElectricCyan)
                        }
                    }

                    // Large BPM
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${metronomeState.bpm}",
                            fontSize = 62.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary,
                            lineHeight = 62.sp
                        )
                        Text(
                            text = "BPM (Battiti al minuto)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Plus 1 / Plus 5
                    Row {
                        IconButton(
                            onClick = { onSetBpm(metronomeState.bpm + 1) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+1", tint = ElectricCyan)
                        }
                        IconButton(
                            onClick = { onSetBpm(metronomeState.bpm + 5) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Text("+5", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Slider
                Slider(
                    value = metronomeState.bpm.toFloat(),
                    onValueChange = { onSetBpm(it.toInt()) },
                    valueRange = 30f..260f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = Color(0xFF28364F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("bpm_slider")
                )

                // Tempo Markings (Largo, Andante, Moderato, Allegro, Presto)
                Text(
                    text = when (metronomeState.bpm) {
                        in 30..60 -> "Largo / Grave (30-60)"
                        in 61..76 -> "Adagio (61-76)"
                        in 77..108 -> "Andante (77-108)"
                        in 109..120 -> "Moderato (109-120)"
                        in 121..168 -> "Allegro (121-168)"
                        else -> "Presto / Prestissimo (168+)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Controls: Tap Tempo, Big Play/Pause, Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tap Tempo Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(StudioCardElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                            .clickable { onTapTempo() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("tap_tempo_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Tap Tempo",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TAP", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    // Main Start/Stop Floating Style Button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(if (metronomeState.isPlaying) Color(0xFFE53935) else EmeraldTune)
                            .clickable { onTogglePlay() }
                            .testTag("metronome_play_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (metronomeState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (metronomeState.isPlaying) "Pausa Metronomo" else "Avvia Metronomo",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Vibration Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (metronomeState.hapticEnabled) ElectricCyan.copy(alpha = 0.2f) else StudioCardElevated)
                            .border(
                                1.dp,
                                if (metronomeState.hapticEnabled) ElectricCyan else StudioBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onToggleHaptic() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("haptic_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Feedback Tattile",
                                tint = if (metronomeState.hapticEnabled) ElectricCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "APTICA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (metronomeState.hapticEnabled) ElectricCyan else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Section: Drum Loops
        item {
            Text(
                text = "LOOP DI BATTERIA & GROOVE DI PRATICA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        items(DrumStyle.values()) { style ->
            val isSelected = metronomeState.selectedStyle == style

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) ElectricCyan.copy(alpha = 0.15f) else StudioCardSurface)
                    .border(
                        1.5.dp,
                        if (isSelected) ElectricCyan else StudioBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSetDrumStyle(style) }
                    .padding(14.dp)
                    .testTag("drum_style_${style.name}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ElectricCyan else StudioCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = style.displayName,
                            tint = if (isSelected) Color.Black else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = style.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ElectricCyan else TextPrimary
                        )
                        Text(
                            text = style.description,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    if (isSelected && metronomeState.isPlaying) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldTune.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "IN RIPRODUZIONE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldTune
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
