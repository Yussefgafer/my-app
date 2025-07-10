package com.chatex.app.di

import android.content.Context
import com.chatex.app.data.remote.supabase.SupabaseRepository
import com.chatex.app.util.NetworkMonitor
import com.chatex.app.util.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "YOUR_SUPABASE_URL",
            supabaseKey = "YOUR_SUPABASE_ANON_KEY"
        ) {
            install(GoTrue)
            install(Postgrest)
            install(Realtime)
        }
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideNetworkUtils(): NetworkUtils = NetworkUtils

    @Provides
    @Singleton
    fun provideSupabaseRepository(
        supabaseClient: SupabaseClient
    ): SupabaseRepository = SupabaseRepository(supabaseClient)
}
