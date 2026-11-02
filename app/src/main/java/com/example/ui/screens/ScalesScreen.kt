package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
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
import com.example.dsp.MusicalPitch
import com.example.scales.ScaleCalculator
import com.example.scales.ScaleNote
import com.example.scales.ScaleType
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldTune
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCardSurface
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScalesScreen(
    selectedRoot: String,
    selectedScaleType: ScaleType,
    isGuitarView: Boolean,
    activePlayingNote: String?,
    onSelectRoot: (String) -> Unit,
    onSelectScaleType: (ScaleType) -> Unit,
    onToggleInstrument: (Boolean) -> Unit,
    onPlayNote: (Double, String) -> Unit,
    onPlayFullScale: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleNotes = ScaleCalculator.computeScaleNotes(selectedRoot, selectedScaleType)
    val scaleNoteNames = scaleNotes.map { it.noteName }.toSet()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Instrument Toggle: Chitarra vs Violino
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isGuitarView) ElectricCyan else Color.Transparent)
                        .clickable { onToggleInstrument(true) }
                        .padding(vertical = 10.dp)
                        .testTag("scale_guitar_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tastiera Chitarra (6 Corde)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGuitarView) Color.Black else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isGuitarView) ElectricCyan else Color.Transparent)
                        .clickable { onToggleInstrument(false) }
                        .padding(vertical = 10.dp)
                        .testTag("scale_violin_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Manico Violino (4 Corde)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isGuitarView) Color.Black else TextSecondary
                    )
                }
            }
        }

        // Root Note Selector
        item {
            Column {
                Text(
                    text = "NOTA TONICA (ROOT)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ScaleCalculator.ROOT_NOTES.indices.toList()) { i ->
                        val note = ScaleCalculator.ROOT_NOTES[i]
                        val ita = ScaleCalculator.ROOT_ITALIAN[i]
                        val isSelected = note == selectedRoot

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricCyan else StudioCardSurface)
                                .border(1.dp, if (isSelected) ElectricCyan else StudioBorder, RoundedCornerShape(12.dp))
                                .clickable { onSelectRoot(note) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("root_note_$note"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = note,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextPrimary
                                )
                                Text(
                                    text = ita,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.Black.copy(alpha = 0.8f) else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scale Type Selector
        item {
            Column {
                Text(
                    text = "TIPO DI SCALA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ScaleType.values()) { type ->
                        val isSelected = selectedScaleType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else StudioCardSurface)
                                .border(1.dp, if (isSelected) ElectricCyan else StudioBorder, RoundedCornerShape(12.dp))
                                .clickable { onSelectScaleType(type) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("scale_type_${type.name}")
                        ) {
                            Text(
                                text = type.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Scale Overview Banner & Play Button
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Scala $selectedRoot ${selectedScaleType.title}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = selectedScaleType.mood,
                            fontSize = 12.sp,
                            color = ElectricCyan
                        )
                    }

                    // Play Scale Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldTune)
                            .clickable { onPlayFullScale() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("play_scale_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Riproduci Scala",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ascolta", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes badges row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scaleNotes.forEach { sn ->
                        val isPlaying = activePlayingNote == sn.noteName
                        val badgeColor = when {
                            isPlaying -> GoldAccent
                            sn.isRoot -> ElectricCyan
                            else -> EmeraldTune
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${sn.noteName} (${sn.degreeLabel})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }
            }
        }

        // Interactive Fretboard / Fingerboard Display
        item {
            Text(
                text = if (isGuitarView) "TASTIERA CHITARRA (Tocca le note per ascoltarle)" else "MANICO VIOLINO - 1ª POSIZIONE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isGuitarView) {
                GuitarFretboardView(
                    scaleNoteNames = scaleNoteNames,
                    rootNote = selectedRoot,
                    activePlayingNote = activePlayingNote,
                    onPlayNote = onPlayNote
                )
            } else {
                ViolinFingerboardView(
                    scaleNoteNames = scaleNoteNames,
                    rootNote = selectedRoot,
                    activePlayingNote = activePlayingNote,
                    onPlayNote = onPlayNote
                )
            }
        }

        // Educational Theory Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StudioCardSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Tutorial",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guida Teorica & Consigli di Studio",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedScaleType.explanation,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioCardElevated)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = if (isGuitarView) "💡 Consiglio Chitarra:" else "💡 Consiglio Violino:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Text(
                            text = if (isGuitarView) {
                                "Memorizza la posizione della Tonica (pallino azzurro). Tutte le diteggiature delle scale si muovono lungo il manico mantenendo le stesse relazioni di intervalli."
                            } else {
                                "Presta molta attenzione alla distanza tra 2° e 3° dito: quando c'è un semitono le dita devono toccarsi (dito stretto), quando c'è un tono devono essere staccate."
                            },
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GuitarFretboardView(
    scaleNoteNames: Set<String>,
    rootNote: String,
    activePlayingNote: String?,
    onPlayNote: (Double, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131926))
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Fret Numbers Row (0 to 12)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                    Text("Corda", fontSize = 10.sp, color = TextMuted)
                }
                for (fret in 0..12) {
                    Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (fret == 0) "Vuoto" else "$fret",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (fret in listOf(3, 5, 7, 9, 12)) ElectricCyan else TextMuted
                        )
                    }
                }
            }

            // 6 Strings (E4, B3, G3, D3, A2, E2)
            ScaleCalculator.GUITAR_STRING_MIDIS.forEachIndexed { stringIndex, openMidi ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // String label
                    Box(modifier = Modifier.width(60.dp)) {
                        Text(
                            text = ScaleCalculator.GUITAR_STRING_LABELS[stringIndex].substringBefore(" "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    for (fret in 0..12) {
                        val noteMidi = openMidi + fret
                        val noteInfo = MusicalPitch.getNoteFromMidi(noteMidi)
                        val inScale = scaleNoteNames.contains(noteInfo.name)
                        val isRoot = noteInfo.name == rootNote
                        val isPlaying = activePlayingNote == noteInfo.name

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPlaying -> GoldAccent
                                        isRoot -> ElectricCyan
                                        inScale -> EmeraldTune.copy(alpha = 0.25f)
                                        else -> Color(0xFF1E2638)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isRoot -> ElectricCyan
                                        inScale -> EmeraldTune
                                        else -> Color(0xFF28364F)
                                    },
                                    CircleShape
                                )
                                .clickable {
                                    onPlayNote(noteInfo.frequency, noteInfo.name)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (inScale) {
                                Text(
                                    text = noteInfo.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRoot || isPlaying) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViolinFingerboardView(
    scaleNoteNames: Set<String>,
    rootNote: String,
    activePlayingNote: String?,
    onPlayNote: (Double, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131926))
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Position headers
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.width(70.dp)) {
                    Text("Corda", fontSize = 10.sp, color = TextMuted)
                }
                ScaleCalculator.VIOLIN_POSITIONS.forEach { (_, label) ->
                    Box(modifier = Modifier.width(62.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }
            }

            // 4 Violin Strings (Mi E5, La A4, Re D4, Sol G3)
            ScaleCalculator.VIOLIN_STRING_MIDIS.forEachIndexed { stringIndex, openMidi ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(70.dp)) {
                        Text(
                            text = ScaleCalculator.VIOLIN_STRING_LABELS[stringIndex].substringBefore(" -"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    ScaleCalculator.VIOLIN_POSITIONS.forEach { (semitoneOffset, _) ->
                        val noteMidi = openMidi + semitoneOffset
                        val noteInfo = MusicalPitch.getNoteFromMidi(noteMidi)
                        val inScale = scaleNoteNames.contains(noteInfo.name)
                        val isRoot = noteInfo.name == rootNote
                        val isPlaying = activePlayingNote == noteInfo.name

                        Box(
                            modifier = Modifier
                                .width(62.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isPlaying -> GoldAccent
                                        isRoot -> ElectricCyan
                                        inScale -> EmeraldTune.copy(alpha = 0.25f)
                                        else -> Color(0xFF1E2638)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isRoot -> ElectricCyan
                                        inScale -> EmeraldTune
                                        else -> Color(0xFF28364F)
                                    },
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onPlayNote(noteInfo.frequency, noteInfo.name)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (inScale) "${noteInfo.name} (${noteInfo.italianName})" else "--",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRoot || isPlaying) Color.Black else if (inScale) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
