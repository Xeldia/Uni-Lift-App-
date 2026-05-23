package com.example.uni_lift.core.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseProvider {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://xglaagdzaiabwbptnref.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhnbGFhZ2R6YWlhYndicHRucmVmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI4MTk3MDQsImV4cCI6MjA4ODM5NTcwNH0.D_GuHiG_heRcOqBehhLbS9kf-X64BzsHfl6QkkegBCU"
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}
