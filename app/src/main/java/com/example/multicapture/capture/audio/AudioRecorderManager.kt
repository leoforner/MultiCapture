package com.example.multicapture.capture.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.multicapture.settings.AudioFormatOption

class AudioRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var pfd: ParcelFileDescriptor? = null

    fun startRecording(fileUri: Uri, format: AudioFormatOption) {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            
            val (outputFormat, audioEncoder) = when (format) {
                AudioFormatOption.AAC -> Pair(MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC)
                AudioFormatOption.AMR_NB -> Pair(MediaRecorder.OutputFormat.AMR_NB, MediaRecorder.AudioEncoder.AMR_NB)
                AudioFormatOption.AMR_WB -> Pair(MediaRecorder.OutputFormat.AMR_WB, MediaRecorder.AudioEncoder.AMR_WB)
                AudioFormatOption.OPUS -> Pair(MediaRecorder.OutputFormat.OGG, MediaRecorder.AudioEncoder.OPUS)
                AudioFormatOption.VORBIS -> Pair(MediaRecorder.OutputFormat.WEBM, MediaRecorder.AudioEncoder.VORBIS)
                AudioFormatOption.WAV -> {
                    Log.w("AudioRecorder", "WAV fallback to AAC.")
                    Pair(MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC)
                }
            }
            
            setOutputFormat(outputFormat)
            setAudioEncoder(audioEncoder)
            
            try {
                pfd = context.contentResolver.openFileDescriptor(fileUri, "w")
                setOutputFile(pfd?.fileDescriptor)
                prepare()
                start()
            } catch (e: Exception) {
                Log.e("AudioRecorderManager", "startRecording failed", e)
            }
        }
    }

    fun stopRecording() {
        recorder?.apply {
            try {
                stop()
            } catch (e: Exception) {}
            release()
        }
        recorder = null
        try {
            pfd?.close()
        } catch (e: Exception) {}
        pfd = null
    }
}
