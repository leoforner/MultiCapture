package com.example.multicapture.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatInfoScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guia de Formatos") },
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
            Text("Áudio", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            FormatItem("AAC (.m4a)", "Padrão moderno. Alta qualidade com tamanho reduzido. Excelente para uso geral.")
            FormatItem("AMR-NB (.amr)", "Otimizado para voz humana (banda estreita). Qualidade baixa, arquivo minúsculo.")
            FormatItem("AMR-WB (.awb)", "Otimizado para voz humana (banda larga). Melhor que NB, arquivo pequeno.")
            FormatItem("Opus (.webm)", "Excelente qualidade para voz e música. Ótimo para streaming.")
            FormatItem("Vorbis (.webm)", "Formato de código aberto, boa qualidade geral, similar ao MP3.")
            FormatItem("WAV (.wav)", "Sem compressão. Qualidade máxima, ideal para edição, mas gera arquivos gigantes.")

            Spacer(modifier = Modifier.height(24.dp))

            Text("Vídeo", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
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
