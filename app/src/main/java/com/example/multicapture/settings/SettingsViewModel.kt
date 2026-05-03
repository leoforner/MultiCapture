package com.example.multicapture.settings

import android.app.Application
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    val audioFormat = repository.audioFormatFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudioFormatOption.AAC)
    val videoQuality = repository.videoQualityFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoQualityOption.FHD_1080P)
    val videoCodec = repository.videoCodecFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoCodecOption.H264)
    val videoAspectRatio = repository.videoAspectRatioFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoAspectRatioOption.RATIO_16_9)
    val cameraLens = repository.cameraLensFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CameraLensOption.BACK)
    val selectedCameraId = repository.selectedCameraIdFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val selectedMicrophoneId = repository.selectedMicrophoneIdFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val outputDirUri = repository.outputDirUriFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    private val _availableCameras = MutableStateFlow<List<CameraHardwareInfo>>(emptyList())
    val availableCameras = _availableCameras.asStateFlow()

    private val _availableMicrophones = MutableStateFlow<List<MicrophoneInfo>>(emptyList())
    val availableMicrophones = _availableMicrophones.asStateFlow()

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
    fun setSelectedMicrophoneId(id: Int?) = viewModelScope.launch { repository.setSelectedMicrophoneId(id) }
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

    private val _supportsConcurrentCameras = MutableStateFlow(false)
    val supportsConcurrentCameras = _supportsConcurrentCameras.asStateFlow()

    /**
     * Detects all cameras using Camera2 API natively.
     * Discovers PHYSICAL camera IDs that are grouped under logical cameras
     * (e.g. macro, ultrawide lenses on the Poco X6 Pro).
     */
    fun loadAvailableCameras() {
        val context = getApplication<Application>()
        val cameraManager = context.getSystemService(CameraManager::class.java)
        
        try {
            val cameras = mutableListOf<CameraHardwareInfo>()
            
            for (logicalCameraId in cameraManager.cameraIdList) {
                val logicalChars = cameraManager.getCameraCharacteristics(logicalCameraId)
                val logicalFacing = logicalChars.get(CameraCharacteristics.LENS_FACING) ?: continue
                
                // Get physical camera IDs grouped under this logical camera
                val physicalIds = logicalChars.physicalCameraIds
                
                if (physicalIds.isNotEmpty()) {
                    // This logical camera has physical sub-cameras (macro, ultrawide, etc.)
                    // Add each physical camera separately
                    for (physicalId in physicalIds) {
                        try {
                            val physChars = cameraManager.getCameraCharacteristics(physicalId)
                            val cam = buildCameraInfo(physicalId, physChars, logicalFacing, logicalCameraId)
                            cameras.add(cam)
                        } catch (_: Exception) {
                            // Some physical IDs may not be directly accessible
                        }
                    }
                    // Also add the logical camera itself as "Auto" option
                    val logicalCam = buildCameraInfo(logicalCameraId, logicalChars, logicalFacing, null)
                    cameras.add(0, logicalCam.copy(
                        name = "Auto ${if (logicalFacing == CameraCharacteristics.LENS_FACING_FRONT) "(Frontal)" else "(Traseira)"} — ID $logicalCameraId",
                        lensType = "Auto"
                    ))
                } else {
                    // Simple camera with no physical sub-cameras
                    val cam = buildCameraInfo(logicalCameraId, logicalChars, logicalFacing, null)
                    cameras.add(cam)
                }
            }
            
            _availableCameras.value = cameras
            
            // Check concurrent camera support (needed for PiP)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val concurrentIds = cameraManager.concurrentCameraIds
                _supportsConcurrentCameras.value = concurrentIds.any { it.size >= 2 }
            }
        } catch (_: Exception) {
            // Camera access error — device might not grant camera permission yet
        }
    }
    
    private fun buildCameraInfo(
        cameraId: String,
        characteristics: CameraCharacteristics,
        facing: Int,
        parentLogicalId: String?
    ): CameraHardwareInfo {
        val facingName = when (facing) {
            CameraCharacteristics.LENS_FACING_FRONT -> "Frontal"
            CameraCharacteristics.LENS_FACING_BACK -> "Traseira"
            else -> "Externa"
        }
        
        val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val primaryFocal = focalLengths?.firstOrNull() ?: 0f
        
        val lensType = classifyLens(primaryFocal, facing)
        
        val streamConfigMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val outputSizes = streamConfigMap?.getOutputSizes(android.graphics.ImageFormat.JPEG)
        val resolutionsStr = outputSizes
            ?.sortedByDescending { it.width * it.height }
            ?.take(3)
            ?.joinToString(", ") { "${it.width}x${it.height}" }
            ?: "N/A"
        
        val parentInfo = if (parentLogicalId != null) " [Lógica: $parentLogicalId]" else ""
        
        return CameraHardwareInfo(
            id = cameraId,
            name = "$lensType ($facingName) — ID $cameraId$parentInfo",
            lensFacing = facing,
            lensType = lensType,
            focalLength = primaryFocal,
            resolutions = resolutionsStr
        )
    }
    
    private fun classifyLens(focalLength: Float, facing: Int): String {
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) return "Frontal"
        return when {
            focalLength <= 0f -> "Desconhecida"
            focalLength < 2.5f -> "Ultrawide"
            focalLength in 2.5f..6.0f -> "Principal"
            focalLength in 6.0f..15.0f -> "Telefoto"
            focalLength > 15.0f -> "Super Telefoto"
            else -> "Principal"
        }
    }

    /**
     * Detects all audio input devices including USB microphones and Bluetooth headsets.
     */
    fun loadAvailableMicrophones() {
        val context = getApplication<Application>()
        val audioManager = context.getSystemService(AudioManager::class.java)
        
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        val microphones = devices.map { device ->
            val typeName = when (device.type) {
                AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Microfone Interno"
                AudioDeviceInfo.TYPE_USB_DEVICE -> "USB"
                AudioDeviceInfo.TYPE_USB_HEADSET -> "Headset USB"
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Headset com Fio"
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
                AudioDeviceInfo.TYPE_USB_ACCESSORY -> "Acessório USB"
                else -> "Outro (${device.type})"
            }
            val isExternal = device.type != AudioDeviceInfo.TYPE_BUILTIN_MIC
            val productName = device.productName?.toString()?.ifBlank { null }
            val displayName = productName ?: typeName
            
            MicrophoneInfo(
                id = device.id,
                name = displayName,
                type = typeName,
                isExternal = isExternal
            )
        }
        
        _availableMicrophones.value = microphones
    }
}
