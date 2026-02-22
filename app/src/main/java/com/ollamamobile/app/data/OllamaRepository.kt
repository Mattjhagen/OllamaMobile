package com.ollamamobile.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OllamaRepository(
    context: Context,
    private var baseUrl: String = DEFAULT_BASE_URL,
) {
    private val prefs: OllamaPreferences = OllamaPreferences(context)
    init {
        baseUrl = prefs.getBaseUrl() ?: baseUrl
    }

    fun getApi(): OllamaApi = OllamaApi(baseUrl)

    fun setBaseUrl(url: String) {
        baseUrl = url.trim().removeSuffix("/")
        prefs.setBaseUrl(baseUrl)
    }

    fun getBaseUrl(): String = baseUrl

    suspend fun listModels(): Result<List<OllamaModel>> = getApi().listModels()
    fun chatStream(model: String, messages: List<ChatMessagePayload>): Flow<Result<String>> =
        getApi().chatStream(model, messages)
    suspend fun ping(): Result<Unit> = getApi().ping()

    companion object {
        const val DEFAULT_BASE_URL = "http://127.0.0.1:11434"
    }
}
