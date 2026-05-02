package com.example.multicapture.capture

import android.content.Context
import android.graphics.Color
import android.view.SurfaceHolder
import com.example.multicapture.settings.StreamType
import com.pedro.encoder.input.gl.render.filters.`object`.TextObjectFilter
import com.pedro.library.generic.GenericCamera2
import com.pedro.library.view.OpenGlView

class StreamManager(private val context: Context, private val openGlView: OpenGlView) {
    
    val genericCamera2: GenericCamera2 = GenericCamera2(openGlView, context)
    private var textOverlayFilter: TextObjectFilter? = null
    
    fun prepare(streamType: StreamType, videoWidth: Int = 1280, videoHeight: Int = 720): Boolean {
        var prepared = true
        
        if (streamType == StreamType.AUDIO_AND_VIDEO || streamType == StreamType.VIDEO_ONLY) {
            prepared = prepared && genericCamera2.prepareVideo(videoWidth, videoHeight, 30, 2500 * 1024, 0)
        }
        
        if (streamType == StreamType.AUDIO_AND_VIDEO || streamType == StreamType.AUDIO_ONLY) {
            prepared = prepared && genericCamera2.prepareAudio()
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

    fun startStream(url: String) {
        if (!genericCamera2.isStreaming) {
            genericCamera2.startStream(url)
        }
    }
    
    fun stopStream() {
        if (genericCamera2.isStreaming) {
            genericCamera2.stopStream()
        }
    }
    
    fun setupChatOverlay() {
        textOverlayFilter = TextObjectFilter()
        textOverlayFilter?.setText("Aguardando mensagens...", 30f, Color.WHITE)
        textOverlayFilter?.setDefaultScale(openGlView.width, openGlView.height)
        textOverlayFilter?.setPosition(10f, 10f) // Top-left corner
        
        genericCamera2.glInterface.setFilter(textOverlayFilter)
    }
    
    fun updateChatText(text: String) {
        textOverlayFilter?.setText(text, 30f, Color.WHITE)
    }
}
