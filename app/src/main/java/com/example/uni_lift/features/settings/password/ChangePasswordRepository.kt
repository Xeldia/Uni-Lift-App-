package com.example.uni_lift.features.settings.password

import com.example.uni_lift.core.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChangePasswordRepository : ChangePasswordContract.Repository {

    override suspend fun changePassword(
        token: String,
        oldPassword: String,
        newPassword: String
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val client = SupabaseProvider.client
                val currentEmail = client.auth.currentSessionOrNull()?.user?.email
                    ?: error("Not logged in")
                client.auth.signInWith(Email) {
                    this.email = currentEmail
                    this.password = oldPassword
                }
                client.auth.updateUser {
                    password = newPassword
                }
                Unit
            }
        }
}
