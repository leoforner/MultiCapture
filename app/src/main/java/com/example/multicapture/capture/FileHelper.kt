package com.example.multicapture.capture

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.*

object FileHelper {
    fun createAudioFile(context: Context, customDirUri: String?, extension: String, mimeType: String): Uri? {
        val fileName = "Audio_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}$extension"
        
        if (!customDirUri.isNullOrEmpty()) {
            try {
                val treeUri = Uri.parse(customDirUri)
                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
                return DocumentsContract.createDocument(context.contentResolver, docUri, mimeType, fileName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Default to MediaStore
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/MultiCapture")
            }
        }
        return context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun createVideoFile(context: Context, customDirUri: String?, extension: String, mimeType: String): Uri? {
        val fileName = "Video_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}$extension"
        
        if (!customDirUri.isNullOrEmpty()) {
            try {
                val treeUri = Uri.parse(customDirUri)
                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
                return DocumentsContract.createDocument(context.contentResolver, docUri, mimeType, fileName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Default to MediaStore
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MultiCapture")
            }
        }
        return context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    }
}
