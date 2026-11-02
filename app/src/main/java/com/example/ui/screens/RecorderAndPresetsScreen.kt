package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetEntity
import com.example.recorder.AudioRecording
import com.example.recorder.RecorderState
import com.example.ui.theme.CoralFlat
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldTune
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RecorderAndPresetsScreen(
    recorderState: RecorderState,
    presets: List<PresetEntity>,
    onStartRecording: (String?) -> Unit,
    onStopRecording: () -> Unit,
    onPlayRecording: (AudioRecording) -> Unit,
    onDeleteRecording: (AudioRecording) -> Unit,
    onShareRecording: (AudioRecording) -> Unit,
    onSaveCurrentPreset: (String) -> Unit,
    onApplyPreset: (PresetEntity) -> Unit,
    onDeletePreset: (PresetEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var recordingNameInput by remember { mutableStateOf("") }

    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            containerColor = StudioCardSurface,
            title = {
                Text("Salva Preset Personalizzato", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Salva la configurazione corrente (accordatura, diapason A4 e soglia rumore) nel database locale.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Nome Preset (es. Chitarra Acustica Drop D)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            onSaveCurrentPreset(presetNameInput.trim())
                            presetNameInput = ""
                            showSavePresetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_preset_confirm_button")
                ) {
                    Text("Salva")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Annulla", color = TextMuted)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Section 1: Audio Recorder Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Registratore",
                            tint = if (recorderState.isRecording) CoralFlat else ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REGISTRATORE AUDIO WAV",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (recorderState.isRecording) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CoralFlat)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralFlat)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recording Timer Display
                val durationSec = (recorderState.recordingDurationMs / 1000).toInt()
                val minutes = durationSec / 60
                val seconds = durationSec % 60
                Text(
                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (recorderState.isRecording) CoralFlat else TextPrimary
                )

                if (recorderState.isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Live mic recording level bar
                    LinearProgressIndicator(
                        progress = { (recorderState.currentRms * 3f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldTune,
                        trackColor = StudioBorder
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Record / Stop Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (recorderState.isRecording) {
                        Button(
                            onClick = onStopRecording,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralFlat, contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("stop_recording_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Ferma Registrazione")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salva Brano (WAV)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onStartRecording(recordingNameInput.ifBlank { null }) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("start_recording_button")
                        ) {
                            Icon(Icons.Default.FiberManualRecord, contentDescription = "Avvia Registrazione")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Avvia Nuova Registrazione", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 2: Recordings List & Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BRANI REGISTRATI (${recorderState.recordings.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        if (recorderState.recordings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(StudioCardSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nessun brano registrato. Avvia una registrazione per riascoltare e salvare le tue esecuzioni in formato WAV.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            items(recorderState.recordings) { rec ->
                val isCurrentPlaying = recorderState.playingFile == rec.file
                val isPlayingNow = isCurrentPlaying && recorderState.isPlaying

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCurrentPlaying) Color(0xFF182338) else StudioCardSurface)
                        .border(1.dp, if (isCurrentPlaying) ElectricCyan else StudioBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isPlayingNow) EmeraldTune else ElectricCyan)
                                .clickable { onPlayRecording(rec) }
                                .testTag("play_recording_${rec.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlayingNow) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pausa",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rec.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row {
                                Text(
                                    text = rec.formattedDate,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${(rec.sizeBytes / 1024)} KB (WAV)",
                                    fontSize = 11.sp,
                                    color = ElectricCyan
                                )
                            }
                        }

                        // Share / Export WAV Button
                        IconButton(
                            onClick = { onShareRecording(rec) },
                            modifier = Modifier.testTag("share_recording_${rec.name}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Esporta WAV",
                                tint = ElectricCyan
                            )
                        }

                        // Delete recording button
                        IconButton(
                            onClick = { onDeleteRecording(rec) },
                            modifier = Modifier.testTag("delete_recording_${rec.name}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Elimina",
                                tint = CoralFlat.copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (isCurrentPlaying) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val progressFraction = if (recorderState.playbackDurationMs > 0) {
                            (recorderState.playbackProgressMs.toFloat() / recorderState.playbackDurationMs).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = ElectricCyan,
                            trackColor = StudioBorder
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", (recorderState.playbackProgressMs / 1000) / 60, (recorderState.playbackProgressMs / 1000) % 60),
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", (recorderState.playbackDurationMs / 1000) / 60, (recorderState.playbackDurationMs / 1000) % 60),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Room Database Presets Manager
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DATABASE PRESET & ACCORDATURE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )

                // Save Current Tuning Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, ElectricCyan, RoundedCornerShape(10.dp))
                        .clickable { showSavePresetDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("add_custom_preset_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Nuovo Preset",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Salva Attuale",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        items(presets) { preset ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = preset.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (preset.isCustom) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GoldAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("CUSTOM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Corde: ${preset.notesFormatted}  •  Diapason: ${preset.a4Frequency} Hz",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Apply Button
                    Button(
                        onClick = { onApplyPreset(preset) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCardElevated, contentColor = ElectricCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("apply_preset_${preset.id}")
                    ) {
                        Text("Applica", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Delete custom button
                    if (preset.isCustom) {
                        IconButton(
                            onClick = { onDeletePreset(preset) },
                            modifier = Modifier.testTag("delete_preset_${preset.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Elimina Preset", tint = CoralFlat)
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
