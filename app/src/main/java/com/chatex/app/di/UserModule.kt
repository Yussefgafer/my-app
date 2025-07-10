package com.chatex.app.di

import com.chatex.app.data.local.datastore.AppPreferences
import com.chatex.app.data.manager.UserProfileManager
import com.chatex.app.data.remote.api.UserApi
import com.chatex.app.data.remote.api.SupabaseUserApi
import com.chatex.app.data.repository.UserRepository
import com.chatex.app.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Hilt module that provides user-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    
    @Provides
    @Singleton
    fun provideUserApi(
        supabaseClient: SupabaseClient
    ): UserApi = SupabaseUserApi(supabaseClient)
    
    @Provides
    @Singleton
    fun provideUserProfileManager(
        userApi: UserApi,
        appPreferences: AppPreferences,
        @ApplicationScope applicationScope: CoroutineScope
    ): UserProfileManager {
        return UserProfileManager(
            userApi = userApi,
            appPreferences = appPreferences,
            coroutineScope = applicationScope
        )
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        userApi: UserApi,
        userProfileManager: UserProfileManager,
        appPreferences: AppPreferences,
        dispatcherProvider: DispatcherProvider
    ): UserRepository {
        return UserRepository(
            userApi = userApi,
            userProfileManager = userProfileManager,
            appPreferences = appPreferences,
            ioDispatcher = dispatcherProvider.io
        )
    }
    
    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

/**
 * Custom annotation for application-wide coroutine scope.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
