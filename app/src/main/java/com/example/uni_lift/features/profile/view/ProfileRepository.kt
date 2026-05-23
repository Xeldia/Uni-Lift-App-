package com.example.uni_lift.features.profile.view

import android.content.Context
import com.example.uni_lift.core.session.SessionManager
import com.example.uni_lift.core.supabase.SupabaseProvider
import com.example.uni_lift.core.supabase.UserRow
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(private val context: Context) : ProfileContract.Repository {

    override suspend fun loadProfile(token: String, userId: String): Result<ProfileUiData> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sm = SessionManager(context)
                val uid = sm.fetchUserId() ?: error("Not logged in")
                val row = SupabaseProvider.client.from("users")
                    .select { filter { eq("id", uid) } }
                    .decodeSingle<UserRow>()
                val initials = row.fullName.split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifEmpty { "??" }
                ProfileUiData(
                    initials = initials,
                    fullName = row.fullName,
                    studentId = row.studentId ?: "",
                    email = row.email,
                    contactNumber = row.phoneNumber ?: "",
                    campusLocation = row.university ?: "",
                    role = row.role.uppercase(),
                    accountStatus = row.accountStatus ?: row.status,
                    isVerified = row.isVerified,
                    driverVerificationStatus = row.driverVerificationStatus,
                    avatarUrl = row.avatarUrl,
                    vehicle = row.vehicle,
                    vehicleType = row.vehicleType,
                    rating = row.rating,
                    ridesCompleted = row.ridesCompleted,
                    userId = uid
                )
            }
        }
}
