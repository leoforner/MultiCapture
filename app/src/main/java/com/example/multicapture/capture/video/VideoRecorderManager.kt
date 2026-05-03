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

            val cameraSelector = if (selectedCameraId != null) {
                // When a specific physical camera ID is selected, do NOT filter by lens facing
                // because the system may group physical lenses (macro/ultrawide) under a
                // logical camera that doesn't match the expected facing filter.
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

        }, ContextCompat.getMainExecutor(context))
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
