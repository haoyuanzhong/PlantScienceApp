package com.example.plantscienceapp.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// --- 百度 AI Token 响应模型 ---
data class TokenResponse(
    val access_token: String,
    val expires_in: Long
)

// --- AI 识别相关数据模型 ---
data class PlantRecognitionResponse(
    val log_id: Long,
    val result: List<RecognitionResult>
)

data class RecognitionResult(
    val name: String,
    val score: Double,
    val baike_info: BaikeInfo?
)

data class BaikeInfo(
    val description: String?,
    val image_url: String?,
    val baike_url: String?
)

interface PlantApiService {

    // 获取百度 AI Access Token
    @GET("oauth/2.0/token")
    suspend fun getAccessToken(
        @Query("grant_type") grantType: String = "client_credentials",
        @Query("client_id") apiKey: String,
        @Query("client_secret") secretKey: String
    ): TokenResponse

    // 调用植物识别接口
    @FormUrlEncoded
    @POST("rest/2.0/image-classify/v1/plant")
    suspend fun identifyPlant(
        @Query("access_token") accessToken: String,
        @Field("image") base64Image: String,
        @Field("baike_num") baikeNum: Int = 1
    ): PlantRecognitionResponse
}
