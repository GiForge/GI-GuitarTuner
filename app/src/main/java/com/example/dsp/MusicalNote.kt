package com.example.dsp

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class NoteInfo(
    val name: String,         // e.g. "A"
    val italianName: String,  // e.g. "La"
    val octave: Int,          // e.g. 4
    val midiNumber: Int,      // e.g. 69
    val frequency: Double,    // e.g. 440.0
    val fullName: String = "$name$octave"
)

data class InstrumentString(
    val index: Int,           // 1-based, e.g. 1st string = high E, or 6th = low E
    val note: NoteInfo,
    val label: String         // e.g. "6ª Corda - E2"
)

enum class InstrumentType(val title: String) {
    GUITAR("Chitarra"),
    VIOLIN("Violino"),
    CHROMATIC("Cromatica"),
    OTHER("Altri Strumenti")
}

data class TuningPreset(
    val id: String,
    val name: String,
    val instrumentType: InstrumentType,
    val strings: List<String> // list of note full names e.g. ["E2", "A2", "D3", "G3", "B3", "E4"]
)

object MusicalPitch {
    private val NOTE_NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val ITALIAN_NAMES = arrayOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")

    fun getFrequencyForMidi(midi: Int, a4: Double = 440.0): Double {
        return a4 * 2.0.pow((midi - 69) / 12.0)
    }

    fun getNoteFromMidi(midi: Int, a4: Double = 440.0): NoteInfo {
        val noteIndex = (midi % 12 + 12) % 12
        val octave = (midi / 12) - 1
        return NoteInfo(
            name = NOTE_NAMES[noteIndex],
            italianName = ITALIAN_NAMES[noteIndex],
            octave = octave,
            midiNumber = midi,
            frequency = getFrequencyForMidi(midi, a4)
        )
    }

    fun getNoteFromFullName(fullName: String, a4: Double = 440.0): NoteInfo? {
        val regex = Regex("^([A-Ga-g][#b]?)(-?\\d+)$")
        val match = regex.find(fullName.trim()) ?: return null
        var notePart = match.groupValues[1].uppercase()
        val octave = match.groupValues[2].toIntOrNull() ?: 4

        // Convert flats to sharps
        when (notePart) {
            "DB" -> notePart = "C#"
            "EB" -> notePart = "D#"
            "GB" -> notePart = "F#"
            "AB" -> notePart = "G#"
            "BB" -> notePart = "A#"
        }

        val noteIndex = NOTE_NAMES.indexOf(notePart)
        if (noteIndex == -1) return null
        val midi = (octave + 1) * 12 + noteIndex
        return getNoteFromMidi(midi, a4)
    }

    fun analyzeFrequency(freq: Double, a4: Double = 440.0): PitchResult {
        if (freq < 20.0 || freq > 5000.0) {
            return PitchResult(isValid = false)
        }

        val midiExact = 69.0 + 12.0 * log2(freq / a4)
        val nearestMidi = midiExact.roundToInt().coerceIn(12, 127)
        val noteInfo = getNoteFromMidi(nearestMidi, a4)
        val cents = 1200.0 * log2(freq / noteInfo.frequency)

        return PitchResult(
            isValid = true,
            detectedFrequency = freq,
            targetFrequency = noteInfo.frequency,
            centsDeviation = cents,
            note = noteInfo,
            isInTune = kotlin.math.abs(cents) <= 3.0
        )
    }

    val BUILT_IN_TUNINGS = listOf(
        TuningPreset(
            id = "guitar_standard",
            name = "Standard (E A D G B E)",
            instrumentType = InstrumentType.GUITAR,
            strings = listOf("E2", "A2", "D3", "G3", "B3", "E4")
        ),
        TuningPreset(
            id = "guitar_drop_d",
            name = "Drop D (D A D G B E)",
            instrumentType = InstrumentType.GUITAR,
            strings = listOf("D2", "A2", "D3", "G3", "B3", "E4")
        ),
        TuningPreset(
            id = "guitar_dadgad",
            name = "DADGAD (Celtica / Folk)",
            instrumentType = InstrumentType.GUITAR,
            strings = listOf("D2", "A2", "D3", "G3", "A3", "D4")
        ),
        TuningPreset(
            id = "guitar_open_d",
            name = "Open D (D A D F# A D)",
            instrumentType = InstrumentType.GUITAR,
            strings = listOf("D2", "A2", "D3", "F#3", "A3", "D4")
        ),
        TuningPreset(
            id = "guitar_open_g",
            name = "Open G (D G D G B D)",
            instrumentType = InstrumentType.GUITAR,
            strings = listOf("D2", "G2", "D3", "G3", "B3", "D4")
        ),
        TuningPreset(
            id = "guitar_half_step_down",
            name = "Mezzo Tono Sotto (Eb Ab Db Gb Bb Eb)",
            instrumentType = InstrumentType.GUITAR,
            strings = listOf("D#2", "G#2", "C#3", "F#3", "A#3", "D#4")
        ),
        TuningPreset(
            id = "violin_standard",
            name = "Violino Standard (G D A E)",
            instrumentType = InstrumentType.VIOLIN,
            strings = listOf("G3", "D4", "A4", "E5")
        ),
        TuningPreset(
            id = "violin_scordatura_d",
            name = "Violino Sol-Re-La-Re (G D A D)",
            instrumentType = InstrumentType.VIOLIN,
            strings = listOf("G3", "D4", "A4", "D5")
        ),
        TuningPreset(
            id = "viola_standard",
            name = "Viola Standard (C G D A)",
            instrumentType = InstrumentType.OTHER,
            strings = listOf("C3", "G3", "D4", "A4")
        ),
        TuningPreset(
            id = "cello_standard",
            name = "Violoncello Standard (C G D A)",
            instrumentType = InstrumentType.OTHER,
            strings = listOf("C2", "G2", "D3", "A3")
        ),
        TuningPreset(
            id = "ukulele_standard",
            name = "Ukulele Standard (G C E A)",
            instrumentType = InstrumentType.OTHER,
            strings = listOf("G4", "C4", "E4", "A4")
        )
    )
}

data class PitchResult(
    val isValid: Boolean = false,
    val detectedFrequency: Double = 0.0,
    val targetFrequency: Double = 0.0,
    val centsDeviation: Double = 0.0,
    val note: NoteInfo? = null,
    val isInTune: Boolean = false
)
