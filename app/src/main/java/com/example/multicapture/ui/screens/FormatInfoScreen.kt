package com.example.multicapture.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multicapture.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatInfoScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val availableCameras by settingsViewModel.availableCameras.collectAsState()
    val availableMicrophones by settingsViewModel.availableMicrophones.collectAsState()

    LaunchedEffect(Unit) {
        settingsViewModel.loadAvailableCameras()
        settingsViewModel.loadAvailableMicrophones()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guia de Formatos & Hardware") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Voltar") }
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
            // ---- CAMERAS ----
            Text("Câmeras Detectadas", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (availableCameras.isEmpty()) {
                Text("Nenhuma câmera detectada. Verifique as permissões.", style = MaterialTheme.typography.bodyMedium)
            } else {
                availableCameras.forEach { cam ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                Icons.Default.CameraAlt, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp).size(32.dp)
                            )
                            Column {
                                Text(
                                    text = cam.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text("Tipo: ${cam.lensType}", style = MaterialTheme.typography.bodySmall)
                                if (cam.focalLength > 0f) {
                                    Text("Distância Focal: ${String.format("%.1f", cam.focalLength)}mm", style = MaterialTheme.typography.bodySmall)
                                }
                                if (cam.resolutions.isNotBlank()) {
                                    Text("Resoluções: ${cam.resolutions}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---- MICROPHONES ----
            Text("Microfones Detectados", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (availableMicrophones.isEmpty()) {
                Text("Nenhum microfone detectado.", style = MaterialTheme.typography.bodyMedium)
            } else {
                availableMicrophones.forEach { mic ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mic.isExternal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (mic.isExternal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp).size(32.dp)
                            )
                            Column {
                                Text(
                                    text = if (mic.isExternal) "⚡ ${mic.name}" else mic.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text("Tipo: ${mic.type}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = if (mic.isExternal) "Dispositivo Externo" else "Dispositivo Interno",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---- FORMAT GUIDE ----
            Text("Formatos de Áudio", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            FormatItem("AAC (.m4a)", "Padrão moderno. Alta qualidade com tamanho reduzido. Excelente para uso geral.")
            FormatItem("AMR-NB (.amr)", "Otimizado para voz humana (banda estreita). Qualidade baixa, arquivo minúsculo.")
            FormatItem("AMR-WB (.awb)", "Otimizado para voz humana (banda larga). Melhor que NB, arquivo pequeno.")
            FormatItem("Opus (.webm)", "Excelente qualidade para voz e música. Ótimo para streaming.")
            FormatItem("Vorbis (.webm)", "Formato de código aberto, boa qualidade geral, similar ao MP3.")
            FormatItem("WAV (.wav)", "Sem compressão. Qualidade máxima, ideal para edição, mas gera arquivos gigantes.")

            Spacer(modifier = Modifier.height(24.dp))

            Text("Formatos de Vídeo", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            FormatItem("H.264 / AVC", "Compatibilidade universal. Toca em qualquer dispositivo, arquivo maior.")
            FormatItem("H.265 / HEVC", "Padrão moderno. Arquivos 50% menores com mesma qualidade. Pode não rodar em PCs/Celulares muito antigos.")
            FormatItem("Resoluções", "4K (Máxima nitidez, muito pesado), 1080p (Padrão ideal), 720p (Bom para redes sociais), 480p (Apenas para economia extrema).")
        }
    }
}

@Composable
fun FormatItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
    }
}
