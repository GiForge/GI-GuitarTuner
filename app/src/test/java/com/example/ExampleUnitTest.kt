package com.example

import com.example.dsp.MusicalPitch
import com.example.scales.ScaleCalculator
import com.example.scales.ScaleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {
    @Test
    fun testStandardA4PitchAnalysis() {
        val result = MusicalPitch.analyzeFrequency(440.0, a4 = 440.0)
        assertTrue(result.isValid)
        assertEquals("A", result.note?.name)
        assertEquals(4, result.note?.octave)
        assertTrue(abs(result.centsDeviation) < 0.1)
        assertTrue(result.isInTune)
    }

    @Test
    fun testCustomA4Calibration432Hz() {
        val result = MusicalPitch.analyzeFrequency(432.0, a4 = 432.0)
        assertTrue(result.isValid)
        assertEquals("A", result.note?.name)
        assertEquals(4, result.note?.octave)
        assertTrue(abs(result.centsDeviation) < 0.1)
        assertTrue(result.isInTune)
    }

    @Test
    fun testGuitarAndViolinOpenStrings() {
        // Guitar E2 = 82.41 Hz
        val noteE2 = MusicalPitch.getNoteFromFullName("E2")
        assertNotNull(noteE2)
        assertEquals(40, noteE2!!.midiNumber)
        assertTrue(abs(noteE2.frequency - 82.41) < 0.1)

        // Violin E5 = 659.25 Hz
        val noteE5 = MusicalPitch.getNoteFromFullName("E5")
        assertNotNull(noteE5)
        assertEquals(76, noteE5!!.midiNumber)
        assertTrue(abs(noteE5.frequency - 659.25) < 0.1)
    }

    @Test
    fun testScaleCalculator() {
        // C Major scale should be: C, D, E, F, G, A, B
        val cMajor = ScaleCalculator.computeScaleNotes("C", ScaleType.MAJOR)
        val noteNames = cMajor.map { it.noteName }
        assertEquals(listOf("C", "D", "E", "F", "G", "A", "B"), noteNames)

        // A Minor Pentatonic: A, C, D, E, G
        val aPentMinor = ScaleCalculator.computeScaleNotes("A", ScaleType.PENTATONIC_MINOR)
        val aPentNames = aPentMinor.map { it.noteName }
        assertEquals(listOf("A", "C", "D", "E", "G"), aPentNames)

        // A Blues: A, C, D, D#, E, G
        val aBlues = ScaleCalculator.computeScaleNotes("A", ScaleType.BLUES)
        val aBluesNames = aBlues.map { it.noteName }
        assertEquals(listOf("A", "C", "D", "D#", "E", "G"), aBluesNames)
    }
}
