package com.example.data

import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {
    val allPresets: Flow<List<PresetEntity>> = presetDao.getAllPresets()

    suspend fun insertPreset(preset: PresetEntity): Long = presetDao.insertPreset(preset)

    suspend fun deletePreset(preset: PresetEntity) = presetDao.deletePreset(preset)

    suspend fun seedDefaultsIfEmpty() {
        if (presetDao.getCount() == 0) {
            val defaults = listOf(
                PresetEntity(
                    name = "Chitarra Classica / Elettrica (Standard)",
                    instrumentType = "GUITAR",
                    tuningName = "Standard EADGBE",
                    notesFormatted = "E2,A2,D3,G3,B3,E4",
                    a4Frequency = 440.0,
                    noiseThreshold = 0.015f,
                    isCustom = false
                ),
                PresetEntity(
                    name = "Chitarra Drop D (Rock / Metal)",
                    instrumentType = "GUITAR",
                    tuningName = "Drop D",
                    notesFormatted = "D2,A2,D3,G3,B3,E4",
                    a4Frequency = 440.0,
                    noiseThreshold = 0.015f,
                    isCustom = false
                ),
                PresetEntity(
                    name = "Chitarra DADGAD (Folk / Celtica)",
                    instrumentType = "GUITAR",
                    tuningName = "DADGAD",
                    notesFormatted = "D2,A2,D3,G3,A3,D4",
                    a4Frequency = 440.0,
                    noiseThreshold = 0.015f,
                    isCustom = false
                ),
                PresetEntity(
                    name = "Chitarra 432 Hz (Frequenza Aurea)",
                    instrumentType = "GUITAR",
                    tuningName = "Standard 432Hz",
                    notesFormatted = "E2,A2,D3,G3,B3,E4",
                    a4Frequency = 432.0,
                    noiseThreshold = 0.015f,
                    isCustom = false
                ),
                PresetEntity(
                    name = "Violino Standard 440 Hz",
                    instrumentType = "VIOLIN",
                    tuningName = "Standard GDAE",
                    notesFormatted = "G3,D4,A4,E5",
                    a4Frequency = 440.0,
                    noiseThreshold = 0.012f,
                    isCustom = false
                ),
                PresetEntity(
                    name = "Violino Orchestra Europea 442 Hz",
                    instrumentType = "VIOLIN",
                    tuningName = "Orchestrale 442Hz",
                    notesFormatted = "G3,D4,A4,E5",
                    a4Frequency = 442.0,
                    noiseThreshold = 0.012f,
                    isCustom = false
                ),
                PresetEntity(
                    name = "Violino Barocco 415 Hz",
                    instrumentType = "VIOLIN",
                    tuningName = "Barocco 415Hz",
                    notesFormatted = "G3,D4,A4,E5",
                    a4Frequency = 415.0,
                    noiseThreshold = 0.012f,
                    isCustom = false
                ),
                PresetEntity(
                    name = "Ukulele Soprano / Concerto",
                    instrumentType = "OTHER",
                    tuningName = "Standard GCEA",
                    notesFormatted = "G4,C4,E4,A4",
                    a4Frequency = 440.0,
                    noiseThreshold = 0.015f,
                    isCustom = false
                )
            )
            for (p in defaults) {
                presetDao.insertPreset(p)
            }
        }
    }
}
