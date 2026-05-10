package com.example.coachfitness_belag.data.repository

import android.util.Log
import com.example.coachfitness_belag.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

data class ChatHistoryItem(val role: String, val content: String)

class OllamaRepository {

    private val baseUrl = "http://192.168.11.101:8080"

    private val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor { message -> 
                Log.d("OllamaHttp", message) 
            }.apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(600, TimeUnit.SECONDS) 
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

    suspend fun sendMessage(
            message: String,
            history: List<ChatHistoryItem>,
            threadId: String = "1",
            ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "$baseUrl/api/chat".toHttpUrlOrNull()?.newBuilder()
            if (urlBuilder == null) return@withContext Result.failure(Exception("URL invalide"))

            urlBuilder.addQueryParameter("message", message)
            urlBuilder.addQueryParameter("thread_id", threadId)
            
            val url = urlBuilder.build()
            
            val request = Request.Builder()
                    .url(url)
                    .get()
                    .apply {
                        TokenManager.getToken()?.let { token ->
                            val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                            header("Authorization", authHeader)
                        }
                    }
                    .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("OllamaRepo", "Status: ${response.code} | Body: $responseBody")

                if (response.isSuccessful) {
                    val json = try { JSONObject(responseBody) } catch (_: Exception) { null }
                    
                    var reply = when {
                        json != null && !json.optString("response").isNullOrBlank() -> json.getString("response")
                        json != null && !json.optString("answer").isNullOrBlank() -> json.getString("answer")
                        else -> responseBody
                    }

                    // Nettoyer la réponse pour enlever les scripts de réflexion/analyse
                    reply = cleanAIResponse(reply)

                    if (reply.isNotBlank()) {
                        Result.success(reply)
                    } else {
                        Result.failure(Exception("Le modèle n'a rien généré."))
                    }
                } else {
                    Result.failure(Exception("Erreur serveur ${response.code}: $responseBody"))
                }
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Le serveur est trop lent à répondre (Timeout)."))
        } catch (e: Exception) {
            Log.e("OllamaRepo", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Nettoie la réponse de l'IA pour ne garder que le message final utile.
     */
    private fun cleanAIResponse(text: String): String {
        var cleaned = text
        
        // 1. Supprimer les balises de réflexion type <thought>...</thought> ou <analysis>
        cleaned = cleaned.replace(Regex("<thought>.*?</thought>", RegexOption.DOT_MATCHES_ALL), "")
        cleaned = cleaned.replace(Regex("<analysis>.*?</analysis>", RegexOption.DOT_MATCHES_ALL), "")
        
        // 2. Supprimer les préfixes courants si l'IA les ajoute
        val markers = listOf("Response:", "Answer:", "Assistant:", "AI:", "Résultat:", "Voici la réponse :")
        for (marker in markers) {
            if (cleaned.contains(marker, ignoreCase = true)) {
                cleaned = cleaned.substringAfterLast(marker).trim()
            }
        }

        // 3. Si l'IA répète la structure de la discussion
        if (cleaned.contains("\nUser:")) {
            cleaned = cleaned.substringBefore("\nUser:")
        }

        return cleaned.trim()
    }
}
