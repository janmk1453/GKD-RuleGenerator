// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val TAG = "AiApiClient"

data class StreamChunk(
    val thinking: String = "",
    val content: String = "",
    val isDone: Boolean = false
)

class AiApiClient {

    private val client = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            socketTimeoutMillis = 120_000
        }
    }
    private val json = Json { ignoreUnknownKeys = true }

    fun chatCompletionStream(
        apiUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userMessage: String
    ): Flow<StreamChunk> = flow {
        val fullUrl = buildApiUrl(apiUrl)
        Log.d(TAG, "Streaming API: $fullUrl, model: $model")

        val requestBody = buildString {
            append("{")
            append("\"model\":\"$model\",")
            append("\"stream\":true,")
            append("\"messages\":[")
            append("{\"role\":\"system\",\"content\":${jsonEscape(systemPrompt)}},")
            append("{\"role\":\"user\",\"content\":${jsonEscape(userMessage)}}")
            append("],")
            append("\"temperature\":0.7,")
            append("\"max_tokens\":4096")
            append("}")
        }

        val response: HttpResponse = client.post(fullUrl) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(requestBody)
        }

        val status = response.status.value
        if (status !in 200..299) {
            val body = response.bodyAsChannel().readUTF8Line() ?: ""
            throw Exception("HTTP $status: ${body.take(500)}")
        }

        val channel = response.bodyAsChannel()
        val contentBuffer = StringBuilder()
        val thinkingBuffer = StringBuilder()

        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: continue
            if (!line.startsWith("data: ")) continue

            val data = line.removePrefix("data: ").trim()
            if (data == "[DONE]") {
                emit(StreamChunk(
                    thinking = thinkingBuffer.toString(),
                    content = contentBuffer.toString(),
                    isDone = true
                ))
                break
            }

            try {
                val parsed = parseStreamChunk(data) ?: continue
                if (parsed.thinking.isNotEmpty()) {
                    thinkingBuffer.append(parsed.thinking)
                }
                if (parsed.content.isNotEmpty()) {
                    contentBuffer.append(parsed.content)
                }
                emit(StreamChunk(
                    thinking = thinkingBuffer.toString(),
                    content = contentBuffer.toString()
                ))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse chunk: $data", e)
            }
        }
    }

    private fun parseStreamChunk(data: String): StreamChunk? {
        return try {
            val jsonObj = json.parseToJsonElement(data).jsonObject
            val choices = jsonObj["choices"]?.jsonArray ?: return null
            if (choices.isEmpty()) return null

            val delta = choices[0].jsonObject["delta"]?.jsonObject ?: return null

            val contentEl = delta["content"]
            val content = if (contentEl != null && contentEl != JsonNull) {
                contentEl.jsonPrimitive.content
            } else ""

            val reasoningEl = delta["reasoning_content"] ?: delta["reasoning"]
            val reasoning = if (reasoningEl != null && reasoningEl != JsonNull) {
                reasoningEl.jsonPrimitive.content
            } else ""

            if (content.isEmpty() && reasoning.isEmpty()) return null
            StreamChunk(thinking = reasoning, content = content)
        } catch (e: Exception) {
            null
        }
    }

    private fun buildApiUrl(baseUrl: String): String {
        val trimmed = baseUrl.trimEnd('/')
        return when {
            trimmed.endsWith("/chat/completions") -> trimmed
            trimmed.endsWith("/v1") -> "$trimmed/chat/completions"
            else -> "$trimmed/v1/chat/completions"
        }
    }

    private fun jsonEscape(text: String): String {
        return "\"" + text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""
    }

    fun close() {
        client.close()
    }
}
