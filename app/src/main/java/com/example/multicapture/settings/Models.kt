package com.example.multicapture.settings

enum class AudioFormatOption(val displayName: String, val extension: String, val mimeType: String) {
    AAC("AAC (.m4a)", ".m4a", "audio/mp4"),
    AMR_NB("AMR-NB (.amr)", ".amr", "audio/amr"),
    AMR_WB("AMR-WB (.awb)", ".awb", "audio/amr-wb"),
    OPUS("Opus (.webm)", ".webm", "audio/ogg"),
    VORBIS("Vorbis (.webm)", ".webm", "audio/webm"),
    WAV("WAV (PCM Não Comprimido)", ".wav", "audio/wav")
}

enum class VideoQualityOption(val displayName: String) {
    UHD_4K("4K (UHD)"),
    FHD_1080P("1080p (FHD)"),
    HD_720P("720p (HD)"),
    SD_480P("480p (SD)")
}

enum class VideoAspectRatioOption(val displayName: String) {
    RATIO_16_9("16:9 (Widescreen)"),
    RATIO_4_3("4:3 (Padrão)"),
    RATIO_1_1("1:1 (Quadrado)")
}

enum class VideoCodecOption(val displayName: String) {
    H264("H.264 / AVC"),
    H265("H.265 / HEVC")
}

enum class CameraLensOption(val displayName: String) {
    FRONT("Frontal"),
    BACK("Traseira")
}

enum class CaptureMode(val displayName: String) {
    LOCAL_RECORD("Gravação Local"),
    STREAMING("Transmissão (Stream)")
}

enum class LocalRecordType(val displayName: String) {
    AUDIO_AND_VIDEO("Áudio e Vídeo"),
    AUDIO_ONLY("Apenas Áudio"),
    VIDEO_ONLY("Apenas Vídeo")
}

enum class StreamType(val displayName: String) {
    AUDIO_AND_VIDEO("Áudio e Vídeo"),
    AUDIO_ONLY("Apenas Áudio"),
    VIDEO_ONLY("Apenas Vídeo")
}

enum class StreamMode(val displayName: String) {
    UNIFIED("Unificado (1 URL)"),
    SEPARATED_AUDIO("Áudio Separado (2 URLs)"),
    MULTI_CAMERA("Multi-Câmera Separada (2 URLs Vídeo + Áudio)")
}

enum class DualCameraMode(val displayName: String) {
    SINGLE("Lente Única"),
    PIP("Dual Camera (PiP)")
}

enum class PiPPosition(val displayName: String) {
    TOP_RIGHT("Canto Superior Direito"),
    TOP_LEFT("Canto Superior Esquerdo"),
    BOTTOM_RIGHT("Canto Inferior Direito"),
    BOTTOM_LEFT("Canto Inferior Esquerdo")
}

data class CameraHardwareInfo(
    val id: String,
    val name: String,
    val lensFacing: Int,
    val lensType: String = "Principal",
    val focalLength: Float = 0f,
    val resolutions: String = ""
)

data class MicrophoneInfo(
    val id: Int,
    val name: String,
    val type: String,
    val isExternal: Boolean
)
