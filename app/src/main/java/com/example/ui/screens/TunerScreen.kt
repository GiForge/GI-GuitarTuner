package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.PitchResult
import com.example.dsp.TunerAudioState
import com.example.ui.components.StringPegsSelector
import com.example.ui.components.TunerGaugeView
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCardSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TunerSettings
import java.util.Locale

@Composable
fun TunerScreen(
    tunerSettings: TunerSettings,
    tunerAudioState: TunerAudioState,
    hasRecordPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenCalibration: () -> Unit,
    onSelectString: (Int?) -> Unit,
    onPlayReferenceTone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Permission Request Banner if not granted
        if (!hasRecordPermission) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2B1D1D))
                        .border(1.dp, Color(0xFFEF5350), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Microfono Richiesto",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Consenti l'accesso per accordare il tuo strumento in tempo reale.",
                            fontSize = 11.sp,
                            color = Color(0xFFE0E0E0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("request_permission_button")
                        ) {
                            Text("Consenti Accesso al Microfono", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Instrument & Calibration summary bar: FIXED HEIGHT 44.dp
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tunerSettings.selectedTuning.instrumentType.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${tunerSettings.selectedTuning.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // A4 Calibration button
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioCardElevated)
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable { onOpenCalibration() }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Calibra",
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "A4 = %.1f Hz", tunerSettings.a4Frequency),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Main Tuner Gauge Dial & Fine Tuning Needle: FIXED COMPACT DIMENSIONS
        item {
            TunerGaugeView(
                pitchResult = tunerAudioState.pitchResult,
                isSignalActive = tunerAudioState.isSignalAboveThreshold
            )
        }

        // Instrument Strings Pegs: FIXED SIZES
        item {
            StringPegsSelector(
                tuning = tunerSettings.selectedTuning,
                lockedStringIndex = tunerSettings.lockedStringIndex,
                isAutoString = tunerSettings.isAutoString,
                currentPitchResult = tunerAudioState.pitchResult,
                isSignalActive = tunerAudioState.isSignalAboveThreshold,
                onSelectString = onSelectString,
                onPlayTone = onPlayReferenceTone
            )
        }

        // Real-Time Oscilloscope Waveform Visualizer: FIXED COMPACT SIZE
        item {
            WaveformVisualizer(
                waveformBuffer = tunerAudioState.waveformBuffer,
                rmsLevel = tunerAudioState.rmsLevel,
                noiseThreshold = tunerSettings.noiseGateThreshold,
                isSignalAboveGate = tunerAudioState.isSignalAboveThreshold
            )
        }

        // Quick Reference Tone (Diapason 440 Hz) action: FIXED HEIGHT 40.dp
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .clickable { onPlayReferenceTone("A4") }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Diapason",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Suona Diapason di Riferimento (La 440 Hz)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
