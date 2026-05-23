package com.example.uni_lift.features.rides.driver

import android.content.Context
import com.example.uni_lift.core.models.RideRecord
import com.example.uni_lift.core.session.SessionManager
import com.example.uni_lift.core.supabase.DriverLocationUpdate
import com.example.uni_lift.core.supabase.RideRow
import com.example.uni_lift.core.supabase.RideStatusUpdate
import com.example.uni_lift.core.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriverRepository(private val context: Context) : DriverContract.Repository {

    private val session by lazy { SessionManager(context) }
    fun getToken(): String? = session.fetchAuthToken()

    private fun RideRow.toRideRecord() = RideRecord(
        id = id, riderId = riderId, driverId = driverId,
        pickup = pickup, dropoff = dropoff,
        pickupLat = pickupLat, pickupLng = pickupLng,
        dropoffLat = dropoffLat, dropoffLng = dropoffLng,
        driverLat = driverLat, driverLng = driverLng,
        fare = fare ?: 0.0, rideType = rideType ?: "MOTO",
        status = status, createdAt = createdAt ?: "", updatedAt = updatedAt
    )

    override suspend fun getRidingRequests(token: String): Result<List<RideRecord>> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("rides").select {
                    filter { eq("status", "SEARCHING") }
                }.decodeList<RideRow>().map { it.toRideRecord() }
            }
        }

    override suspend fun acceptRide(token: String, id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val driverId = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.id
                    ?: error("Not logged in")
                SupabaseProvider.client.from("rides")
                    .update(RideStatusUpdate(status = "ACCEPTED", driverId = driverId)) {
                        filter { eq("id", id) }
                    }
                Unit
            }
        }

    override suspend fun startRide(token: String, id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("rides")
                    .update(RideStatusUpdate(status = "IN_PROGRESS")) {
                        filter { eq("id", id) }
                    }
                Unit
            }
        }

    override suspend fun completeRide(token: String, id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("rides")
                    .update(RideStatusUpdate(status = "COMPLETED")) {
                        filter { eq("id", id) }
                    }
                Unit
            }
        }

    override suspend fun updateDriverLocation(rideId: String, lat: Double, lng: Double): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("rides")
                    .update(DriverLocationUpdate(driverLat = lat, driverLng = lng)) {
                        filter { eq("id", rideId) }
                    }
                Unit
            }
        }
}
