package com.example.multicapture.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multicapture.capture.CaptureViewModel
import com.example.multicapture.capture.StreamManager
import com.example.multicapture.chat.ChatWebSocketClient
import com.example.multicapture.settings.CaptureMode
import com.example.multicapture.settings.SettingsViewModel
import com.example.multicapture.ui.HUDOverlay
import com.pedro.library.view.OpenGlView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToFormatInfo: () -> Unit,
    captureViewModel: CaptureViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as? Activity

    // Settings
    val captureMode by settingsViewModel.captureMode.collectAsState()
    val localRecordType by settingsViewModel.localRecordType.collectAsState()
    val streamType by settingsViewModel.streamType.collectAsState()
    val streamUrl by settingsViewModel.streamUrl.collectAsState()
    val audioFormat by settingsViewModel.audioFormat.collectAsState()
    val videoQuality by settingsViewModel.videoQuality.collectAsState()
    val videoCodec by settingsViewModel.videoCodec.collectAsState()
    val videoAspectRatio by settingsViewModel.videoAspectRatio.collectAsState()
    val cameraLens by settingsViewModel.cameraLens.collectAsState()
    val outputDirUri by settingsViewModel.outputDirUri.collectAsState()

    val enableChatOverlay by settingsViewModel.enableChatOverlay.collectAsState()
    val chatServerUrl by settingsViewModel.chatServerUrl.collectAsState()
    val enableBatterySaver by settingsViewModel.enableBatterySaver.collectAsState()
    val enableMacros by settingsViewModel.enableMacros.collectAsState()
    
    val macro1Name by settingsViewModel.macro1Name.collectAsState()
    val macro1Url by settingsViewModel.macro1Url.collectAsState()
    val macro2Name by settingsViewModel.macro2Name.collectAsState()
    val macro2Url by settingsViewModel.macro2Url.collectAsState()
    val macro3Name by settingsViewModel.macro3Name.collectAsState()
    val macro3Url by settingsViewModel.macro3Url.collectAsState()
    val macro4Name by settingsViewModel.macro4Name.collectAsState()
    val macro4Url by settingsViewModel.macro4Url.collectAsState()

    // State
    val isRecordingLocal by captureViewModel.isRecording.collectAsState()
    var isStreaming by remember { mutableStateOf(false) }
    val isActionActive = isRecordingLocal || isStreaming

    // Stream & Chat
    val openGlView = remember { OpenGlView(context) }
    val streamManager = remember { StreamManager(context, openGlView) }
    val chatClient = remember { ChatWebSocketClient() }
    val chatMessages by chatClient.messages.collectAsState()

    // Permissions
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions[Manifest.permission.CAMERA] == true &&
                         permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    // Battery Saver Logic & Foreground Service
    LaunchedEffect(isActionActive, enableBatterySaver) {
        // Battery Saver
        activity?.window?.let { window ->
            val layoutParams = window.attributes
            if (isActionActive && enableBatterySaver) {
                layoutParams.screenBrightness = 0.01f
            } else {
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
            window.attributes = layoutParams
        }
        
        // Foreground Service
        val serviceIntent = android.content.Intent(context, com.example.multicapture.capture.CaptureService::class.java)
        if (isActionActive) {
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            serviceIntent.action = "STOP_SERVICE"
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    // Chat Updates Logic
    LaunchedEffect(chatMessages) {
        if (enableChatOverlay && isStreaming) {
            streamManager.updateChatText(chatMessages)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isStreaming) {
                streamManager.stopStream()
                chatClient.disconnect()
            }
            if (isRecordingLocal) {
                captureViewModel.stopRecording()
            }
            streamManager.stopPreview()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { padding ->
        if (hasPermissions) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Don't apply padding so it draws fullscreen
            ) {
                // Camera View
                val showCamera = if (captureMode == CaptureMode.LOCAL_RECORD) {
                    localRecordType != com.example.multicapture.settings.LocalRecordType.AUDIO_ONLY
                } else {
                    streamType != com.example.multicapture.settings.StreamType.AUDIO_ONLY
                }

                if (showCamera) {
                    if (captureMode == CaptureMode.LOCAL_RECORD) {
                        val previewView = remember { PreviewView(context) }

                        LaunchedEffect(cameraLens, videoQuality, videoAspectRatio) {
                            captureViewModel.videoManager.bindCamera(
                                lifecycleOwner = lifecycleOwner,
                                surfaceProvider = previewView.surfaceProvider,
                                lensOption = cameraLens,
                                qualityOption = videoQuality,
                                aspectOption = videoAspectRatio
                            )
                        }

                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        captureViewModel.videoManager.setFocusAndMetering(
                                            x = offset.x,
                                            y = offset.y,
                                            width = size.width.toFloat(),
                                            height = size.height.toFloat()
                                        )
                                    }
                                }
                        )
                    } else {
                        // Streaming Mode View
                        AndroidView(
                            factory = { 
                                openGlView.holder.addCallback(object : android.view.SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                                        if (streamManager.prepare(streamType)) {
                                            streamManager.startPreview()
                                        }
                                    }
                                    override fun surfaceChanged(holder: android.view.SurfaceHolder, format: Int, width: Int, height: Int) {}
                                    override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                                        streamManager.stopPreview()
                                    }
                                })
                                openGlView 
                            }, 
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Audio Only Mode - Show a placeholder
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings, // Replace with Mic if possible, using Settings just as placeholder for now since we didn't import Mic
                            contentDescription = "Audio Only",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Text("Modo Somente Áudio (Câmera Desligada)", color = Color.White, modifier = Modifier.padding(top = 100.dp))
                    }
                }

                // Chat Overlay (Local View)
                if (captureMode == CaptureMode.STREAMING && enableChatOverlay && chatMessages.isNotEmpty()) {
                    Text(
                        text = chatMessages,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }

                // HUD Overlay
                val audioLevel by captureViewModel.currentAudioLevel.collectAsState()
                
                HUDOverlay(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp), // Add padding for status bar if needed
                    batteryLevel = 100, // TODO: Implement real battery monitoring
                    isStreamHealthy = true, // TODO: Implement real stream health
                    bitrateMbps = 0.0f, // TODO: Implement real bitrate
                    sessionTime = "00:00:00", // TODO: Implement real session timer
                    audioLevel = audioLevel
                )

                // Settings Button (Floating)
                IconButton(
                    onClick = onNavigateToSettings,
                    enabled = !isActionActive,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 32.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = Color.White)
                }

                // Macros
                if (enableMacros) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (macro1Name.isNotBlank() && macro1Url.isNotBlank()) {
                            Button(onClick = { captureViewModel.triggerMacro(macro1Url) }) { Text(macro1Name) }
                        }
                        if (macro2Name.isNotBlank() && macro2Url.isNotBlank()) {
                            Button(onClick = { captureViewModel.triggerMacro(macro2Url) }) { Text(macro2Name) }
                        }
                        if (macro3Name.isNotBlank() && macro3Url.isNotBlank()) {
                            Button(onClick = { captureViewModel.triggerMacro(macro3Url) }) { Text(macro3Name) }
                        }
                        if (macro4Name.isNotBlank() && macro4Url.isNotBlank()) {
                            Button(onClick = { captureViewModel.triggerMacro(macro4Url) }) { Text(macro4Name) }
                        }
                    }
                }

                // Main Action Button
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isRecordingOrStreaming = if (captureMode == CaptureMode.LOCAL_RECORD) isRecordingLocal else isStreaming
                    val buttonShape by androidx.compose.animation.core.animateDpAsState(
                        targetValue = if (isRecordingOrStreaming) 8.dp else 50.dp,
                        label = "buttonShape"
                    )
                    
                    Button(
                        onClick = {
                            if (captureMode == CaptureMode.LOCAL_RECORD) {
                                if (isRecordingLocal) {
                                    captureViewModel.stopRecording()
                                } else {
                                    captureViewModel.startRecording(outputDirUri, audioFormat, videoCodec, localRecordType)
                                }
                            } else {
                                // Streaming Mode
                                if (isStreaming) {
                                    streamManager.stopStream()
                                    if (enableChatOverlay) chatClient.disconnect()
                                    isStreaming = false
                                } else {
                                    if (streamUrl.isNotBlank()) {
                                        if (enableChatOverlay) {
                                            streamManager.setupChatOverlay()
                                            chatClient.connect(chatServerUrl)
                                        }
                                        streamManager.startStream(streamUrl)
                                        isStreaming = true
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(buttonShape),
                        modifier = Modifier.size(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecordingOrStreaming) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        // Empty inside, the shape and color communicate the state
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Permissões necessárias.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                }) {
                    Text("Conceder")
                }
            }
        }
    }
}
