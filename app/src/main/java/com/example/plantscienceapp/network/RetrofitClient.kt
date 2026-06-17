package com.example.plantscienceapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BAIDU_BASE_URL = "https://aip.baidubce.com/"
    private const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baiduRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BAIDU_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val deepSeekRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(DEEPSEEK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: PlantApiService by lazy {
        baiduRetrofit.create(PlantApiService::class.java)
    }

    val deepSeekService: DeepSeekApiService by lazy {
        deepSeekRetrofit.create(DeepSeekApiService::class.java)
    }
}
