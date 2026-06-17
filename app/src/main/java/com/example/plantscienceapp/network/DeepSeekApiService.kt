package com.example.plantscienceapp.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<DeepSeekMessage>,
    val response_format: DeepSeekResponseFormat? = null
)

data class DeepSeekMessage(
    val role: String,
    val content: String
)

data class DeepSeekResponseFormat(
    val type: String
)

data class DeepSeekResponse(
    val choices: List<DeepSeekChoice>
)

data class DeepSeekChoice(
    val message: DeepSeekMessage
)

interface DeepSeekApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletions(
        @Header("Authorization") auth: String,
        @Body request: DeepSeekRequest
    ): DeepSeekResponse
}
