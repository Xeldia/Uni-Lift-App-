package com.example.uni_lift.features.rides.history

import android.content.Context
import com.example.uni_lift.core.models.RideRecord
import com.example.uni_lift.core.session.SessionManager
import com.example.uni_lift.core.supabase.RideRow
import com.example.uni_lift.core.supabase.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RideHistoryRepository(private val context: Context) : RideHistoryContract.Repository {

    private fun RideRow.toRideRecord() = RideRecord(
        id = id, riderId = riderId, driverId = driverId,
        pickup = pickup, dropoff = dropoff,
        fare = fare ?: 0.0, rideType = rideType ?: "MOTO",
        status = status, createdAt = createdAt ?: "", updatedAt = updatedAt
    )

    override suspend fun getRides(token: String): Result<List<RideRecord>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val userId = SessionManager(context).fetchUserId() ?: error("Not logged in")
                SupabaseProvider.client.from("rides").select {
                    filter { eq("rider_id", userId) }
                    order("created_at", Order.DESCENDING)
                }.decodeList<RideRow>().map { it.toRideRecord() }
            }
        }
}
