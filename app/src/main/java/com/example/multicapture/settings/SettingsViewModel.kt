package com.example.multicapture.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.core.content.ContextCompat

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    val audioFormat = repository.audioFormatFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudioFormatOption.AAC)
    val videoQuality = repository.videoQualityFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoQualityOption.FHD_1080P)
    val videoCodec = repository.videoCodecFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoCodecOption.H264)
    val videoAspectRatio = repository.videoAspectRatioFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoAspectRatioOption.RATIO_16_9)
    val cameraLens = repository.cameraLensFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CameraLensOption.BACK)
    val selectedCameraId = repository.selectedCameraIdFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val outputDirUri = repository.outputDirUriFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    private val _availableCameras = MutableStateFlow<List<CameraHardwareInfo>>(emptyList())
    val availableCameras = _availableCameras.asStateFlow()

    val captureMode = repository.captureModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CaptureMode.LOCAL_RECORD)
    val localRecordType = repository.localRecordTypeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalRecordType.AUDIO_AND_VIDEO)
    val streamType = repository.streamTypeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreamType.AUDIO_AND_VIDEO)
    val streamUrl = repository.streamUrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    val enableChatOverlay = repository.enableChatOverlayFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val chatServerUrl = repository.chatServerUrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    val enableBatterySaver = repository.enableBatterySaverFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val enableMacros = repository.enableMacrosFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val macro1Name = repository.macro1NameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Macro 1")
    val macro1Url = repository.macro1UrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val macro2Name = repository.macro2NameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Macro 2")
    val macro2Url = repository.macro2UrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val macro3Name = repository.macro3NameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Macro 3")
    val macro3Url = repository.macro3UrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val macro4Name = repository.macro4NameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Macro 4")
    val macro4Url = repository.macro4UrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setAudioFormat(format: AudioFormatOption) = viewModelScope.launch { repository.setAudioFormat(format) }
    fun setVideoQuality(quality: VideoQualityOption) = viewModelScope.launch { repository.setVideoQuality(quality) }
    fun setVideoCodec(codec: VideoCodecOption) = viewModelScope.launch { repository.setVideoCodec(codec) }
    fun setVideoAspectRatio(ratio: VideoAspectRatioOption) = viewModelScope.launch { repository.setVideoAspectRatio(ratio) }
    fun setCameraLens(lens: CameraLensOption) = viewModelScope.launch { repository.setCameraLens(lens) }
    fun setSelectedCameraId(id: String?) = viewModelScope.launch { repository.setSelectedCameraId(id) }
    fun setOutputDirUri(uri: String) = viewModelScope.launch { repository.setOutputDirUri(uri) }

    fun setCaptureMode(mode: CaptureMode) = viewModelScope.launch { repository.setCaptureMode(mode) }
    fun setLocalRecordType(type: LocalRecordType) = viewModelScope.launch { repository.setLocalRecordType(type) }
    fun setStreamType(type: StreamType) = viewModelScope.launch { repository.setStreamType(type) }
    fun setStreamUrl(url: String) = viewModelScope.launch { repository.setStreamUrl(url) }
    fun setEnableChatOverlay(enable: Boolean) = viewModelScope.launch { repository.setEnableChatOverlay(enable) }
    fun setChatServerUrl(url: String) = viewModelScope.launch { repository.setChatServerUrl(url) }
    fun setEnableBatterySaver(enable: Boolean) = viewModelScope.launch { repository.setEnableBatterySaver(enable) }
    fun setEnableMacros(enable: Boolean) = viewModelScope.launch { repository.setEnableMacros(enable) }
    
    fun setMacro1Name(name: String) = viewModelScope.launch { repository.setMacro1Name(name) }
    fun setMacro1Url(url: String) = viewModelScope.launch { repository.setMacro1Url(url) }
    fun setMacro2Name(name: String) = viewModelScope.launch { repository.setMacro2Name(name) }
    fun setMacro2Url(url: String) = viewModelScope.launch { repository.setMacro2Url(url) }
    fun setMacro3Name(name: String) = viewModelScope.launch { repository.setMacro3Name(name) }
    fun setMacro3Url(url: String) = viewModelScope.launch { repository.setMacro3Url(url) }
    fun setMacro4Name(name: String) = viewModelScope.launch { repository.setMacro4Name(name) }
    fun setMacro4Url(url: String) = viewModelScope.launch { repository.setMacro4Url(url) }

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    fun loadAvailableCameras() {
        val context = getApplication<Application>()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                val cameras = provider.availableCameraInfos.map { info ->
                    val id = Camera2CameraInfo.from(info).cameraId
                    val facing = if (info.lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_FRONT) "Frontal" else "Traseira"
                    CameraHardwareInfo(id, "Câmera $id ($facing)", info.lensFacing)
                }
                _availableCameras.value = cameras
            } catch (e: Exception) {
                // Ignorar em caso de falha de carregamento
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
