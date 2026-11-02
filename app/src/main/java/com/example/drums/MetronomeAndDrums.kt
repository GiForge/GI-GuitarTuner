package com.example.drums

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.dsp.AudioSynthesizer
import com.example.dsp.DrumType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class DrumStyle(val displayName: String, val description: String) {
    NONE("Solo Metronomo", "Click metronomo classico senza batteria"),
    ROCK("Rock Standard 4/4", "Cassa, rullante deciso e charleston in ottavi"),
    FUNK("Funk Groove", "Ritmo sincopato con cassa dinamica e charleston aperti"),
    JAZZ("Jazz Swing", "Piatto ride swingato e accenti morbidi"),
    BALLAD("Pop Ballad", "Beat acustico rilassato per canzoni lente"),
    BLUES_SHUFFLE("Blues Shuffle", "Groove terzinato tipico blues e shuffle")
}

data class MetronomeState(
    val isPlaying: Boolean = false,
    val bpm: Int = 120,
    val beatsPerBar: Int = 4,
    val currentBeat: Int = 0, // 0 to beatsPerBar - 1
    val isAccent: Boolean = false,
    val selectedStyle: DrumStyle = DrumStyle.NONE,
    val hapticEnabled: Boolean = true
)

class MetronomeEngine(
    private val context: Context,
    private val synthesizer: AudioSynthesizer
) {
    private val _state = MutableStateFlow(MetronomeState())
    val state: StateFlow<MetronomeState> = _state.asStateFlow()

    private var playbackJob: Job? = null
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Tap tempo history
    private val tapTimestamps = mutableListOf<Long>()

    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(30, 280)
        _state.value = _state.value.copy(bpm = clamped)
    }

    fun setBeatsPerBar(beats: Int) {
        val clamped = beats.coerceIn(2, 8)
        _state.value = _state.value.copy(beatsPerBar = clamped, currentBeat = 0)
    }

    fun setStyle(style: DrumStyle) {
        _state.value = _state.value.copy(selectedStyle = style)
    }

    fun toggleHaptic() {
        _state.value = _state.value.copy(hapticEnabled = !_state.value.hapticEnabled)
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)
        // Keep only recent taps (within 2.5s)
        while (tapTimestamps.size > 1 && (now - tapTimestamps.first() > 3000)) {
            tapTimestamps.removeAt(0)
        }
        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                intervals.add(tapTimestamps[i] - tapTimestamps[i - 1])
            }
            val avgInterval = intervals.average()
            if (avgInterval > 0) {
                val computedBpm = (60000.0 / avgInterval).toInt().coerceIn(30, 280)
                setBpm(computedBpm)
            }
        }
    }

    fun togglePlay(scope: CoroutineScope) {
        if (_state.value.isPlaying) {
            stop()
        } else {
            start(scope)
        }
    }

    fun start(scope: CoroutineScope) {
        stop()
        _state.value = _state.value.copy(isPlaying = true, currentBeat = 0)

        playbackJob = scope.launch(Dispatchers.Default) {
            var step16 = 0 // 16th note subdivision tracker for drum patterns
            var beatQuarter = 0

            while (isActive) {
                val bpm = _state.value.bpm
                val beatsPerBar = _state.value.beatsPerBar
                val style = _state.value.selectedStyle
                val msPerBeat = (60000.0 / bpm).toLong()
                val msPer16th = msPerBeat / 4

                if (style == DrumStyle.NONE) {
                    // Standard Metronome click on quarter notes
                    val isAccent = beatQuarter == 0
                    synthesizer.playClick(isAccent)

                    if (isAccent && _state.value.hapticEnabled) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(30)
                            }
                        } catch (_: Exception) {}
                    }

                    _state.value = _state.value.copy(
                        currentBeat = beatQuarter,
                        isAccent = isAccent
                    )

                    beatQuarter = (beatQuarter + 1) % beatsPerBar
                    delay(msPerBeat)
                } else {
                    // 16th-note drum pattern loop
                    // 16 steps = 1 bar of 4/4
                    val stepInBar = step16 % 16
                    val beatIndex = (stepInBar / 4) % beatsPerBar
                    val isQuarterBeat = stepInBar % 4 == 0

                    if (isQuarterBeat) {
                        val isAccent = beatIndex == 0
                        _state.value = _state.value.copy(
                            currentBeat = beatIndex,
                            isAccent = isAccent
                        )
                        if (isAccent && _state.value.hapticEnabled) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            } catch (_: Exception) {}
                        }
                    }

                    playDrumsForStep(style, stepInBar)
                    step16++
                    delay(msPer16th)
                }
            }
        }
    }

    private fun playDrumsForStep(style: DrumStyle, step: Int) {
        when (style) {
            DrumStyle.ROCK -> {
                // Kick on 0, 8 (beats 1 & 3) + ghost kick on 10
                if (step == 0 || step == 8 || step == 10) synthesizer.playDrum(DrumType.KICK)
                // Snare on 4, 12 (beats 2 & 4)
                if (step == 4 || step == 12) synthesizer.playDrum(DrumType.SNARE)
                // Closed hat on every 8th note (0, 2, 4, 6, 8, 10, 12, 14)
                if (step % 2 == 0) synthesizer.playDrum(DrumType.HI_HAT_CLOSED)
            }
            DrumStyle.FUNK -> {
                // Funk syncopated kicks
                if (step == 0 || step == 3 || step == 6 || step == 10) synthesizer.playDrum(DrumType.KICK)
                // Snare on 4, 12, ghost on 15
                if (step == 4 || step == 12 || step == 15) synthesizer.playDrum(DrumType.SNARE)
                // 16th hats, open hat on 14
                if (step == 14) synthesizer.playDrum(DrumType.HI_HAT_OPEN)
                else synthesizer.playDrum(DrumType.HI_HAT_CLOSED)
            }
            DrumStyle.JAZZ -> {
                // Jazz swing ride pattern on 0, 4, 6, 8, 12, 14
                if (step == 0 || step == 4 || step == 6 || step == 8 || step == 12 || step == 14) {
                    synthesizer.playDrum(DrumType.HI_HAT_CLOSED)
                }
                if (step == 0 || step == 8) synthesizer.playDrum(DrumType.KICK)
                if (step == 4 || step == 12) synthesizer.playDrum(DrumType.SNARE)
            }
            DrumStyle.BALLAD -> {
                if (step == 0 || step == 8) synthesizer.playDrum(DrumType.KICK)
                if (step == 4 || step == 12) synthesizer.playDrum(DrumType.SNARE)
                if (step == 0 || step == 4 || step == 8 || step == 12) synthesizer.playDrum(DrumType.HI_HAT_CLOSED)
            }
            DrumStyle.BLUES_SHUFFLE -> {
                // Shuffle triplet feel approximated on 16th steps (0, 3, 4, 7, 8, 11, 12, 15)
                if (step == 0 || step == 8) synthesizer.playDrum(DrumType.KICK)
                if (step == 4 || step == 12) synthesizer.playDrum(DrumType.SNARE)
                if (step == 0 || step == 3 || step == 4 || step == 7 || step == 8 || step == 11 || step == 12 || step == 15) {
                    synthesizer.playDrum(DrumType.HI_HAT_CLOSED)
                }
            }
            DrumStyle.NONE -> {}
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        _state.value = _state.value.copy(isPlaying = false, currentBeat = 0)
    }
}
