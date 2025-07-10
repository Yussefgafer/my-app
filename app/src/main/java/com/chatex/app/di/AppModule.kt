package com.chatex.app.di

import android.content.Context
import androidx.room.Room
import com.chatex.app.data.local.ChatDatabase
import com.chatex.app.data.remote.GeminiApi
import com.chatex.app.data.remote.GeminiService
import com.chatex.app.data.repository.GeminiRepository
import com.chatex.app.data.repository.MessageRepository
import com.chatex.app.util.ErrorHandler
import com.chatex.app.util.NetworkMonitor
import com.chatex.app.util.SyncManager
import com.chatex.app.worker.SyncWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideMessageRepository(
        database: ChatDatabase,
        networkMonitor: NetworkMonitor
    ): MessageRepository {
        return MessageRepository(
            database = database,
            networkMonitor = networkMonitor,
            errorHandler = ErrorHandler(context = database.applicationContext)
        )
    }
    
    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }
    
    @Provides
    @Singleton
    fun provideErrorHandler(@ApplicationContext context: Context): ErrorHandler {
        return ErrorHandler(context)
    }
    
    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        messageRepository: MessageRepository,
        networkMonitor: NetworkMonitor
    ): SyncManager {
        return SyncManager(context, messageRepository, networkMonitor)
    }
    
    // Gemini API
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideGeminiApi(okHttpClient: OkHttpClient): GeminiApi {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideGeminiService(api: GeminiApi): GeminiService {
        return GeminiService(api)
    }
    
    @Provides
    @Singleton
    fun provideGeminiRepository(
        geminiService: GeminiService,
        errorHandler: ErrorHandler
    ): GeminiRepository {
        return GeminiRepository(geminiService, errorHandler)
    }
    
    // WorkManager
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
