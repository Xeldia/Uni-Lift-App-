package com.example.uni_lift.features.admin.users

import com.example.uni_lift.core.models.User
import com.example.uni_lift.core.supabase.AccountStatusUpdate
import com.example.uni_lift.core.supabase.SupabaseProvider
import com.example.uni_lift.core.supabase.UserRow
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminUsersRepository : AdminUsersContract.Repository {

    override suspend fun getUsers(token: String): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("users")
                    .select()
                    .decodeList<UserRow>()
                    .map { it.toUser() }
            }
        }

    override suspend fun suspendUser(token: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("users")
                    .update(AccountStatusUpdate(accountStatus = "SUSPENDED")) {
                        filter { eq("id", userId) }
                    }
                Unit
            }
        }

    override suspend fun reactivateUser(token: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("users")
                    .update(AccountStatusUpdate(accountStatus = "ACTIVE")) {
                        filter { eq("id", userId) }
                    }
                Unit
            }
        }

    private fun UserRow.toUser() = User(
        id = id,
        email = email,
        fullName = fullName,
        studentId = studentId,
        phoneNumber = phoneNumber,
        role = role.uppercase(),
        university = university,
        isVerified = isVerified,
        accountStatus = accountStatus ?: "ACTIVE",
        driverVerificationStatus = driverVerificationStatus,
        avatarUrl = avatarUrl,
        vehicle = vehicle,
        vehicleType = vehicleType,
        rating = rating,
        ridesCompleted = ridesCompleted
    )
}
