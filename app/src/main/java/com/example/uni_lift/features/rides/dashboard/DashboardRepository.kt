package com.example.uni_lift.features.rides.dashboard

import android.content.Context
import com.example.uni_lift.core.models.RideRecord
import com.example.uni_lift.core.session.SessionManager
import com.example.uni_lift.core.supabase.RideInsert
import com.example.uni_lift.core.supabase.RideRow
import com.example.uni_lift.core.supabase.RideStatusUpdate
import com.example.uni_lift.core.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class DashboardRepository(private val context: Context) : DashboardContract.Repository {

    private var realtimeChannel: RealtimeChannel? = null
    private var realtimeScope: CoroutineScope? = null

    private fun mapVehicleType(uiType: String) = when (uiType.uppercase()) {
        "CAR" -> "CAR"
        "SIDECAR" -> "SHUTTLE"
        else -> "HABAL"
    }

    private fun RideRow.toRideRecord() = RideRecord(
        id = id, riderId = riderId, driverId = driverId,
        pickup = pickup, dropoff = dropoff,
        pickupLat = pickupLat, pickupLng = pickupLng,
        dropoffLat = dropoffLat, dropoffLng = dropoffLng,
        driverLat = driverLat, driverLng = driverLng,
        fare = fare ?: 0.0, rideType = rideType ?: "MOTO",
        status = status, createdAt = createdAt ?: "", updatedAt = updatedAt
    )

    override suspend fun getActiveRide(token: String): Result<RideRecord?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val userId = SessionManager(context).fetchUserId() ?: return@runCatching null
                val rows = SupabaseProvider.client.from("rides").select {
                    filter {
                        eq("rider_id", userId)
                        isIn("status", listOf("SEARCHING", "ACCEPTED", "IN_TRANSIT", "IN_PROGRESS"))
                    }
                }.decodeList<RideRow>()
                rows.firstOrNull()?.toRideRecord()
            }
        }

    override suspend fun createRide(token: String, form: BookingForm): Result<RideRecord> =
        withContext(Dispatchers.IO) {
            runCatching {
                val fare = form.fare.toDoubleOrNull() ?: error("Invalid fare amount")
                val client = SupabaseProvider.client
                val userId = client.auth.currentSessionOrNull()?.user?.id
                    ?: error("Not logged in")
                val riderName = SessionManager(context).fetchUserName()
                val insert = RideInsert(
                    riderId = userId,
                    pickup = form.pickup.ifBlank { "Current Location" },
                    dropoff = form.dropoff,
                    pickupLat = form.pickupLat,
                    pickupLng = form.pickupLng,
                    dropoffLat = form.dropoffLat,
                    dropoffLng = form.dropoffLng,
                    fare = fare,
                    rideType = mapVehicleType(form.vehicleType),
                    riderName = riderName
                )
                val row = client.from("rides").insert(insert) {
                    select()
                }.decodeSingle<RideRow>()
                row.toRideRecord()
            }
        }

    override suspend fun cancelRide(token: String, rideId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                SupabaseProvider.client.from("rides")
                    .update(RideStatusUpdate(status = "CANCELLED")) {
                        filter { eq("id", rideId) }
                    }
                Unit
            }
        }

    override suspend fun subscribeToRideUpdates(rideId: String, onUpdate: (RideRecord) -> Unit) {
        try {
            // Tear down any existing channel before creating a new one.
            // Re-using the same channel name causes "postgresChangeFlow after joining" crash.
            realtimeScope?.cancel()
            realtimeScope = null
            realtimeChannel = null

            val client = SupabaseProvider.client
            client.realtime.connect()
            val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            realtimeScope = scope

            // Unique name guarantees a fresh channel every time.
            val channel = client.channel("ride-$rideId-${System.currentTimeMillis()}")
            realtimeChannel = channel

            // Register listener BEFORE subscribing — required by Supabase Realtime.
            channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "rides"
                filter("id", FilterOperator.EQ, rideId)
            }.onEach { action ->
                val r = action.record
                fun str(key: String) = r[key]?.jsonPrimitive?.contentOrNull
                fun dbl(key: String) = str(key)?.toDoubleOrNull()
                val updated = RideRecord(
                    id = rideId,
                    riderId = str("rider_id") ?: "",
                    driverId = str("driver_id"),
                    pickup = str("pickup") ?: "",
                    dropoff = str("dropoff") ?: "",
                    pickupLat = dbl("pickup_lat"),
                    pickupLng = dbl("pickup_lng"),
                    dropoffLat = dbl("dropoff_lat"),
                    dropoffLng = dbl("dropoff_lng"),
                    driverLat = dbl("driver_lat"),
                    driverLng = dbl("driver_lng"),
                    fare = dbl("fare") ?: 0.0,
                    status = str("status") ?: "",
                    rideType = str("ride_type") ?: "MOTO",
                    createdAt = str("created_at") ?: ""
                )
                onUpdate(updated)
            }.launchIn(scope)

            channel.subscribe()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun unsubscribeFromRideUpdates() {
        realtimeChannel?.let { ch ->
            realtimeScope?.let { scope ->
                scope.cancel()
                // unsubscribe without blocking — channel cleanup is best-effort
            }
        }
        realtimeChannel = null
        realtimeScope = null
    }
}
