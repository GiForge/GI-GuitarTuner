package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.MusicalPitch
import com.example.dsp.TuningPreset
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldTune
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationSheet(
    a4Frequency: Double,
    noiseThreshold: Float,
    currentRms: Float,
    selectedTuning: TuningPreset,
    onSetA4: (Double) -> Unit,
    onSetNoiseThreshold: (Float) -> Unit,
    onSelectTuning: (TuningPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioDarkBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Calibrazione",
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calibrazione & Accordature",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_calibration_button")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Chiudi",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: A4 Pitch Calibration
            Text(
                text = "CALIBRAZIONE DIAPASON (A4 / LA3)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Large A4 Readout and Steppers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioCardElevated)
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onSetA4((a4Frequency - 1.0).coerceAtLeast(410.0)) },
                    modifier = Modifier.testTag("a4_minus_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "-1 Hz", tint = ElectricCyan)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.1f Hz", a4Frequency),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = ElectricCyan
                    )
                    Text(
                        text = when (a4Frequency.toInt()) {
                            415 -> "Barocco (415 Hz)"
                            432 -> "Verdi / Aureo (432 Hz)"
                            440 -> "Standard Internazionale (440 Hz)"
                            442 -> "Orchestra Europa / Violino (442 Hz)"
                            444 -> "Brillante (444 Hz)"
                            else -> "Personalizzato"
                        },
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { onSetA4((a4Frequency + 1.0).coerceAtMost(466.0)) },
                    modifier = Modifier.testTag("a4_plus_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "+1 Hz", tint = ElectricCyan)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Calibration Preset Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    Pair(415.0, "415 Hz Barocco"),
                    Pair(432.0, "432 Hz Verdi"),
                    Pair(440.0, "440 Hz Standard"),
                    Pair(442.0, "442 Hz Orchestra"),
                    Pair(444.0, "444 Hz")
                )
                items(presets) { (freq, label) ->
                    val isSelected = kotlin.math.abs(a4Frequency - freq) < 0.1
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else StudioCardElevated)
                            .border(1.dp, if (isSelected) ElectricCyan else StudioBorder, RoundedCornerShape(10.dp))
                            .clickable { onSetA4(freq) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ElectricCyan else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Background Noise Gate Filter
            Text(
                text = "GESTIONE RUMORE DI FONDO (NOISE GATE)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Regola la soglia per silenziare i rumori ambientali della stanza o del microfono.",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioCardElevated)
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Soglia Noise Gate: ${(noiseThreshold * 1000).toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (currentRms > noiseThreshold) "Segnale Attivo" else "Silenzio / Filtrato",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentRms > noiseThreshold) EmeraldTune else TextMuted
                    )
                }

                Slider(
                    value = noiseThreshold,
                    onValueChange = onSetNoiseThreshold,
                    valueRange = 0.003f..0.06f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = Color(0xFF28364F)
                    ),
                    modifier = Modifier.testTag("noise_threshold_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Molto Sensibile", fontSize = 10.sp, color = TextMuted)
                    Text("Normale", fontSize = 10.sp, color = TextMuted)
                    Text("Ambiente Rumoroso", fontSize = 10.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Select Instrument Tuning
            Text(
                text = "ACCORDATURA STRUMENTO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MusicalPitch.BUILT_IN_TUNINGS) { tuning ->
                    val isSelected = selectedTuning.id == tuning.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.18f) else StudioCardElevated)
                            .border(1.dp, if (isSelected) ElectricCyan else StudioBorder, RoundedCornerShape(12.dp))
                            .clickable { onSelectTuning(tuning) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("preset_select_${tuning.id}")
                    ) {
                        Column {
                            Text(
                                text = tuning.instrumentType.title,
                                fontSize = 10.sp,
                                color = if (isSelected) ElectricCyan else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tuning.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
