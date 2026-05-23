package com.example.uni_lift.features.admin.verifications

import com.example.uni_lift.core.models.User
import com.example.uni_lift.core.supabase.SupabaseProvider
import com.example.uni_lift.core.supabase.VerificationRow
import com.example.uni_lift.core.supabase.VerificationStatusUpdate
import com.example.uni_lift.core.supabase.VerificationStatusWithReason
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminVerificationsRepository : AdminVerificationsContract.Repository {

    override suspend fun getPendingAccount(token: String): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("verifications")
                    .select { filter { eq("status", "PENDING") } }
                    .decodeList<VerificationRow>()
                    .map { it.toPlaceholderUser() }
            }
        }

    override suspend fun getPendingDriver(token: String): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("verifications")
                    .select {
                        filter {
                            eq("status", "PENDING")
                            neq("vehicle_type", "")
                        }
                    }
                    .decodeList<VerificationRow>()
                    .map { it.toPlaceholderUser() }
            }
        }

    override suspend fun approveAccount(token: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("verifications")
                    .update(VerificationStatusUpdate(status = "APPROVED")) {
                        filter { eq("user_id", userId) }
                    }
                Unit
            }
        }

    override suspend fun approveDriver(token: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("verifications")
                    .update(VerificationStatusUpdate(status = "APPROVED")) {
                        filter { eq("user_id", userId) }
                    }
                Unit
            }
        }

    override suspend fun rejectAccount(token: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("verifications")
                    .update(VerificationStatusWithReason(
                        status = "REJECTED",
                        rejectionReason = "Rejected by admin"
                    )) {
                        filter { eq("user_id", userId) }
                    }
                Unit
            }
        }

    private fun VerificationRow.toPlaceholderUser() = User(
        id = userId,
        email = "",
        fullName = "User $userId",
        accountStatus = "PENDING",
        driverVerificationStatus = status,
        vehicleType = vehicleType,
        vehicle = plateNumber
    )
}
