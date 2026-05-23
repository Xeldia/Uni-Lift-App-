package com.example.uni_lift.features.auth.login

import android.content.Context
import com.example.uni_lift.core.models.User
import com.example.uni_lift.core.session.SessionManager
import com.example.uni_lift.core.supabase.SupabaseProvider
import com.example.uni_lift.core.supabase.UserRow
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRepository(private val context: Context) : LoginContract.Repository {

    override suspend fun login(email: String, password: String): Result<AuthData> =
        withContext(Dispatchers.IO) {
            runCatching {
                val client = SupabaseProvider.client
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val session = client.auth.currentSessionOrNull()
                    ?: error("Login failed — no session returned")
                val userId = session.user?.id ?: error("No user ID in session")

                val userRow = client.from("users")
                    .select { filter { eq("id", userId) } }
                    .decodeSingle<UserRow>()

                val sm = SessionManager(context)
                sm.saveSession(
                    token = session.accessToken,
                    userId = userId,
                    role = userRow.role.uppercase(),
                    name = userRow.fullName,
                    email = userRow.email
                )
                session.refreshToken?.let { sm.saveRefreshToken(it) }

                AuthData(
                    token = session.accessToken,
                    refreshToken = session.refreshToken,
                    user = User(
                        id = userId,
                        email = userRow.email,
                        fullName = userRow.fullName,
                        role = userRow.role.uppercase(),
                        studentId = userRow.studentId,
                        phoneNumber = userRow.phoneNumber,
                        university = userRow.university,
                        isVerified = userRow.isVerified,
                        accountStatus = userRow.accountStatus ?: userRow.status,
                        avatarUrl = userRow.avatarUrl,
                        vehicle = userRow.vehicle,
                        vehicleType = userRow.vehicleType,
                        rating = userRow.rating,
                        ridesCompleted = userRow.ridesCompleted
                    )
                )
            }
        }
}
