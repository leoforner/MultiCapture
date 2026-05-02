package com.example.multicapture.capture

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.multicapture.capture.audio.AudioRecorderManager
import com.example.multicapture.capture.video.VideoRecorderManager
import com.example.multicapture.settings.AudioFormatOption
import com.example.multicapture.settings.LocalRecordType
import com.example.multicapture.settings.VideoCodecOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.multicapture.macros.MacroManager

class CaptureViewModel(application: Application) : AndroidViewModel(application) {
    val audioManager = AudioRecorderManager(application)
    val videoManager = VideoRecorderManager(application)

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

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
            }
        } else {
            Toast.makeText(app, "Erro ao criar arquivos", Toast.LENGTH_SHORT).show()
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
