package com.example.multicapture.capture

import android.content.Context
import android.graphics.Color
import android.view.SurfaceHolder
import com.example.multicapture.settings.StreamType
import com.pedro.encoder.input.gl.render.filters.`object`.TextObjectFilterRender
import com.pedro.library.generic.GenericCamera2
import com.pedro.library.view.OpenGlView
import com.pedro.common.ConnectChecker

import com.pedro.library.generic.GenericOnlyAudio

class StreamManager(private val context: Context, private val openGlView: OpenGlView) : ConnectChecker {
    
    val genericCamera2: GenericCamera2 = GenericCamera2(openGlView, this)
    val genericCamera2Second: GenericCamera2 = GenericCamera2(context, this) // Headless for second stream
    val genericOnlyAudio: GenericOnlyAudio = GenericOnlyAudio(this)
    private var textOverlayFilter: TextObjectFilterRender? = null
    
    private var isSplitMode = false
    private var isMultiCameraMode = false
    
    override fun onConnectionStarted(url: String) {}
    override fun onConnectionSuccess() {}
    override fun onConnectionFailed(reason: String) {}
    override fun onDisconnect() {}
    override fun onAuthError() {}
    override fun onAuthSuccess() {}
    override fun onNewBitrate(bitrate: Long) {}
    
    fun prepare(streamType: StreamType, splitAudio: Boolean = false, multiCamera: Boolean = false, videoWidth: Int = 1280, videoHeight: Int = 720): Boolean {
        this.isSplitMode = splitAudio
        this.isMultiCameraMode = multiCamera
        var prepared = true
        
        if (streamType == StreamType.AUDIO_AND_VIDEO || streamType == StreamType.VIDEO_ONLY) {
            prepared = prepared && genericCamera2.prepareVideo(videoWidth, videoHeight, 30, 2500 * 1024, 0)
            if (isMultiCameraMode) {
                prepared = prepared && genericCamera2Second.prepareVideo(videoWidth, videoHeight, 30, 2500 * 1024, 0)
            }
        }
        
        if (streamType == StreamType.AUDIO_AND_VIDEO || streamType == StreamType.AUDIO_ONLY) {
            if (isSplitMode) {
                prepared = prepared && genericOnlyAudio.prepareAudio()
            } else {
                prepared = prepared && genericCamera2.prepareAudio()
            }
        }
        
        return prepared
    }
    
    fun startPreview() {
        if (!genericCamera2.isOnPreview) {
            genericCamera2.startPreview()
        }
    }
    
    fun stopPreview() {
        if (genericCamera2.isOnPreview) {
            genericCamera2.stopPreview()
        }
    }

    fun startStream(videoUrl: String, audioUrl: String? = null, videoUrlSecond: String? = null) {
        if (!genericCamera2.isStreaming) {
            genericCamera2.startStream(videoUrl)
        }
        if (isSplitMode && audioUrl != null && !genericOnlyAudio.isStreaming) {
            genericOnlyAudio.startStream(audioUrl)
        }
        if (isMultiCameraMode && videoUrlSecond != null && !genericCamera2Second.isStreaming) {
            genericCamera2Second.startStream(videoUrlSecond)
        }
    }
    
    fun stopStream() {
        if (genericCamera2.isStreaming) {
            genericCamera2.stopStream()
        }
        if (genericOnlyAudio.isStreaming) {
            genericOnlyAudio.stopStream()
        }
        if (genericCamera2Second.isStreaming) {
            genericCamera2Second.stopStream()
        }
    }
    
    fun setupChatOverlay() {
        val filter = TextObjectFilterRender()
        filter.setText("Aguardando mensagens...", 30f, Color.WHITE)
        filter.setDefaultScale(openGlView.width, openGlView.height)
        filter.setPosition(10f, 10f) // Top-left corner
        
        genericCamera2.glInterface.setFilter(filter)
        textOverlayFilter = filter
    }
    
    fun updateChatText(text: String) {
        textOverlayFilter?.setText(text, 30f, Color.WHITE)
    }
}
