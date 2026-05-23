package com.example.uni_lift.features.profile.update

import com.example.uni_lift.core.supabase.ProfileUpdateRow
import com.example.uni_lift.core.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateProfileRepository : UpdateProfileContract.Repository {

    override suspend fun update(token: String, state: UpdateProfileContract.UiState): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val userId = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.id
                    ?: error("Not logged in")
                val updates = ProfileUpdateRow(
                    fullName = state.name.takeIf { it.isNotBlank() },
                    phoneNumber = state.contactNumber.takeIf { it.isNotBlank() },
                    university = state.campusLocation.takeIf { it.isNotBlank() }
                )
                SupabaseProvider.client.from("users")
                    .update(updates) { filter { eq("id", userId) } }
                Unit
            }
        }
}
