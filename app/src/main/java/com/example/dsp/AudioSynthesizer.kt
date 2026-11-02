package com.example.dsp

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class DrumType {
    KICK,
    SNARE,
    HI_HAT_CLOSED,
    HI_HAT_OPEN,
    CLAP
}

class AudioSynthesizer {
    private val sampleRate = 44100
    private var toneJob: Job? = null

    // Pre-synthesized drum samples for instant zero-latency playback
    private val kickBuffer: ShortArray by lazy { generateKick() }
    private val snareBuffer: ShortArray by lazy { generateSnare() }
    private val hiHatClosedBuffer: ShortArray by lazy { generateHiHat(closed = true) }
    private val hiHatOpenBuffer: ShortArray by lazy { generateHiHat(closed = false) }
    private val clickAccentBuffer: ShortArray by lazy { generateClick(accent = true) }
    private val clickRegularBuffer: ShortArray by lazy { generateClick(accent = false) }
    private val inTuneChimeBuffer: ShortArray by lazy { generateInTuneChime() }

    fun playClick(accent: Boolean) {
        val buffer = if (accent) clickAccentBuffer else clickRegularBuffer
        playBuffer(buffer)
    }

    fun playInTuneChime() {
        playBuffer(inTuneChimeBuffer)
    }

    fun playDrum(type: DrumType) {
        val buffer = when (type) {
            DrumType.KICK -> kickBuffer
            DrumType.SNARE -> snareBuffer
            DrumType.HI_HAT_CLOSED -> hiHatClosedBuffer
            DrumType.HI_HAT_OPEN -> hiHatOpenBuffer
            DrumType.CLAP -> snareBuffer
        }
        playBuffer(buffer)
    }

    fun playTone(frequency: Double, durationMs: Long = 800, scope: CoroutineScope) {
        toneJob?.cancel()
        toneJob = scope.launch(Dispatchers.Default) {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            val attackSamples = (sampleRate * 0.03).toInt()
            val releaseSamples = (sampleRate * 0.08).toInt()

            for (i in 0 until numSamples) {
                // Envelope
                val envelope = when {
                    i < attackSamples -> i.toDouble() / attackSamples
                    i > numSamples - releaseSamples -> (numSamples - i).toDouble() / releaseSamples
                    else -> 1.0
                }
                val angle = 2.0 * PI * i * frequency / sampleRate
                val sampleVal = (sin(angle) * 0.7 + sin(angle * 2) * 0.2) * envelope * 32767.0
                buffer[i] = sampleVal.coerceIn(-32768.0, 32767.0).toInt().toShort()
            }
            playBuffer(buffer)
        }
    }

    private fun playBuffer(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Release after playback finishes
            val durationMs = (buffer.size.toDouble() / sampleRate * 1000).toLong() + 50
            CoroutineScope(Dispatchers.Default).launch {
                kotlinx.coroutines.delay(durationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    // Procedural Kick synthesis: fast pitch drop from 140Hz to 48Hz with exponential decay
    private fun generateKick(): ShortArray {
        val durationMs = 280
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val currentFreq = 45.0 + (130.0 - 45.0) * exp(-28.0 * t)
            phase += 2.0 * PI * currentFreq / sampleRate
            val envelope = exp(-12.0 * t)
            val sample = sin(phase) * envelope * 32767.0 * 0.95
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    // Procedural Snare synthesis: 180Hz tone pop combined with shaped white noise
    private fun generateSnare(): ShortArray {
        val durationMs = 220
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            phase += 2.0 * PI * 185.0 / sampleRate
            val tone = sin(phase) * exp(-30.0 * t) * 0.4
            val noise = (Math.random() * 2.0 - 1.0) * exp(-18.0 * t) * 0.6
            val sample = (tone + noise) * 32767.0 * 0.9
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    // Procedural Hi-hat synthesis: filtered high frequency noise
    private fun generateHiHat(closed: Boolean): ShortArray {
        val durationMs = if (closed) 55 else 260
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val decayRate = if (closed) 55.0 else 12.0

        var lastSample = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val rawNoise = Math.random() * 2.0 - 1.0
            // Simple high-pass differentiator
            val hpNoise = rawNoise - lastSample * 0.85
            lastSample = rawNoise
            val envelope = exp(-decayRate * t)
            val sample = hpNoise * envelope * 32767.0 * 0.6
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    // High precision metronome click
    private fun generateClick(accent: Boolean): ShortArray {
        val durationMs = 25
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val frequency = if (accent) 1800.0 else 900.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-180.0 * t)
            val sample = sin(2.0 * PI * frequency * t) * envelope * 32767.0 * (if (accent) 0.95 else 0.75)
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }

    // High quality in-tune confirmation chime: harmonious two-tone bell chime (1046.5Hz C6 + 1318.5Hz E6)
    private fun generateInTuneChime(): ShortArray {
        val durationMs = 280
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val freq1 = 1046.5 // C6
        val freq2 = 1318.5 // E6
        val attackSamples = (sampleRate * 0.005).toInt() // fast 5ms attack

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val attackEnv = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
            val decayEnv = exp(-14.0 * t)
            val envelope = attackEnv * decayEnv
            val s1 = sin(2.0 * PI * freq1 * t) * 0.55
            val s2 = sin(2.0 * PI * freq2 * t) * 0.45
            val sample = (s1 + s2) * envelope * 32767.0 * 0.85
            buffer[i] = sample.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return buffer
    }
}
