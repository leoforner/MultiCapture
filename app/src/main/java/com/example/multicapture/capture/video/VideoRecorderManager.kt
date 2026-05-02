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
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        lensOption: CameraLensOption,
        qualityOption: VideoQualityOption
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
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
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = if (lensOption == CameraLensOption.FRONT) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
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

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
