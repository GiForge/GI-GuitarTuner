package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tuning_presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val instrumentType: String, // "GUITAR", "VIOLIN", "OTHER"
    val tuningName: String,
    val notesFormatted: String, // e.g. "E2,A2,D3,G3,B3,E4"
    val a4Frequency: Double = 440.0,
    val noiseThreshold: Float = 0.015f,
    val isCustom: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
