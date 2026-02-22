package com.ollamamobile.app.data

import org.json.JSONObject

/** GET /api/tags response */
data class TagsResponse(val models: List<OllamaModel>)

data class OllamaModel(
    val name: String,
    val size: Long = 0L,
    val modifiedAt: String? = null,
    val details: ModelDetails? = null
) {
    companion object {
        fun fromJson(obj: JSONObject): OllamaModel = OllamaModel(
            name = obj.optString("name", obj.optString("model", "")),
            size = obj.optLong("size", 0L),
            modifiedAt = obj.optString("modified_at").takeIf { it.isNotEmpty() },
            details = obj.optJSONObject("details")?.let { ModelDetails.fromJson(it) }
        )
    }
}

data class ModelDetails(
    val parameterSize: String? = null,
    val family: String? = null,
    val format: String? = null
) {
    companion object {
        fun fromJson(obj: JSONObject): ModelDetails = ModelDetails(
            parameterSize = obj.optString("parameter_size").takeIf { it.isNotEmpty() },
            family = obj.optString("family").takeIf { it.isNotEmpty() },
            format = obj.optString("format").takeIf { it.isNotEmpty() }
        )
    }
}

/** Chat message for UI and API */
data class ChatMessage(
    val role: String, // "user" | "assistant" | "system"
    val content: String,
    val isStreaming: Boolean = false
)

/** POST /api/chat request body */
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessagePayload>,
    val stream: Boolean = true
)

data class ChatMessagePayload(val role: String, val content: String)

/** Streamed chunk from POST /api/chat (NDJSON) */
data class ChatChunk(
    val message: MessageChunk?,
    val done: Boolean,
    val doneReason: String? = null
) {
    data class MessageChunk(val role: String?, val content: String?)
}
