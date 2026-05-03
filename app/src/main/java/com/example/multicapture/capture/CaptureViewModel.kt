package com.example.multicapture.capture

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.multicapture.capture.audio.AudioRecorderManager
import com.example.multicapture.capture.video.VideoRecorderManager
import com.example.multicapture.settings.AudioFormatOption
import com.example.multicapture.settings.LocalRecordType
import com.example.multicapture.settings.VideoCodecOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.viewModelScope
import com.example.multicapture.macros.MacroManager

class CaptureViewModel(application: Application) : AndroidViewModel(application) {
    val audioManager = AudioRecorderManager(application)
    val videoManager = VideoRecorderManager(application)

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()
    
    private val _currentAudioLevel = MutableStateFlow(0.0f)
    val currentAudioLevel = _currentAudioLevel.asStateFlow()

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel = _batteryLevel.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) {
                _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
            }
        }
    }

    init {
        application.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun startRecording(
        outputDirUri: String?,
        audioFormat: AudioFormatOption,
        videoCodec: VideoCodecOption,
        recordType: LocalRecordType
    ) {
        val app = getApplication<Application>()
        
        var videoUri: android.net.Uri? = null
        var audioUri: android.net.Uri? = null
        
        if (recordType == LocalRecordType.AUDIO_AND_VIDEO || recordType == LocalRecordType.VIDEO_ONLY) {
            val videoExt = ".mp4"
            val videoMime = "video/mp4"
            videoUri = FileHelper.createVideoFile(app, outputDirUri, videoExt, videoMime)
        }
        
        if (recordType == LocalRecordType.AUDIO_AND_VIDEO || recordType == LocalRecordType.AUDIO_ONLY) {
            audioUri = FileHelper.createAudioFile(app, outputDirUri, audioFormat.extension, audioFormat.mimeType)
        }

        val canStartVideo = (recordType != LocalRecordType.AUDIO_ONLY && videoUri != null)
        val canStartAudio = (recordType != LocalRecordType.VIDEO_ONLY && audioUri != null)

        if (canStartVideo || canStartAudio) {
            _isRecording.value = true
            
            if (canStartVideo) {
                videoManager.startRecording(videoUri!!, 
                    onVideoSaved = { },
                    onError = {
                        Toast.makeText(app, "Erro Vídeo: $it", Toast.LENGTH_LONG).show()
                    }
                )
            }
            
            if (canStartAudio) {
                audioManager.startRecording(audioUri!!, audioFormat)
                startAudioLevelMonitor()
            }
        } else {
            Toast.makeText(app, "Erro ao criar arquivos", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startAudioLevelMonitor() {
        viewModelScope.launch {
            while (_isRecording.value) {
                val maxAmp = audioManager.getMaxAmplitude()
                // maxAmp is roughly 0 to 32767
                val level = (maxAmp / 32767f).coerceIn(0f, 1f)
                _currentAudioLevel.value = level
                delay(100)
            }
            _currentAudioLevel.value = 0f
        }
    }

    fun stopRecording() {
        if (_isRecording.value) {
            videoManager.stopRecording()
            audioManager.stopRecording()
            _isRecording.value = false
            Toast.makeText(getApplication(), "Arquivos Salvos!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {}
        videoManager.shutdown()
        audioManager.stopRecording()
    }

    fun triggerMacro(url: String) {
        MacroManager.triggerMacro(
            url = url,
            onSuccess = {
                // Notificando sucesso em background via handler da main thread ou só ignorar (MacroManager já lida em background)
            },
            onError = { err ->
                // Tratamento de erro leve
            }
        )
    }
}
