package com.example.multicapture.capture.video

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
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

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        lensOption: CameraLensOption,
        qualityOption: VideoQualityOption,
        aspectOption: com.example.multicapture.settings.VideoAspectRatioOption = com.example.multicapture.settings.VideoAspectRatioOption.RATIO_16_9
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val aspectRatio = when (aspectOption) {
                com.example.multicapture.settings.VideoAspectRatioOption.RATIO_16_9 -> androidx.camera.core.AspectRatio.RATIO_16_9
                com.example.multicapture.settings.VideoAspectRatioOption.RATIO_4_3 -> androidx.camera.core.AspectRatio.RATIO_4_3
                com.example.multicapture.settings.VideoAspectRatioOption.RATIO_1_1 -> androidx.camera.core.AspectRatio.RATIO_4_3 // Fallback, CameraX doesn't natively support 1:1 AspectRatio enum out of the box on older versions without custom ResolutionSelector
            }

            val preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build()
                .also {
                    it.setSurfaceProvider(surfaceProvider)
                }

            val quality = when (qualityOption) {
                VideoQualityOption.UHD_4K -> Quality.UHD
                VideoQualityOption.FHD_1080P -> Quality.FHD
                VideoQualityOption.HD_720P -> Quality.HD
                VideoQualityOption.SD_480P -> Quality.SD
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(quality))
                .setAspectRatio(aspectRatio)
                .build()

            // Try to enable video stabilization if supported by the device
            val videoCaptureBuilder = VideoCapture.Builder(recorder)
            
            // Note: CameraX 1.3.0+ has setVideoStabilizationEnabled. Since we use 1.3.1, we can call it.
            // Using reflection or safe call to avoid crash if method is not found on older versions by accident, but we declared 1.3.1
            try {
                videoCaptureBuilder.setVideoStabilizationEnabled(true)
            } catch (e: Exception) {
                // Ignore if not supported
            }
            
            videoCapture = videoCaptureBuilder.build()

            val cameraSelector = if (lensOption == CameraLensOption.FRONT) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                
                // Concurrent Camera Check
                val hasConcurrent = false // TODO: implement using cameraProvider.availableConcurrentCameraInfos when fully stable
                if (hasConcurrent) {
                    // Try to bind concurrent front and back if required by settings
                    // val concurrentSelectors = cameraProvider.availableConcurrentCameraSelectors[0]
                    // cameraProvider.bindToLifecycle(listOf(SingleCameraConfig(...), SingleCameraConfig(...)))
                    // We will fallback to single for now to maintain stability in the demo
                }
                
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
                        try { pfd.close() } catch (e: Exception) {}
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
