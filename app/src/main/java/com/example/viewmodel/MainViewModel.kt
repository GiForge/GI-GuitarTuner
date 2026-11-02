package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PresetEntity
import com.example.data.PresetRepository
import com.example.drums.DrumStyle
import com.example.drums.MetronomeEngine
import com.example.drums.MetronomeState
import com.example.dsp.AudioPitchDetector
import com.example.dsp.AudioSynthesizer
import com.example.dsp.InstrumentType
import com.example.dsp.MusicalPitch
import com.example.dsp.TunerAudioState
import com.example.dsp.TuningPreset
import com.example.recorder.AudioRecorderManager
import com.example.recorder.RecorderState
import com.example.scales.ScaleCalculator
import com.example.scales.ScaleNote
import com.example.scales.ScaleType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TunerSettings(
    val a4Frequency: Double = 440.0,
    val noiseGateThreshold: Float = 0.015f,
    val selectedTuning: TuningPreset = MusicalPitch.BUILT_IN_TUNINGS[0],
    val lockedStringIndex: Int? = null, // null = Auto string detection
    val isAutoString: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = PresetRepository(database.presetDao())

    val audioSynthesizer = AudioSynthesizer()
    val pitchDetector = AudioPitchDetector()
    val metronomeEngine = MetronomeEngine(application, audioSynthesizer)
    val recorderManager = AudioRecorderManager(application)

    // Room DB presets
    val presets: StateFlow<List<PresetEntity>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tuner Settings State
    private val _tunerSettings = MutableStateFlow(TunerSettings())
    val tunerSettings: StateFlow<TunerSettings> = _tunerSettings.asStateFlow()

    // Tuner Pitch State from AudioPitchDetector
    val tunerAudioState: StateFlow<TunerAudioState> = pitchDetector.audioState

    // Metronome State
    val metronomeState: StateFlow<MetronomeState> = metronomeEngine.state

    // Recorder State
    val recorderState: StateFlow<RecorderState> = recorderManager.state

    // Scales State
    private val _selectedScaleRoot = MutableStateFlow("A")
    val selectedScaleRoot: StateFlow<String> = _selectedScaleRoot.asStateFlow()

    private val _selectedScaleType = MutableStateFlow(ScaleType.PENTATONIC_MINOR)
    val selectedScaleType: StateFlow<ScaleType> = _selectedScaleType.asStateFlow()

    private val _activePlayingNote = MutableStateFlow<String?>(null)
    val activePlayingNote: StateFlow<String?> = _activePlayingNote.asStateFlow()

    private val _isGuitarScaleView = MutableStateFlow(true) // true = Guitar, false = Violin
    val isGuitarScaleView: StateFlow<Boolean> = _isGuitarScaleView.asStateFlow()

    private var scalePlaybackJob: Job? = null

    private var wasInTune = false
    private var inTuneConsecutiveCount = 0
    private var lastChimeTime = 0L

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
        }
        pitchDetector.a4Calibration = _tunerSettings.value.a4Frequency
        pitchDetector.noiseThreshold = _tunerSettings.value.noiseGateThreshold

        // Confirmation chime when reaching the exact in-tune pitch
        viewModelScope.launch {
            pitchDetector.audioState.collect { audioState ->
                val isInTune = audioState.isSignalAboveThreshold &&
                        audioState.pitchResult.isValid &&
                        audioState.pitchResult.isInTune
                val now = System.currentTimeMillis()

                if (isInTune) {
                    inTuneConsecutiveCount++
                    if (!wasInTune && inTuneConsecutiveCount >= 2 && (now - lastChimeTime > 1500)) {
                        wasInTune = true
                        lastChimeTime = now
                        audioSynthesizer.playInTuneChime()
                    }
                } else {
                    if (!audioState.isSignalAboveThreshold || kotlin.math.abs(audioState.pitchResult.centsDeviation) > 6.0) {
                        wasInTune = false
                        inTuneConsecutiveCount = 0
                    }
                }
            }
        }
    }

    // --- Tuner Methods ---
    fun startTuner() {
        pitchDetector.start(viewModelScope)
    }

    fun stopTuner() {
        pitchDetector.stop()
    }

    fun setA4Frequency(freq: Double) {
        val clamped = freq.coerceIn(410.0, 466.0)
        _tunerSettings.value = _tunerSettings.value.copy(a4Frequency = clamped)
        pitchDetector.a4Calibration = clamped
    }

    fun setNoiseGateThreshold(threshold: Float) {
        val clamped = threshold.coerceIn(0.002f, 0.08f)
        _tunerSettings.value = _tunerSettings.value.copy(noiseGateThreshold = clamped)
        pitchDetector.noiseThreshold = clamped
    }

    fun selectTuning(preset: TuningPreset) {
        _tunerSettings.value = _tunerSettings.value.copy(
            selectedTuning = preset,
            lockedStringIndex = null,
            isAutoString = true
        )
    }

    fun toggleLockString(index: Int?) {
        if (_tunerSettings.value.lockedStringIndex == index) {
            _tunerSettings.value = _tunerSettings.value.copy(lockedStringIndex = null, isAutoString = true)
        } else {
            _tunerSettings.value = _tunerSettings.value.copy(lockedStringIndex = index, isAutoString = index == null)
        }
    }

    fun playReferenceTone(noteName: String) {
        val note = MusicalPitch.getNoteFromFullName(noteName, _tunerSettings.value.a4Frequency)
        note?.let {
            audioSynthesizer.playTone(it.frequency, durationMs = 1200, scope = viewModelScope)
        }
    }

    // --- Preset Database Methods ---
    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            val currentTuning = _tunerSettings.value.selectedTuning
            val entity = PresetEntity(
                name = name,
                instrumentType = currentTuning.instrumentType.name,
                tuningName = currentTuning.name,
                notesFormatted = currentTuning.strings.joinToString(","),
                a4Frequency = _tunerSettings.value.a4Frequency,
                noiseThreshold = _tunerSettings.value.noiseGateThreshold,
                isCustom = true
            )
            repository.insertPreset(entity)
        }
    }

    fun applyPreset(preset: PresetEntity) {
        val strings = preset.notesFormatted.split(",").map { it.trim() }
        val instType = try {
            InstrumentType.valueOf(preset.instrumentType)
        } catch (_: Exception) {
            InstrumentType.OTHER
        }
        val tuning = TuningPreset(
            id = "custom_${preset.id}",
            name = preset.tuningName.ifBlank { preset.name },
            instrumentType = instType,
            strings = strings
        )
        _tunerSettings.value = _tunerSettings.value.copy(
            selectedTuning = tuning,
            a4Frequency = preset.a4Frequency,
            noiseGateThreshold = preset.noiseThreshold,
            lockedStringIndex = null,
            isAutoString = true
        )
        pitchDetector.a4Calibration = preset.a4Frequency
        pitchDetector.noiseThreshold = preset.noiseThreshold
    }

    fun deletePreset(preset: PresetEntity) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }

    // --- Metronome / Drum Loops ---
    fun toggleMetronome() {
        metronomeEngine.togglePlay(viewModelScope)
    }

    fun setMetronomeBpm(bpm: Int) {
        metronomeEngine.setBpm(bpm)
    }

    fun setBeatsPerBar(beats: Int) {
        metronomeEngine.setBeatsPerBar(beats)
    }

    fun setDrumStyle(style: DrumStyle) {
        metronomeEngine.setStyle(style)
    }

    fun tapTempo() {
        metronomeEngine.tapTempo()
    }

    fun toggleMetronomeHaptic() {
        metronomeEngine.toggleHaptic()
    }

    // --- Scales ---
    fun setScaleRoot(root: String) {
        _selectedScaleRoot.value = root
    }

    fun setScaleType(type: ScaleType) {
        _selectedScaleType.value = type
    }

    fun toggleInstrumentScaleView(isGuitar: Boolean) {
        _isGuitarScaleView.value = isGuitar
    }

    fun playScaleNote(freq: Double, noteName: String) {
        _activePlayingNote.value = noteName
        audioSynthesizer.playTone(freq, 600, viewModelScope)
        viewModelScope.launch {
            delay(500)
            if (_activePlayingNote.value == noteName) {
                _activePlayingNote.value = null
            }
        }
    }

    fun playFullScale() {
        scalePlaybackJob?.cancel()
        val root = _selectedScaleRoot.value
        val type = _selectedScaleType.value
        val notes = ScaleCalculator.computeScaleNotes(root, type)
        if (notes.isEmpty()) return

        scalePlaybackJob = viewModelScope.launch {
            // Ascending
            for (note in notes) {
                val fullNote = "${note.noteName}4"
                val nInfo = MusicalPitch.getNoteFromFullName(fullNote, _tunerSettings.value.a4Frequency)
                val freq = nInfo?.frequency ?: 440.0
                _activePlayingNote.value = note.noteName
                audioSynthesizer.playTone(freq, 400, viewModelScope)
                delay(450)
            }
            // Descending
            for (i in notes.size - 2 downTo 0) {
                val note = notes[i]
                val fullNote = "${note.noteName}4"
                val nInfo = MusicalPitch.getNoteFromFullName(fullNote, _tunerSettings.value.a4Frequency)
                val freq = nInfo?.frequency ?: 440.0
                _activePlayingNote.value = note.noteName
                audioSynthesizer.playTone(freq, 400, viewModelScope)
                delay(450)
            }
            _activePlayingNote.value = null
        }
    }

    // --- Recording ---
    fun startRecording(name: String? = null) {
        recorderManager.startRecording(name, viewModelScope)
    }

    fun stopRecording() {
        recorderManager.stopRecording()
    }

    fun playRecording(recording: com.example.recorder.AudioRecording) {
        recorderManager.playRecording(recording, viewModelScope)
    }

    fun deleteRecording(recording: com.example.recorder.AudioRecording) {
        recorderManager.deleteRecording(recording)
    }

    fun shareRecording(recording: com.example.recorder.AudioRecording) {
        recorderManager.shareRecording(recording)
    }

    override fun onCleared() {
        super.onCleared()
        pitchDetector.stop()
        metronomeEngine.stop()
        recorderManager.stopPlayback()
        recorderManager.stopRecording()
    }
}
