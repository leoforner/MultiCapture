package com.example.multicapture.capture.video

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import android.util.Size
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.multicapture.settings.CameraLensOption
import com.example.multicapture.settings.VideoQualityOption
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoRecorderManager(private val context: Context) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var camera: androidx.camera.core.Camera? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        lensOption: CameraLensOption,
        qualityOption: VideoQualityOption,
        aspectOption: com.example.multicapture.settings.VideoAspectRatioOption = com.example.multicapture.settings.VideoAspectRatioOption.RATIO_16_9,
        selectedCameraId: String? = null,
        enableStabilization: Boolean = true
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val resolutionSelector = when (aspectOption) {
                com.example.multicapture.settings.VideoAspectRatioOption.RATIO_16_9 ->
                    ResolutionSelector.Builder()
                        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                        .build()
                com.example.multicapture.settings.VideoAspectRatioOption.RATIO_4_3 ->
                    ResolutionSelector.Builder()
                        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                        .build()
                com.example.multicapture.settings.VideoAspectRatioOption.RATIO_1_1 -> {
                    val squareSize = when (qualityOption) {
                        VideoQualityOption.UHD_4K -> Size(2160, 2160)
                        VideoQualityOption.FHD_1080P -> Size(1080, 1080)
                        VideoQualityOption.HD_720P -> Size(720, 720)
                        VideoQualityOption.SD_480P -> Size(480, 480)
                    }
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(squareSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                        )
                        .build()
                }
            }

            val previewBuilder = Preview.Builder()
                .setResolutionSelector(resolutionSelector)

            if (enableStabilization) {
                androidx.camera.camera2.interop.Camera2Interop.Extender(previewBuilder)
                    .setCaptureRequestOption(
                        android.hardware.camera2.CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                        android.hardware.camera2.CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                    )
            }

            val preview = previewBuilder.build().also {
                it.setSurfaceProvider(surfaceProvider)
            }

            val quality = when (qualityOption) {
                VideoQualityOption.UHD_4K -> Quality.UHD
                VideoQualityOption.FHD_1080P -> Quality.FHD
                VideoQualityOption.HD_720P -> Quality.HD
                VideoQualityOption.SD_480P -> Quality.SD
            }

            val aspectRatioInt = when (aspectOption) {
                com.example.multicapture.settings.VideoAspectRatioOption.RATIO_16_9 -> androidx.camera.core.AspectRatio.RATIO_16_9
                else -> androidx.camera.core.AspectRatio.RATIO_4_3
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(quality))
                .setAspectRatio(aspectRatioInt)
                .build()

            // Try to enable video stabilization if supported by the device
            val videoCaptureBuilder = VideoCapture.Builder(recorder)
            
            // Note: CameraX 1.3.1 VideoCapture Builder doesn't expose setVideoStabilizationEnabled.
            // OIS is automatically managed by the camera device on newer APIs unless explicitly requested via Camera2Interop.
            
            videoCapture = videoCaptureBuilder.build()

            // Determine if selectedCameraId is a physical or logical camera
            val cameraManager = context.getSystemService(android.hardware.camera2.CameraManager::class.java)
            val logicalIds = cameraManager.cameraIdList.toSet()
            val isPhysicalCamera = selectedCameraId != null && selectedCameraId !in logicalIds
            
            if (isPhysicalCamera && selectedCameraId != null) {
                // Physical camera: find the parent logical camera and use Camera2Interop
                // to force the physical lens
                val parentLogicalId = findParentLogicalCamera(cameraManager, selectedCameraId)
                
                val cameraSelector = CameraSelector.Builder()
                    .addCameraFilter { cameraInfos ->
                        val targetId = parentLogicalId ?: selectedCameraId
                        val match = cameraInfos.filter { Camera2CameraInfo.from(it).cameraId == targetId }
                        match.ifEmpty { cameraInfos }
                    }
                    .build()
                
                // Rebuild preview with physical camera ID set via Camera2Interop
                val physicalPreviewBuilder = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                
                androidx.camera.camera2.interop.Camera2Interop.Extender(physicalPreviewBuilder)
                    .setPhysicalCameraId(selectedCameraId)
                
                if (enableStabilization) {
                    androidx.camera.camera2.interop.Camera2Interop.Extender(physicalPreviewBuilder)
                        .setCaptureRequestOption(
                            android.hardware.camera2.CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                            android.hardware.camera2.CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                        )
                }
                
                val physicalPreview = physicalPreviewBuilder.build().also {
                    it.setSurfaceProvider(surfaceProvider)
                }
                
                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, physicalPreview, videoCapture
                    )
                } catch (exc: Exception) {
                    Log.e("VideoRecorderManager", "Physical camera binding failed, falling back", exc)
                    // Fallback to default
                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, videoCapture
                        )
                    } catch (exc2: Exception) {
                        Log.e("VideoRecorderManager", "Fallback binding also failed", exc2)
                    }
                }
            } else {
                // Logical camera or default
                val cameraSelector = if (selectedCameraId != null) {
                    CameraSelector.Builder()
                        .addCameraFilter { cameraInfos ->
                            val exactMatch = cameraInfos.filter { Camera2CameraInfo.from(it).cameraId == selectedCameraId }
                            exactMatch.ifEmpty { cameraInfos }
                        }
                        .build()
                } else {
                    if (lensOption == CameraLensOption.FRONT) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                }
                
                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, videoCapture
                    )
                } catch (exc: Exception) {
                    Log.e("VideoRecorderManager", "Use case binding failed", exc)
                }
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun findParentLogicalCamera(
        cameraManager: android.hardware.camera2.CameraManager,
        physicalCameraId: String
    ): String? {
        for (logicalId in cameraManager.cameraIdList) {
            val chars = cameraManager.getCameraCharacteristics(logicalId)
            if (chars.physicalCameraIds.contains(physicalCameraId)) {
                return logicalId
            }
        }
        return null
    }

    fun startRecording(
        fileUri: Uri,
        onVideoSaved: (Uri?) -> Unit,
        onError: (String) -> Unit
    ) {
        val videoCapture = this.videoCapture ?: return

        val pfd = context.contentResolver.openFileDescriptor(fileUri, "w")
        if (pfd == null) {
            onError("Could not open file descriptor")
            return
        }

        val outputOptions = FileDescriptorOutputOptions.Builder(pfd).build()

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            // NO AUDIO enabled for video recording! Audio is handled separately.
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            onVideoSaved(fileUri)
                        } else {
                            recording?.close()
                            recording = null
                            onError("Video capture ends with error: ${recordEvent.error}")
                        }
                        try { pfd.close() } catch (_: Exception) {}
                    }
                }
            }
    }

    fun stopRecording() {
        recording?.stop()
        recording = null
    }

    fun setFocusAndMetering(x: Float, y: Float, width: Float, height: Float) {
        val currentCamera = this.camera ?: return
        val factory = androidx.camera.core.SurfaceOrientedMeteringPointFactory(width, height)
        val point = factory.createPoint(x, y)
        val action = androidx.camera.core.FocusMeteringAction.Builder(point, androidx.camera.core.FocusMeteringAction.FLAG_AF)
            .addPoint(point, androidx.camera.core.FocusMeteringAction.FLAG_AE)
            .build()
        currentCamera.cameraControl.startFocusAndMetering(action)
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
