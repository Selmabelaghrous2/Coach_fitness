package com.example.coachfitness_belag.data.repository

import android.util.Log
import com.example.coachfitness_belag.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatHistoryItem(val role: String, val content: String)

class OllamaRepository {

    // Utilisation de 10.0.2.2 pour que l'émulateur accède au PC local
    private val baseUrl = "http://10.0.2.2:8080"

    private val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor { message -> 
                Log.d("OllamaHttp", message) 
            }.apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    suspend fun sendMessage(
            message: String,
            history: List<ChatHistoryItem>,
            threadId: String = "1",
            ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("message", message)
                put("thread_id", threadId)
            }
            
            val body = jsonBody.toString().toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                    .url("$baseUrl/api/chat")
                    .post(body)
                    .header("Connection", "keep-alive")
                    .apply {
                        TokenManager.getToken()?.let { token ->
                            val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                            header("Authorization", authHeader)
                        }
                    }
                    .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val json = try { JSONObject(responseBody) } catch (_: Exception) { null }
                    val reply = json?.optString("response") ?: json?.optString("answer") ?: responseBody
                    Result.success(cleanAIResponse(reply))
                } else {
                    Result.failure(Exception("Erreur serveur ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e("OllamaRepo", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun cleanAIResponse(text: String): String {
        return text.replace(Regex("<thought>.*?</thought>", RegexOption.DOT_MATCHES_ALL), "").trim()
    }
}
