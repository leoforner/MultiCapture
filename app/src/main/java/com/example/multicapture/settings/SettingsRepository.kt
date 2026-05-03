package com.example.multicapture.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        val AUDIO_FORMAT_KEY = stringPreferencesKey("audio_format")
        val VIDEO_QUALITY_KEY = stringPreferencesKey("video_quality")
        val VIDEO_CODEC_KEY = stringPreferencesKey("video_codec")
        val CAMERA_LENS_KEY = stringPreferencesKey("camera_lens")
        val OUTPUT_DIR_URI_KEY = stringPreferencesKey("output_dir_uri")
        val VIDEO_ASPECT_RATIO_KEY = stringPreferencesKey("video_aspect_ratio")

        val CAPTURE_MODE_KEY = stringPreferencesKey("capture_mode")
        val LOCAL_RECORD_TYPE_KEY = stringPreferencesKey("local_record_type")
        val STREAM_TYPE_KEY = stringPreferencesKey("stream_type")
        val STREAM_URL_KEY = stringPreferencesKey("stream_url")
        
        val ENABLE_CHAT_OVERLAY_KEY = booleanPreferencesKey("enable_chat_overlay")
        val CHAT_SERVER_URL_KEY = stringPreferencesKey("chat_server_url")
        
        val ENABLE_BATTERY_SAVER_KEY = booleanPreferencesKey("enable_battery_saver")
        
        val ENABLE_MACROS_KEY = booleanPreferencesKey("enable_macros")
        val MACRO_1_NAME_KEY = stringPreferencesKey("macro_1_name")
        val MACRO_1_URL_KEY = stringPreferencesKey("macro_1_url")
        val MACRO_2_NAME_KEY = stringPreferencesKey("macro_2_name")
        val MACRO_2_URL_KEY = stringPreferencesKey("macro_2_url")
        val MACRO_3_NAME_KEY = stringPreferencesKey("macro_3_name")
        val MACRO_3_URL_KEY = stringPreferencesKey("macro_3_url")
        val MACRO_4_NAME_KEY = stringPreferencesKey("macro_4_name")
        val MACRO_4_URL_KEY = stringPreferencesKey("macro_4_url")
    }

    val audioFormatFlow: Flow<AudioFormatOption> = context.dataStore.data.map { prefs ->
        prefs[AUDIO_FORMAT_KEY]?.let { name ->
            runCatching { AudioFormatOption.valueOf(name) }.getOrDefault(AudioFormatOption.AAC)
        } ?: AudioFormatOption.AAC
    }

    val videoQualityFlow: Flow<VideoQualityOption> = context.dataStore.data.map { prefs ->
        prefs[VIDEO_QUALITY_KEY]?.let { name ->
            runCatching { VideoQualityOption.valueOf(name) }.getOrDefault(VideoQualityOption.FHD_1080P)
        } ?: VideoQualityOption.FHD_1080P
    }
    
    val videoCodecFlow: Flow<VideoCodecOption> = context.dataStore.data.map { prefs ->
        prefs[VIDEO_CODEC_KEY]?.let { name ->
            runCatching { VideoCodecOption.valueOf(name) }.getOrDefault(VideoCodecOption.H264)
        } ?: VideoCodecOption.H264
    }

    val videoAspectRatioFlow: Flow<VideoAspectRatioOption> = context.dataStore.data.map { prefs ->
        prefs[VIDEO_ASPECT_RATIO_KEY]?.let { name ->
            runCatching { VideoAspectRatioOption.valueOf(name) }.getOrDefault(VideoAspectRatioOption.RATIO_16_9)
        } ?: VideoAspectRatioOption.RATIO_16_9
    }

    val cameraLensFlow: Flow<CameraLensOption> = context.dataStore.data.map { prefs ->
        prefs[CAMERA_LENS_KEY]?.let { name ->
            runCatching { CameraLensOption.valueOf(name) }.getOrDefault(CameraLensOption.BACK)
        } ?: CameraLensOption.BACK
    }

    val outputDirUriFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[OUTPUT_DIR_URI_KEY]
    }

    val captureModeFlow: Flow<CaptureMode> = context.dataStore.data.map { prefs ->
        prefs[CAPTURE_MODE_KEY]?.let { name ->
            runCatching { CaptureMode.valueOf(name) }.getOrDefault(CaptureMode.LOCAL_RECORD)
        } ?: CaptureMode.LOCAL_RECORD
    }

    val localRecordTypeFlow: Flow<LocalRecordType> = context.dataStore.data.map { prefs ->
        prefs[LOCAL_RECORD_TYPE_KEY]?.let { name ->
            runCatching { LocalRecordType.valueOf(name) }.getOrDefault(LocalRecordType.AUDIO_AND_VIDEO)
        } ?: LocalRecordType.AUDIO_AND_VIDEO
    }

    val streamTypeFlow: Flow<StreamType> = context.dataStore.data.map { prefs ->
        prefs[STREAM_TYPE_KEY]?.let { name ->
            runCatching { StreamType.valueOf(name) }.getOrDefault(StreamType.AUDIO_AND_VIDEO)
        } ?: StreamType.AUDIO_AND_VIDEO
    }

    val streamUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[STREAM_URL_KEY] ?: ""
    }

    val enableChatOverlayFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ENABLE_CHAT_OVERLAY_KEY] ?: false
    }

    val chatServerUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CHAT_SERVER_URL_KEY] ?: ""
    }

    val enableBatterySaverFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ENABLE_BATTERY_SAVER_KEY] ?: false
    }

    val enableMacrosFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ENABLE_MACROS_KEY] ?: false
    }

    val macro1NameFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[MACRO_1_NAME_KEY] ?: "Macro 1" }
    val macro1UrlFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[MACRO_1_URL_KEY] ?: "" }
    val macro2NameFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[MACRO_2_NAME_KEY] ?: "Macro 2" }
    val macro2UrlFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[MACRO_2_URL_KEY] ?: "" }
    val macro3NameFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[MACRO_3_NAME_KEY] ?: "Macro 3" }
    val macro3UrlFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[MACRO_3_URL_KEY] ?: "" }
    val macro4NameFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[MACRO_4_NAME_KEY] ?: "Macro 4" }
    val macro4UrlFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[MACRO_4_URL_KEY] ?: "" }

    suspend fun setAudioFormat(format: AudioFormatOption) {
        context.dataStore.edit { it[AUDIO_FORMAT_KEY] = format.name }
    }

    suspend fun setVideoQuality(quality: VideoQualityOption) {
        context.dataStore.edit { it[VIDEO_QUALITY_KEY] = quality.name }
    }
    
    suspend fun setVideoCodec(codec: VideoCodecOption) {
        context.dataStore.edit { it[VIDEO_CODEC_KEY] = codec.name }
    }

    suspend fun setVideoAspectRatio(ratio: VideoAspectRatioOption) {
        context.dataStore.edit { it[VIDEO_ASPECT_RATIO_KEY] = ratio.name }
    }

    suspend fun setCameraLens(lens: CameraLensOption) {
        context.dataStore.edit { it[CAMERA_LENS_KEY] = lens.name }
    }

    suspend fun setOutputDirUri(uri: String) {
        context.dataStore.edit { it[OUTPUT_DIR_URI_KEY] = uri }
    }

    suspend fun setCaptureMode(mode: CaptureMode) { context.dataStore.edit { it[CAPTURE_MODE_KEY] = mode.name } }
    suspend fun setLocalRecordType(type: LocalRecordType) { context.dataStore.edit { it[LOCAL_RECORD_TYPE_KEY] = type.name } }
    suspend fun setStreamType(type: StreamType) { context.dataStore.edit { it[STREAM_TYPE_KEY] = type.name } }
    suspend fun setStreamUrl(url: String) { context.dataStore.edit { it[STREAM_URL_KEY] = url } }
    suspend fun setEnableChatOverlay(enable: Boolean) { context.dataStore.edit { it[ENABLE_CHAT_OVERLAY_KEY] = enable } }
    suspend fun setChatServerUrl(url: String) { context.dataStore.edit { it[CHAT_SERVER_URL_KEY] = url } }
    suspend fun setEnableBatterySaver(enable: Boolean) { context.dataStore.edit { it[ENABLE_BATTERY_SAVER_KEY] = enable } }
    suspend fun setEnableMacros(enable: Boolean) { context.dataStore.edit { it[ENABLE_MACROS_KEY] = enable } }
    suspend fun setMacro1Name(name: String) { context.dataStore.edit { it[MACRO_1_NAME_KEY] = name } }
    suspend fun setMacro1Url(url: String) { context.dataStore.edit { it[MACRO_1_URL_KEY] = url } }
    suspend fun setMacro2Name(name: String) { context.dataStore.edit { it[MACRO_2_NAME_KEY] = name } }
    suspend fun setMacro2Url(url: String) { context.dataStore.edit { it[MACRO_2_URL_KEY] = url } }
    suspend fun setMacro3Name(name: String) { context.dataStore.edit { it[MACRO_3_NAME_KEY] = name } }
    suspend fun setMacro3Url(url: String) { context.dataStore.edit { it[MACRO_3_URL_KEY] = url } }
    suspend fun setMacro4Name(name: String) { context.dataStore.edit { it[MACRO_4_NAME_KEY] = name } }
    suspend fun setMacro4Url(url: String) { context.dataStore.edit { it[MACRO_4_URL_KEY] = url } }
}
