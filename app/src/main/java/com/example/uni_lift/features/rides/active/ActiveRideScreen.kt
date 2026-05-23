package com.example.uni_lift.features.rides.active

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.uni_lift.core.models.RideRecord
import com.example.uni_lift.core.supabase.RideRow
import com.example.uni_lift.core.supabase.RideStatusUpdate
import com.example.uni_lift.core.supabase.SupabaseProvider
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun ActiveRideRoute(
    rideId: String,
    onRideCompleted: (rideId: String, driverId: String?) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var ride by remember { mutableStateOf<RideRecord?>(null) }
    var simulatedDriverLat by remember { mutableStateOf<Double?>(null) }
    var simulatedDriverLng by remember { mutableStateOf<Double?>(null) }
    var completedNavigated by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }

    val mapView = remember { createActiveMapView(context) }
    var realtimeChannel by remember { mutableStateOf<RealtimeChannel?>(null) }
    var realtimeScope by remember { mutableStateOf<CoroutineScope?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    // Load ride + subscribe to Realtime updates
    LaunchedEffect(rideId) {
        // Initial fetch
        withContext(Dispatchers.IO) {
            runCatching {
                val row = SupabaseProvider.client.from("rides").select {
                    filter { eq("id", rideId) }
                }.decodeSingle<RideRow>()
                RideRecord(
                    id = row.id, riderId = row.riderId, driverId = row.driverId,
                    pickup = row.pickup, dropoff = row.dropoff,
                    pickupLat = row.pickupLat, pickupLng = row.pickupLng,
                    dropoffLat = row.dropoffLat, dropoffLng = row.dropoffLng,
                    driverLat = row.driverLat, driverLng = row.driverLng,
                    fare = row.fare ?: 0.0, rideType = row.rideType ?: "MOTO",
                    status = row.status, createdAt = row.createdAt ?: ""
                )
            }.getOrNull()
        }?.let { ride = it }

        // Tear down any previous channel before creating a new one.
        // This prevents "cannot call postgresChangeFlow after joining the channel"
        // when the LaunchedEffect re-runs (e.g. recomposition / back-stack return).
        realtimeScope?.cancel()
        realtimeScope = null
        realtimeChannel = null

        // Use a unique channel name each time so we always get a fresh channel.
        val client = SupabaseProvider.client
        client.realtime.connect()
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        realtimeScope = scope
        val channelName = "active-ride-$rideId-${System.currentTimeMillis()}"
        val ch = client.channel(channelName)
        realtimeChannel = ch

        // Set up listener BEFORE subscribing — required by Supabase Realtime.
        ch.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "rides"
            filter("id", FilterOperator.EQ, rideId)
        }.onEach { action ->
            val r = action.record
            fun str(k: String) = r[k]?.jsonPrimitive?.contentOrNull
            fun dbl(k: String) = str(k)?.toDoubleOrNull()
            val updated = RideRecord(
                id = rideId,
                riderId = str("rider_id") ?: "",
                driverId = str("driver_id"),
                pickup = str("pickup") ?: "",
                dropoff = str("dropoff") ?: "",
                pickupLat = dbl("pickup_lat"), pickupLng = dbl("pickup_lng"),
                dropoffLat = dbl("dropoff_lat"), dropoffLng = dbl("dropoff_lng"),
                driverLat = dbl("driver_lat"), driverLng = dbl("driver_lng"),
                fare = dbl("fare") ?: 0.0,
                status = str("status") ?: "",
                rideType = str("ride_type") ?: "MOTO",
                createdAt = str("created_at") ?: ""
            )
            ride = updated
            if (updated.status == "COMPLETED" && !completedNavigated) {
                completedNavigated = true
                onRideCompleted(rideId, updated.driverId)
            }
        }.launchIn(scope)
        ch.subscribe()
    }

    // OSRM real routing — fires once pickup+dropoff coords are known
    LaunchedEffect(ride?.pickupLat, ride?.dropoffLat) {
        val r = ride ?: return@LaunchedEffect
        val pLat = r.pickupLat ?: return@LaunchedEffect
        val pLng = r.pickupLng ?: return@LaunchedEffect
        val dLat = r.dropoffLat ?: return@LaunchedEffect
        val dLng = r.dropoffLng ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://router.project-osrm.org/route/v1/driving/$pLng,$pLat;$dLng,$dLat?geometries=polyline&overview=full"
                val resp = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
                val body = resp.body?.string() ?: return@runCatching emptyList()
                val json = Json.parseToJsonElement(body)
                val encoded = json.jsonObject["routes"]!!.jsonArray[0]
                    .jsonObject["geometry"]!!.jsonPrimitive.content
                decodePolyline(encoded)
            }.getOrElse { emptyList() }
        }.let { if (it.isNotEmpty()) routePoints = it }
    }

    // Simulated driver movement — only when ride is active, no real GPS
    LaunchedEffect(ride?.id, ride?.status) {
        val r = ride ?: return@LaunchedEffect
        if (r.driverLat != null) return@LaunchedEffect
        if (r.status !in listOf("ACCEPTED", "IN_PROGRESS", "IN_TRANSIT")) return@LaunchedEffect
        val pLat = r.pickupLat ?: return@LaunchedEffect
        val pLng = r.pickupLng ?: return@LaunchedEffect
        val dLat = r.dropoffLat ?: return@LaunchedEffect
        val dLng = r.dropoffLng ?: return@LaunchedEffect
        for (step in 0..60) {
            val t = step / 60.0
            simulatedDriverLat = pLat + (dLat - pLat) * t
            simulatedDriverLng = pLng + (dLng - pLng) * t
            delay(2000L)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            realtimeScope?.cancel()
            realtimeChannel = null
            realtimeScope = null
            mapView.onDetach()
        }
    }

    val effectiveDriverLat = ride?.driverLat ?: simulatedDriverLat
    val effectiveDriverLng = ride?.driverLng ?: simulatedDriverLng

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen map
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                val pLat = ride?.pickupLat ?: return@AndroidView
                val pLng = ride?.pickupLng ?: return@AndroidView
                val dLat = ride?.dropoffLat ?: return@AndroidView
                val dLng = ride?.dropoffLng ?: return@AndroidView

                map.overlays.clear()
                val pickupPt = GeoPoint(pLat, pLng)
                val dropoffPt = GeoPoint(dLat, dLng)

                // Route polyline — real OSRM route or straight-line fallback
                val polyline = Polyline().apply {
                    setPoints(if (routePoints.isNotEmpty()) routePoints else listOf(pickupPt, dropoffPt))
                    outlinePaint.color = android.graphics.Color.parseColor("#3B82F6")
                    outlinePaint.strokeWidth = 10f
                }
                map.overlays.add(polyline)

                // Pickup marker
                map.overlays.add(Marker(map).apply {
                    position = pickupPt
                    title = "Pickup"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                })

                // Destination marker
                map.overlays.add(Marker(map).apply {
                    position = dropoffPt
                    title = "Destination"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                })

                // Driver marker
                if (effectiveDriverLat != null && effectiveDriverLng != null) {
                    map.overlays.add(Marker(map).apply {
                        position = GeoPoint(effectiveDriverLat, effectiveDriverLng)
                        title = "Driver"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    })
                }

                val points = mutableListOf(pickupPt, dropoffPt)
                effectiveDriverLat?.let { la -> effectiveDriverLng?.let { lo -> points.add(GeoPoint(la, lo)) } }
                map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points).increaseByScale(1.4f), false)
                map.invalidate()
            }
        )

        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(12.dp)
                .background(Color.White, RoundedCornerShape(50))
                .size(40.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
        }

        // Bottom details card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE RIDE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    ride?.let { StatusChip(it.status) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("FROM", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
                Text(ride?.pickup ?: "—", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text("TO", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
                Text(ride?.dropoff ?: "—", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₱${ride?.fare ?: "—"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(ride?.rideType ?: "", color = Color.Gray, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel ride button — always visible so stale rides can be cleared
                OutlinedButton(
                    onClick = {
                        val currentRideId = ride?.id ?: rideId
                        isCancelling = true
                        coroutineScope.launch {
                            runCatching {
                                withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    SupabaseProvider.client.from("rides")
                                        .update(RideStatusUpdate(status = "CANCELLED")) {
                                            filter { eq("id", currentRideId) }
                                        }
                                }
                            }
                            isCancelling = false
                            onBackClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    enabled = !isCancelling,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(color = Color.Red, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CANCEL RIDE", color = Color.Red, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (color, label) = when (status) {
        "SEARCHING" -> Color(0xFFF59E0B) to "SEARCHING"
        "ACCEPTED" -> Color(0xFF3B82F6) to "MATCHED"
        "IN_PROGRESS", "IN_TRANSIT" -> Color(0xFF10B981) to "IN PROGRESS"
        "COMPLETED" -> Color(0xFF6B7280) to "COMPLETED"
        else -> Color(0xFFF59E0B) to status
    }
    Surface(shape = RoundedCornerShape(4.dp), color = color) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = Color.White, fontFamily = FontFamily.Monospace
        )
    }
}

private fun decodePolyline(encoded: String): List<GeoPoint> {
    val points = mutableListOf<GeoPoint>()
    var index = 0; var lat = 0; var lng = 0
    while (index < encoded.length) {
        var b: Int; var shift = 0; var result = 0
        do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
        lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
        shift = 0; result = 0
        do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
        lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
        points.add(GeoPoint(lat / 1e5, lng / 1e5))
    }
    return points
}

private fun createActiveMapView(context: Context): MapView {
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        osmdroidBasePath = context.getExternalFilesDir(null) ?: context.cacheDir
        osmdroidTileCache = java.io.File(osmdroidBasePath, "osm_tiles")
    }
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(15.0)
        controller.setCenter(GeoPoint(10.2897, 123.8628))
    }
}
