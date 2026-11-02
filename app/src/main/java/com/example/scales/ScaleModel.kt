package com.example.scales

import com.example.dsp.MusicalPitch
import com.example.dsp.NoteInfo

enum class ScaleType(
    val title: String,
    val intervals: IntArray,
    val degreeNames: List<String>,
    val mood: String,
    val explanation: String
) {
    MAJOR(
        title = "Maggiore",
        intervals = intArrayOf(2, 2, 1, 2, 2, 2, 1),
        degreeNames = listOf("Tonica", "2ª", "3ª Magg", "4ª Giusta", "5ª Giusta", "6ª Magg", "7ª Magg"),
        mood = "Luminosa, trionfale, positiva",
        explanation = "La scala fondamentale della musica occidentale. Formula: T - T - S - T - T - T - S. Ideale per comprendere intervalli e armonia."
    ),
    NATURAL_MINOR(
        title = "Minore Naturale",
        intervals = intArrayOf(2, 1, 2, 2, 1, 2, 2),
        degreeNames = listOf("Tonica", "2ª", "3ª Min", "4ª Giusta", "5ª Giusta", "6ª Min", "7ª Min"),
        mood = "Malinconica, introspettiva, epica",
        explanation = "Modo Eolio. Terza, sesta e settima bemolle le donano il classico timbro triste e profondo."
    ),
    HARMONIC_MINOR(
        title = "Minore Armonica",
        intervals = intArrayOf(2, 1, 2, 2, 1, 3, 1),
        degreeNames = listOf("Tonica", "2ª", "3ª Min", "4ª Giusta", "5ª Giusta", "6ª Min", "7ª Magg"),
        mood = "Drammatica, esotica, classica / neoclassica",
        explanation = "Ha la 7ª aumentata (sensibile), creando il caratteristico salto di un tono e mezzo (seconda aumentata) che richiama sonorità klezmer e barocche."
    ),
    PENTATONIC_MINOR(
        title = "Pentatonica Minore",
        intervals = intArrayOf(3, 2, 2, 3, 2),
        degreeNames = listOf("Tonica", "3ª Min", "4ª Giusta", "5ª Giusta", "7ª Min"),
        mood = "Rock, solida, essenziale per assoli",
        explanation = "5 note senza semitoni di tensione (omette la 2ª e la 6ª). La regina indiscussa degli assoli rock e pop per chitarra."
    ),
    BLUES(
        title = "Scala Blues",
        intervals = intArrayOf(3, 2, 1, 1, 3, 2),
        degreeNames = listOf("Tonica", "3ª Min", "4ª Giusta", "Blue Note (b5)", "5ª Giusta", "7ª Min"),
        mood = "Graffiante, blues, espressiva",
        explanation = "Pentatonica minore con l'aggiunta della 'Blue Note' (5ª diminuita). Crea quella tensione cruda tipica del blues e dell'hard rock."
    ),
    PENTATONIC_MAJOR(
        title = "Pentatonica Maggiore",
        intervals = intArrayOf(2, 2, 3, 2, 3),
        degreeNames = listOf("Tonica", "2ª", "3ª Magg", "5ª Giusta", "6ª Magg"),
        mood = "Dolce, country, gospel",
        explanation = "Omette la 4ª e la 7ª. Suono rilassato e melodico, perfetto per il country e le ballate."
    ),
    DORIAN(
        title = "Modo Dorico",
        intervals = intArrayOf(2, 1, 2, 2, 2, 1, 2),
        degreeNames = listOf("Tonica", "2ª", "3ª Min", "4ª Giusta", "5ª Giusta", "6ª Magg", "7ª Min"),
        mood = "Jazz, misteriosa, sognante",
        explanation = "Scala minore con la 6ª maggiore. Molto amata da chitarristi come Santana e violoncellisti/violinisti folk."
    )
}

data class ScaleNote(
    val noteName: String,
    val italianName: String,
    val degreeIndex: Int, // 0 = root
    val degreeLabel: String,
    val isRoot: Boolean
)

object ScaleCalculator {
    val ROOT_NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val ROOT_ITALIAN = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")

    fun computeScaleNotes(rootNote: String, scaleType: ScaleType): List<ScaleNote> {
        val rootIndex = ROOT_NOTES.indexOf(rootNote)
        if (rootIndex == -1) return emptyList()

        val list = mutableListOf<ScaleNote>()
        var currentIndex = rootIndex

        list.add(
            ScaleNote(
                noteName = ROOT_NOTES[rootIndex],
                italianName = ROOT_ITALIAN[rootIndex],
                degreeIndex = 0,
                degreeLabel = "R",
                isRoot = true
            )
        )

        for (i in 0 until scaleType.intervals.size - 1) {
            currentIndex = (currentIndex + scaleType.intervals[i]) % 12
            val degName = if (i + 1 < scaleType.degreeNames.size) scaleType.degreeNames[i + 1] else "${i + 2}"
            list.add(
                ScaleNote(
                    noteName = ROOT_NOTES[currentIndex],
                    italianName = ROOT_ITALIAN[currentIndex],
                    degreeIndex = i + 1,
                    degreeLabel = degName,
                    isRoot = false
                )
            )
        }
        return list
    }

    // Fretboard mapping: Guitar 6 strings (E2, A2, D3, G3, B3, E4)
    val GUITAR_STRING_MIDIS = listOf(64, 59, 55, 50, 45, 40) // 1st to 6th (E4, B3, G3, D3, A2, E2)
    val GUITAR_STRING_LABELS = listOf("1ª - Mi (E4)", "2ª - Si (B3)", "3ª - Sol (G3)", "4ª - Re (D3)", "5ª - La (A2)", "6ª - Mi (E2)")

    // Fingerboard mapping: Violin 4 strings (E5, A4, D4, G3)
    val VIOLIN_STRING_MIDIS = listOf(76, 69, 62, 55) // E5, A4, D4, G3
    val VIOLIN_STRING_LABELS = listOf("1ª Corda - Mi (E5)", "2ª Corda - La (A4)", "3ª Corda - Re (D4)", "4ª Corda - Sol (G3)")

    // Violin 1st position semitone offsets from open string:
    // 0: Open, 1: 1st finger low, 2: 1st finger normal, 3: 2nd finger low, 4: 2nd finger high, 5: 3rd finger, 7: 4th finger
    val VIOLIN_POSITIONS = listOf(
        Pair(0, "Corda Vuota"),
        Pair(2, "1° Dito (normale)"),
        Pair(3, "2° Dito (stretto)"),
        Pair(4, "2° Dito (largo)"),
        Pair(5, "3° Dito"),
        Pair(7, "4° Dito")
    )
}
