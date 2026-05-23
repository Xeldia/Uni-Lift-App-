package com.example.uni_lift.features.auth.register

import com.example.uni_lift.core.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RegisterRepository : RegisterContract.Repository {

    override suspend fun register(
        fullName: String,
        studentId: String,
        email: String,
        password: String
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("full_name", fullName)
                        put("student_id", studentId)
                    }
                }
                Unit
            }
        }
}
