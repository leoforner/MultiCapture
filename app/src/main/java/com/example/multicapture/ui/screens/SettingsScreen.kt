package com.example.multicapture.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multicapture.settings.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFormatInfo: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val captureMode by viewModel.captureMode.collectAsState()
    val localRecordType by viewModel.localRecordType.collectAsState()
    val streamType by viewModel.streamType.collectAsState()
    val streamUrl by viewModel.streamUrl.collectAsState()
    
    val enableChatOverlay by viewModel.enableChatOverlay.collectAsState()
    val chatServerUrl by viewModel.chatServerUrl.collectAsState()
    
    val enableBatterySaver by viewModel.enableBatterySaver.collectAsState()
    val enableMacros by viewModel.enableMacros.collectAsState()
    
    val macro1Name by viewModel.macro1Name.collectAsState()
    val macro1Url by viewModel.macro1Url.collectAsState()
    val macro2Name by viewModel.macro2Name.collectAsState()
    val macro2Url by viewModel.macro2Url.collectAsState()
    val macro3Name by viewModel.macro3Name.collectAsState()
    val macro3Url by viewModel.macro3Url.collectAsState()
    val macro4Name by viewModel.macro4Name.collectAsState()
    val macro4Url by viewModel.macro4Url.collectAsState()
    
    val audioFormat by viewModel.audioFormat.collectAsState()
    val videoQuality by viewModel.videoQuality.collectAsState()
    val videoCodec by viewModel.videoCodec.collectAsState()
    val videoAspectRatio by viewModel.videoAspectRatio.collectAsState()
    val outputDirUri by viewModel.outputDirUri.collectAsState()

    // Camera & microphone hardware
    val availableCameras by viewModel.availableCameras.collectAsState()
    val selectedCameraId by viewModel.selectedCameraId.collectAsState()
    val availableMicrophones by viewModel.availableMicrophones.collectAsState()
    val selectedMicrophoneId by viewModel.selectedMicrophoneId.collectAsState()

    var streamMode by remember { mutableStateOf(StreamMode.UNIFIED) }
    var dualCameraMode by remember { mutableStateOf(DualCameraMode.SINGLE) }
    var pipPosition by remember { mutableStateOf(PiPPosition.TOP_RIGHT) }
    var streamUrlSecond by remember { mutableStateOf("") }
    var streamAudioUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAvailableCameras()
        viewModel.loadAvailableMicrophones()
    }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        uri?.let {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            viewModel.setOutputDirUri(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações Avançadas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Voltar") }
                },
                actions = {
                    IconButton(onClick = onNavigateToFormatInfo) { Icon(Icons.Default.Info, "Guia & Hardware") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            SectionTitle("Modo de Operação")
            EnumDropdown("Modo Principal", CaptureMode.entries, captureMode) { viewModel.setCaptureMode(it) }
            
            Divider(Modifier.padding(vertical = 8.dp))
            
            if (captureMode == CaptureMode.LOCAL_RECORD) {
                SectionTitle("Aba Armazenamento (Local)")
                EnumDropdown("Tipo de Gravação", LocalRecordType.entries, localRecordType) { viewModel.setLocalRecordType(it) }
                
                ListItem(
                    headlineContent = { Text("Pasta de Destino (Storage Access)") },
                    supportingContent = { 
                        Text(outputDirUri?.let { Uri.parse(it).lastPathSegment } ?: "Galeria Pública (Padrão)")
                    },
                    leadingContent = { Icon(Icons.Default.Folder, null) },
                    modifier = Modifier.clickable { folderLauncher.launch(null) }
                )
                
                Divider(Modifier.padding(vertical = 8.dp))
                SectionTitle("Formatos (Local)")
                EnumDropdown("Formato de Áudio", AudioFormatOption.entries, audioFormat) { viewModel.setAudioFormat(it) }
                EnumDropdown("Codec do Vídeo", VideoCodecOption.entries, videoCodec) { viewModel.setVideoCodec(it) }
            } else {
                SectionTitle("Aba Streaming (Multi-Stream)")
                EnumDropdown("Modo de Envio", StreamMode.entries, streamMode) { streamMode = it }
                EnumDropdown("Tipo de Transmissão", StreamType.entries, streamType) { viewModel.setStreamType(it) }
                
                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { viewModel.setStreamUrl(it) },
                    label = { Text("URL Principal (Vídeo ou Unificado)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                if (streamMode == StreamMode.SEPARATED_AUDIO || streamMode == StreamMode.MULTI_CAMERA) {
                    OutlinedTextField(
                        value = streamAudioUrl,
                        onValueChange = { streamAudioUrl = it },
                        label = { Text("URL de Áudio Separado") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }

                if (streamMode == StreamMode.MULTI_CAMERA) {
                    OutlinedTextField(
                        value = streamUrlSecond,
                        onValueChange = { streamUrlSecond = it },
                        label = { Text("URL Segunda Câmera (Vídeo)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("Habilitar Overlay de Chat", modifier = Modifier.weight(1f))
                    Switch(checked = enableChatOverlay, onCheckedChange = { viewModel.setEnableChatOverlay(it) })
                }
                
                if (enableChatOverlay) {
                    OutlinedTextField(
                        value = chatServerUrl,
                        onValueChange = { viewModel.setChatServerUrl(it) },
                        label = { Text("URL do WebSocket do Servidor de Chat") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }

            Divider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Aba Câmeras")
            EnumDropdown("Modo da Lente", DualCameraMode.entries, dualCameraMode) { dualCameraMode = it }
            
            if (dualCameraMode == DualCameraMode.PIP) {
                EnumDropdown("Posição PiP", PiPPosition.entries, pipPosition) { pipPosition = it }
            }

            EnumDropdown("Qualidade", VideoQualityOption.entries, videoQuality) { viewModel.setVideoQuality(it) }
            EnumDropdown("Proporção de Vídeo", VideoAspectRatioOption.entries, videoAspectRatio) { viewModel.setVideoAspectRatio(it) }

            // Camera selection dropdown — lists all detected physical cameras
            var cameraExpanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = cameraExpanded,
                onExpandedChange = { cameraExpanded = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                val currentCamera = availableCameras.find { it.id == selectedCameraId }
                val display = currentCamera?.name ?: "Padrão do Sistema (Traseira)"
                
                OutlinedTextField(
                    value = display,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Câmera Ativa") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cameraExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = cameraExpanded,
                    onDismissRequest = { cameraExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Padrão do Sistema") },
                        onClick = {
                            viewModel.setSelectedCameraId(null)
                            viewModel.setCameraLens(CameraLensOption.BACK)
                            cameraExpanded = false
                        }
                    )
                    availableCameras.forEach { cam ->
                        DropdownMenuItem(
                            text = { Text(cam.name) },
                            onClick = {
                                viewModel.setSelectedCameraId(cam.id)
                                // Auto-detect lens facing
                                val lens = if (cam.lensFacing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT)
                                    CameraLensOption.FRONT else CameraLensOption.BACK
                                viewModel.setCameraLens(lens)
                                cameraExpanded = false
                            }
                        )
                    }
                }
            }

            Divider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Microfone")
            
            // Microphone selection dropdown
            var micExpanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = micExpanded,
                onExpandedChange = { micExpanded = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                val currentMic = availableMicrophones.find { it.id == selectedMicrophoneId }
                val micDisplay = currentMic?.let { "${it.name} (${it.type})" } ?: "Padrão do Sistema"
                
                OutlinedTextField(
                    value = micDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Microfone Ativo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = micExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = micExpanded,
                    onDismissRequest = { micExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Padrão do Sistema") },
                        onClick = {
                            viewModel.setSelectedMicrophoneId(null)
                            micExpanded = false
                        }
                    )
                    availableMicrophones.forEach { mic ->
                        val label = if (mic.isExternal) "⚡ ${mic.name} (${mic.type})" else "${mic.name} (${mic.type})"
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.setSelectedMicrophoneId(mic.id)
                                micExpanded = false
                            }
                        )
                    }
                }
            }

            Divider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Recursos Extras")
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Economia de Energia (Reduzir Brilho)", modifier = Modifier.weight(1f))
                Switch(checked = enableBatterySaver, onCheckedChange = { viewModel.setEnableBatterySaver(it) })
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Habilitar Botões Macro (Visíveis na captura)", modifier = Modifier.weight(1f))
                Switch(checked = enableMacros, onCheckedChange = { viewModel.setEnableMacros(it) })
            }
            
            if (enableMacros) {
                Text("Macro 1", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                Row {
                    OutlinedTextField(value = macro1Name, onValueChange = { viewModel.setMacro1Name(it) }, label = { Text("Nome") }, modifier = Modifier.weight(1f).padding(end = 4.dp))
                    OutlinedTextField(value = macro1Url, onValueChange = { viewModel.setMacro1Url(it) }, label = { Text("Webhook URL") }, modifier = Modifier.weight(2f))
                }
                
                Text("Macro 2", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                Row {
                    OutlinedTextField(value = macro2Name, onValueChange = { viewModel.setMacro2Name(it) }, label = { Text("Nome") }, modifier = Modifier.weight(1f).padding(end = 4.dp))
                    OutlinedTextField(value = macro2Url, onValueChange = { viewModel.setMacro2Url(it) }, label = { Text("Webhook URL") }, modifier = Modifier.weight(2f))
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> EnumDropdown(
    label: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val displayValue = when(selectedOption) {
            is AudioFormatOption -> (selectedOption as AudioFormatOption).displayName
            is VideoQualityOption -> (selectedOption as VideoQualityOption).displayName
            is VideoCodecOption -> (selectedOption as VideoCodecOption).displayName
            is VideoAspectRatioOption -> (selectedOption as VideoAspectRatioOption).displayName
            is CameraLensOption -> (selectedOption as CameraLensOption).displayName
            is CaptureMode -> (selectedOption as CaptureMode).displayName
            is LocalRecordType -> (selectedOption as LocalRecordType).displayName
            is StreamType -> (selectedOption as StreamType).displayName
            is StreamMode -> (selectedOption as StreamMode).displayName
            is DualCameraMode -> (selectedOption as DualCameraMode).displayName
            is PiPPosition -> (selectedOption as PiPPosition).displayName
            else -> selectedOption.name
        }
        
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                val text = when(option) {
                    is AudioFormatOption -> option.displayName
                    is VideoQualityOption -> option.displayName
                    is VideoCodecOption -> option.displayName
                    is VideoAspectRatioOption -> option.displayName
                    is CameraLensOption -> option.displayName
                    is CaptureMode -> option.displayName
                    is LocalRecordType -> option.displayName
                    is StreamType -> option.displayName
                    is StreamMode -> option.displayName
                    is DualCameraMode -> option.displayName
                    is PiPPosition -> option.displayName
                    else -> option.name
                }
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
