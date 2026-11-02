package com.example.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AudioRecording(
    val file: File,
    val name: String,
    val durationMs: Long,
    val formattedDate: String,
    val sizeBytes: Long
)

data class RecorderState(
    val isRecording: Boolean = false,
    val recordingDurationMs: Long = 0,
    val currentRms: Float = 0f,
    val recordings: List<AudioRecording> = emptyList(),
    val playingFile: File? = null,
    val isPlaying: Boolean = false,
    val playbackProgressMs: Long = 0,
    val playbackDurationMs: Long = 0
)

class AudioRecorderManager(private val context: Context) {
    private val sampleRate = 44100
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playbackProgressJob: Job? = null

    private val _state = MutableStateFlow(RecorderState())
    val state: StateFlow<RecorderState> = _state.asStateFlow()

    private val recordingsDir: File
        get() {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                ?: File(context.filesDir, "recordings")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    init {
        refreshRecordings()
    }

    fun refreshRecordings() {
        val files = recordingsDir.listFiles { file -> file.extension.equals("wav", ignoreCase = true) }
            ?: emptyArray()
        val sorted = files.sortedByDescending { it.lastModified() }
        val list = sorted.map { f ->
            val duration = getWavDuration(f)
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(f.lastModified()))
            AudioRecording(
                file = f,
                name = f.nameWithoutExtension,
                durationMs = duration,
                formattedDate = dateStr,
                sizeBytes = f.length()
            )
        }
        _state.value = _state.value.copy(recordings = list)
    }

    @SuppressLint("MissingPermission")
    fun startRecording(customName: String?, scope: CoroutineScope) {
        if (_state.value.isRecording) return

        stopPlayback()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = if (!customName.isNullOrBlank()) "${customName.trim()}_$timeStamp.wav" else "Registrazione_$timeStamp.wav"
        val outputFile = File(recordingsDir, fileName)

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, 4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _state.value = _state.value.copy(
                isRecording = true,
                recordingDurationMs = 0
            )

            recordingJob = scope.launch(Dispatchers.IO) {
                val pcmBuffer = ShortArray(bufferSize / 2)
                val fos = FileOutputStream(outputFile)
                // Write placeholder 44-byte WAV header
                fos.write(ByteArray(44))

                var totalBytesWritten = 0L
                val startTime = System.currentTimeMillis()

                while (isActive) {
                    val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: -1
                    if (read > 0) {
                        val byteBuffer = ByteArray(read * 2)
                        var sum = 0.0
                        for (i in 0 until read) {
                            val sample = pcmBuffer[i]
                            sum += sample * sample
                            byteBuffer[i * 2] = (sample.toInt() and 0xFF).toByte()
                            byteBuffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                        }
                        fos.write(byteBuffer, 0, byteBuffer.size)
                        totalBytesWritten += byteBuffer.size

                        val rms = kotlin.math.sqrt(sum / read).toFloat() / 32768.0f
                        val duration = System.currentTimeMillis() - startTime
                        _state.value = _state.value.copy(
                            recordingDurationMs = duration,
                            currentRms = rms
                        )
                    }
                }

                fos.flush()
                fos.close()

                // Finalize WAV header with actual lengths
                writeWavHeader(outputFile, totalBytesWritten, sampleRate, 1, 16)
                refreshRecordings()
            }
        } catch (_: Exception) {
            stopRecording()
        }
    }

    fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _state.value = _state.value.copy(isRecording = false, currentRms = 0f)
        refreshRecordings()
    }

    fun playRecording(recording: AudioRecording, scope: CoroutineScope) {
        if (_state.value.playingFile == recording.file && _state.value.isPlaying) {
            pausePlayback()
            return
        }

        stopPlayback()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(recording.file.absolutePath)
                prepare()
                setOnCompletionListener {
                    stopPlayback()
                }
                start()
            }

            val duration = mediaPlayer?.duration?.toLong() ?: 0L
            _state.value = _state.value.copy(
                playingFile = recording.file,
                isPlaying = true,
                playbackDurationMs = duration,
                playbackProgressMs = 0
            )

            playbackProgressJob = scope.launch(Dispatchers.Main) {
                while (isActive && mediaPlayer?.isPlaying == true) {
                    val currentPos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    _state.value = _state.value.copy(playbackProgressMs = currentPos)
                    delay(100)
                }
            }
        } catch (_: Exception) {
            stopPlayback()
        }
    }

    fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _state.value = _state.value.copy(isPlaying = false)
            }
        }
    }

    fun stopPlayback() {
        playbackProgressJob?.cancel()
        playbackProgressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _state.value = _state.value.copy(
            playingFile = null,
            isPlaying = false,
            playbackProgressMs = 0,
            playbackDurationMs = 0
        )
    }

    fun deleteRecording(recording: AudioRecording) {
        if (_state.value.playingFile == recording.file) {
            stopPlayback()
        }
        if (recording.file.exists()) {
            recording.file.delete()
        }
        refreshRecordings()
    }

    fun shareRecording(recording: AudioRecording) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            recording.file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/wav"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Esporta registrazione WAV").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun getWavDuration(file: File): Long {
        return try {
            val raf = RandomAccessFile(file, "r")
            val pcmLength = raf.length() - 44
            raf.close()
            if (pcmLength > 0) {
                // 16-bit mono = 2 bytes per sample
                (pcmLength * 1000) / (sampleRate * 2)
            } else 0
        } catch (_: Exception) {
            0
        }
    }

    private fun writeWavHeader(
        file: File,
        totalAudioLen: Long,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = (sampleRate * channels * bitsPerSample / 8).toLong()

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // Subchunk1Size (16 for PCM)
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat (1 = PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte() // Block align
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        val raf = RandomAccessFile(file, "rw")
        raf.seek(0)
        raf.write(header)
        raf.close()
    }
}
