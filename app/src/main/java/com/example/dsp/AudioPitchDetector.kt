package com.example.dsp

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class TunerAudioState(
    val isRecording: Boolean = false,
    val pitchResult: PitchResult = PitchResult(isValid = false),
    val rmsLevel: Float = 0f,
    val isSignalAboveThreshold: Boolean = false,
    val waveformBuffer: FloatArray = FloatArray(128)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TunerAudioState) return false
        return isRecording == other.isRecording &&
                pitchResult == other.pitchResult &&
                rmsLevel == other.rmsLevel &&
                isSignalAboveThreshold == other.isSignalAboveThreshold
    }

    override fun hashCode(): Int {
        var result = isRecording.hashCode()
        result = 31 * result + pitchResult.hashCode()
        result = 31 * result + rmsLevel.hashCode()
        result = 31 * result + isSignalAboveThreshold.hashCode()
        return result
    }
}

class AudioPitchDetector {
    private val sampleRate = 44100
    private val bufferSize = 4096
    private var audioRecord: AudioRecord? = null
    private var job: Job? = null

    private val _audioState = MutableStateFlow(TunerAudioState())
    val audioState: StateFlow<TunerAudioState> = _audioState.asStateFlow()

    var a4Calibration: Double = 440.0
    var noiseThreshold: Float = 0.015f // Noise gate RMS threshold

    // Stability smoothing buffer
    private val recentPitches = DoubleArray(3)
    private var pitchIndex = 0

    @SuppressLint("MissingPermission")
    fun start(scope: CoroutineScope) {
        if (job != null && job?.isActive == true) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val actualBufferSize = maxOf(minBufferSize, bufferSize * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                actualBufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _audioState.value = _audioState.value.copy(isRecording = true)

            job = scope.launch(Dispatchers.Default) {
                val audioBuffer = ShortArray(bufferSize)
                val floatBuffer = FloatArray(bufferSize)
                val displayWaveform = FloatArray(128)

                while (isActive) {
                    val readSamples = audioRecord?.read(audioBuffer, 0, bufferSize) ?: -1
                    if (readSamples <= 0) continue

                    // Calculate RMS amplitude and convert to float
                    var sumSquares = 0.0
                    for (i in 0 until readSamples) {
                        val norm = audioBuffer[i] / 32768.0f
                        floatBuffer[i] = norm
                        sumSquares += norm * norm
                    }
                    val rms = sqrt(sumSquares / readSamples).toFloat()

                    // Downsample for visualizer display
                    val step = readSamples / displayWaveform.size
                    for (i in displayWaveform.indices) {
                        displayWaveform[i] = floatBuffer[i * step]
                    }

                    val isAboveGate = rms >= noiseThreshold

                    if (isAboveGate) {
                        val detectedFreq = detectPitchYin(floatBuffer, readSamples)
                        if (detectedFreq in 40.0..2500.0) {
                            // Simple median smoothing to avoid glitchy jumps
                            recentPitches[pitchIndex % 3] = detectedFreq
                            pitchIndex++
                            val sorted = recentPitches.clone().apply { sort() }
                            val smoothedFreq = sorted[1]

                            val pitchRes = MusicalPitch.analyzeFrequency(smoothedFreq, a4Calibration)
                            _audioState.value = TunerAudioState(
                                isRecording = true,
                                pitchResult = pitchRes,
                                rmsLevel = rms,
                                isSignalAboveThreshold = true,
                                waveformBuffer = displayWaveform.clone()
                            )
                        } else {
                            _audioState.value = _audioState.value.copy(
                                rmsLevel = rms,
                                isSignalAboveThreshold = true,
                                waveformBuffer = displayWaveform.clone()
                            )
                        }
                    } else {
                        _audioState.value = _audioState.value.copy(
                            rmsLevel = rms,
                            isSignalAboveThreshold = false,
                            waveformBuffer = displayWaveform.clone()
                        )
                    }
                }
            }
        } catch (_: Exception) {
            stop()
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _audioState.value = TunerAudioState(isRecording = false)
    }

    /**
     * YIN Pitch Detection Algorithm
     * Standard implementation with parabolic interpolation
     */
    private fun detectPitchYin(buffer: FloatArray, length: Int): Double {
        val halfLength = length / 2
        val yinBuffer = DoubleArray(halfLength)

        // Step 1: Difference Function
        for (tau in 0 until halfLength) {
            var sum = 0.0
            for (i in 0 until halfLength) {
                val delta = buffer[i] - buffer[i + tau]
                sum += delta * delta
            }
            yinBuffer[tau] = sum
        }

        // Step 2: Cumulative mean normalized difference
        yinBuffer[0] = 1.0
        var runningSum = 0.0
        for (tau in 1 until halfLength) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = (yinBuffer[tau] * tau) / runningSum
        }

        // Step 3: Absolute threshold
        val threshold = 0.15
        var tauEstimate = -1
        for (tau in 2 until halfLength) {
            if (yinBuffer[tau] < threshold) {
                var minTau = tau
                while (minTau + 1 < halfLength && yinBuffer[minTau + 1] < yinBuffer[minTau]) {
                    minTau++
                }
                tauEstimate = minTau
                break
            }
        }

        if (tauEstimate == -1) {
            // Find global minimum if no point fell below threshold
            var minVal = Double.MAX_VALUE
            for (tau in 2 until halfLength) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau]
                    tauEstimate = tau
                }
            }
            if (minVal > 0.4) return -1.0
        }

        // Step 4: Parabolic Interpolation
        val x0 = if (tauEstimate > 0) tauEstimate - 1 else tauEstimate
        val x2 = if (tauEstimate + 1 < halfLength) tauEstimate + 1 else tauEstimate

        val betterTau = if (x0 == tauEstimate) {
            if (yinBuffer[tauEstimate] <= yinBuffer[x2]) tauEstimate.toDouble() else x2.toDouble()
        } else if (x2 == tauEstimate) {
            if (yinBuffer[tauEstimate] <= yinBuffer[x0]) tauEstimate.toDouble() else x0.toDouble()
        } else {
            val s0 = yinBuffer[x0]
            val s1 = yinBuffer[tauEstimate]
            val s2 = yinBuffer[x2]
            val denominator = 2 * (2 * s1 - s2 - s0)
            if (denominator != 0.0) {
                tauEstimate + (s2 - s0) / denominator
            } else {
                tauEstimate.toDouble()
            }
        }

        return if (betterTau > 0) sampleRate / betterTau else -1.0
    }
}
