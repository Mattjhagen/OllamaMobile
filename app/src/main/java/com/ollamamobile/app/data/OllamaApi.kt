package com.ollamamobile.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OllamaApi(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun listModels(): Result<List<OllamaModel>> = withContext(Dispatchers.IO) {
        runCatching {
            val req = Request.Builder()
                .url("$baseUrl/api/tags")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}: ${resp.message}")
            val body = resp.body?.string() ?: throw Exception("Empty response")
            val json = JSONObject(body)
            val arr = json.optJSONArray("models") ?: JSONArray()
            List(arr.length()) { i -> OllamaModel.fromJson(arr.getJSONObject(i)) }
        }
    }

    /**
     * Stream chat completion. Emits content deltas; final chunk has done=true.
     */
    fun chatStream(model: String, messages: List<ChatMessagePayload>): Flow<Result<String>> = flow {
        val body = JSONObject().apply {
            put("model", model)
            put("stream", true)
            put("messages", JSONArray(messages.map { msg ->
                JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                }
            }))
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl/api/chat")
            .post(body.toRequestBody(jsonType))
            .build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    emit(Result.failure(Exception("HTTP ${response.code}: ${response.message}")))
                    return@withContext
                }
                val source = response.body?.source() ?: return@withContext
                while (true) {
                    val line = source.readUtf8Line() ?: break
                    if (line.isEmpty()) continue
                    try {
                        val obj = JSONObject(line)
                        val done = obj.optBoolean("done", false)
                        val msg = obj.optJSONObject("message")
                        val content = msg?.optString("content") ?: ""
                        if (content.isNotEmpty()) {
                            emit(Result.success(content))
                        }
                        if (done) break
                    } catch (_: Exception) { /* skip malformed line */ }
                }
            }
        }
    }

    /** Ping to check if Ollama is reachable */
    suspend fun ping(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val req = Request.Builder().url("$baseUrl/api/tags").get().build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) throw Exception("Ollama not reachable: ${resp.code}")
        }
    }
}
