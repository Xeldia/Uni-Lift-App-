package com.example.uni_lift.features.admin

import android.content.Context
import com.example.uni_lift.core.api.RetrofitClient
import com.example.uni_lift.core.session.SessionManager

class AdminRepository(context: Context) : AdminContract.Repository {

    private val api = RetrofitClient.instance
    private val session = SessionManager(context)

    override suspend fun getStats(token: String): Result<AdminStats> {
        return try {
            val usersResponse = api.getAllUsers("Bearer $token")
            val ridesResponse = api.getRides("Bearer $token")

            if (!usersResponse.isSuccessful) {
                return Result.failure(Exception("Failed to load users: ${usersResponse.code()}"))
            }
            if (!ridesResponse.isSuccessful) {
                return Result.failure(Exception("Failed to load rides: ${ridesResponse.code()}"))
            }

            val users = usersResponse.body()?.data ?: emptyList()
            val rides = ridesResponse.body()?.data ?: emptyList()

            val stats = AdminStats(
                totalUsers = users.size,
                activeDrivers = users.count { it.role == "DRIVER" },
                ridesInProgress = rides.count { it.status in listOf("SEARCHING", "MATCHED", "IN_PROGRESS") },
                pendingVerifications = users.count { it.driverVerificationStatus == "PENDING" },
                completedRides = rides.count { it.status == "COMPLETED" }
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
