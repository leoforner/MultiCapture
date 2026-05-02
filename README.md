# MultiCapture 🎥🎙️

Um aplicativo Android avançado desenvolvido em Kotlin + Jetpack Compose para criadores de conteúdo, streamers e desenvolvedores que precisam de flexibilidade extrema na captura de mídia.

O **MultiCapture** permite que você vá além das limitações dos gravadores nativos, separando as trilhas de áudio e vídeo, transmitindo ao vivo e criando automações diretamente na tela de gravação.

---

## ✨ Principais Funcionalidades

### 📂 1. Gravação Local Separada
Diferente das câmeras convencionais que gravam áudio e vídeo em um único arquivo (muxed), o MultiCapture permite:
- Gravar **Áudio e Vídeo separados** em arquivos independentes simultaneamente.
- Gravar **Apenas Áudio** ou **Apenas Vídeo**.
- Escolher a pasta de destino (SAF - Storage Access Framework).
- Customizar extensões de áudio (AAC, WAV, OPUS, etc.) e Codecs de Vídeo (H.264, H.265).

### 📡 2. Transmissão Ao Vivo (Stream)
Equipado com o motor da biblioteca *RootEncoder*, o app permite transmitir direto do celular:
- Suporte aos protocolos **SRT, RTMP, RTSP e UDP**.
- Transmissão independente: envie apenas áudio, apenas vídeo ou ambos.
- Simplesmente digite sua URL de ingestão (ex: `srt://ip:porta`) nas configurações e inicie a transmissão.

### 💬 3. Overlay de Chat Integrado
Mantenha contato com seu público sem precisar de uma segunda tela:
- Cliente WebSocket integrado.
- As mensagens recebidas do seu servidor de chat são "carimbadas" em tempo real no vídeo (via OpenGL TextObjectFilter).
- As mensagens aparecem tanto na sua tela quanto na stream dos espectadores.

### ⚡ 4. Botões de Macro (Webhooks)
Controle seu estúdio (OBS, Home Assistant, Servidores HTTP) direto da tela de captura:
- Configure até **4 Botões Macro** visíveis apenas para você (não aparecem na gravação/stream).
- Disparo de requisições HTTP silenciosas no background ao tocar.
- Totalmente customizáveis nas configurações (Nome e URL do Webhook).

### 🔋 5. Modo de Economia de Energia Inteligente
Projetado para longas sessões de gravação/transmissão:
- Ao iniciar a gravação/transmissão, o brilho da tela é reduzido instantaneamente para 1%.
- O brilho volta ao normal assim que a captura é encerrada.
- Garante mais autonomia térmica e de bateria.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Câmera (Local):** Android CameraX
- **Streaming & OpenGL:** [RootEncoder](https://github.com/pedroSG94/RootEncoder) (com.github.pedroSG94)
- **Armazenamento de Configurações:** Jetpack DataStore (Preferences)
- **Rede e WebSockets:** OkHttp3
- **Arquitetura:** MVVM (Model-View-ViewModel) usando Coroutines e StateFlow.

---

## 🚀 Como Compilar e Executar

1. Clone o repositório:
```bash
git clone https://github.com/SEU_USUARIO/MultiCapture.git
```
2. Abra o projeto no **Android Studio**.
3. Sincronize os pacotes do Gradle (o repositório utiliza o `JitPack` para obter a biblioteca RootEncoder).
4retorno. Conecte um dispositivo físico (API 31 ou superior) ou um emulador compatível.
5. Clique em **Run** ou compile o APK via linha de comando:
```bash
./gradlew assembleDebug
```

---

## 🔒 Permissões Necessárias

O aplicativo solicitará e exigirá as seguintes permissões para funcionar adequadamente:
- **Câmera:** Para capturar e transmitir o feed de vídeo.
- **Microfone:** Para gravar o áudio ambiente/microfone principal.
- *Permissão persistente de diretório (Storage Access Framework) concedida via interface.*

---

## 📄 Licença

Sinta-se à vontade para modificar, distribuir e usar em seus projetos. (Adicione aqui os detalhes da licença desejada, ex: MIT License).
