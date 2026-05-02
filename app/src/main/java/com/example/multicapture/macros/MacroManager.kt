package com.example.multicapture.macros

import okhttp3.*
import java.io.IOException

object MacroManager {
    private val client = OkHttpClient()

    fun triggerMacro(url: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (url.isBlank()) {
            onError("URL vazia")
            return
        }
        
        val request = try {
            Request.Builder().url(url).build()
        } catch (e: Exception) {
            onError("URL inválida")
            return
        }
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e.message ?: "Erro desconhecido")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Erro HTTP: ${response.code}")
                }
                response.close()
            }
        })
    }
}
