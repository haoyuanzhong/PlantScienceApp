package com.example.plantscienceapp.network

import com.example.plantscienceapp.data.entity.Plant
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class FeedbackRequest(
    val userId: String,
    val content: String,
    val timestamp: Long
)

data class FeedbackResponse(
    val success: Boolean,
    val message: String
)

interface PlantApiService {
    
    @GET("plants_data.json")
    suspend fun getInitialPlants(): List<Plant>

    @POST("feedback")
    suspend fun submitFeedback(@Body feedback: FeedbackRequest): FeedbackResponse
}
