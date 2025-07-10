package com.chatex.app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    private const val SUPABASE_URL = "https://wtjyqquqjwrnatrrbquw.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind0anlxcXVxandybmF0cnJicXV3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTIwMDczMjYsImV4cCI6MjA2NzU4MzMyNn0.t3yKBaNIgN-rkB-cJMxzbAo7g_oIuSIPkd7doCaQbWI"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(GoTrue)
            install(Postgrest)
            install(Realtime)
        }
    }
}
