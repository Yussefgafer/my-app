package com.chatex.app.data.remote

import com.chatex.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-pro:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
    suspend fun generateContent(@Body request: GeminiRequest): GeminiResponse
}

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

data class Content(
    val parts: List<Part>,
    val role: String = "user"
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Double = 0.7,
    val topK: Int = 1,
    val topP: Double = 0.8,
    val maxOutputTokens: Int = 2048
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content,
    val finishReason: String,
    val index: Int,
    val safetyRatings: List<SafetyRating>
)

data class SafetyRating(
    val category: String,
    val probability: String
)

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)
}
