package com.chatex.app.data.repository

import com.chatex.app.data.remote.GeminiApi
import com.chatex.app.data.remote.GeminiRequest
import com.chatex.app.data.remote.GeminiResponse
import com.chatex.app.data.remote.Part
import com.chatex.app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    private val geminiApi: GeminiApi,
    private val errorHandler: ErrorHandler
) {
    suspend fun generateResponse(prompt: String): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)
            
            val request = GeminiRequest(
                contents = listOf(
                    GeminiRequest.Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            
            val response = geminiApi.generateContent(request)
            val generatedText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No response generated")
                
            emit(Result.Success(generatedText))
        } catch (e: Exception) {
            emit(Result.Error(errorHandler.getErrorMessage(e)))
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun generateChatResponse(conversation: List<Pair<String, String>>): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)
            
            val contents = conversation.map { (role, content) ->
                GeminiRequest.Content(
                    parts = listOf(Part(text = content)),
                    role = if (role == "user") "user" else "model"
                )
            }
            
            val request = GeminiRequest(
                contents = contents
            )
            
            val response = geminiApi.generateContent(request)
            val generatedText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No response generated")
                
            emit(Result.Success(generatedText))
        } catch (e: Exception) {
            emit(Result.Error(errorHandler.getErrorMessage(e)))
        }
    }.flowOn(Dispatchers.IO)
}
