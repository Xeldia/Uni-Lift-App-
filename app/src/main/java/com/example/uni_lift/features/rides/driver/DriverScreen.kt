package com.example.uni_lift.features.rides.driver

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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

// ─── Route ───────────────────────────────────────────────────────────────────

@Composable
fun DriverRoute(
    presenter: DriverContract.Presenter = run {
        val context = LocalContext.current
        remember { DriverPresenter(DriverRepository(context), context) }
    }
) {
    var uiState by remember { mutableStateOf(DriverContract.UiState()) }

    val view = remember {
        object : DriverContract.View {
            override fun render(state: DriverContract.UiState) { uiState = state }
            override fun showError(message: String) { uiState = uiState.copy(error = message) }
        }
    }

    DisposableEffect(presenter, view) {
        presenter.attach(view)
        onDispose { presenter.detach() }
    }

    DriverScreen(
        state = uiState,
        onToggleOnline = presenter::onToggleOnline,
        onAccept = { presenter.onAcceptRide(it) },
        onDecline = { presenter.onDeclineRide(it) },
        onStartRide = { presenter.onStartRide(it) },
        onSimulateRide = { presenter.onSimulateRide(it) },
        onComplete = { presenter.onCompleteRide(it) }
    )
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    state: DriverContract.UiState,
    onToggleOnline: () -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onStartRide: (String) -> Unit,
    onSimulateRide: (String) -> Unit,
    onComplete: (String) -> Unit
) {
    val onlineColor = Color(0xFF10B981)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("DRIVER MODE", fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black, titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))) {
            if (state.activeRide != null) {
                ActiveRideMapView(
                    ride = state.activeRide,
                    isLoading = state.isLoading,
                    isSimulating = state.isSimulating,
                    paddingValues = paddingValues,
                    onStartRide = onStartRide,
                    onSimulateRide = onSimulateRide,
                    onComplete = onComplete
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace, color = Color.Gray)
                                Text(
                                    text = if (state.isOnline) "ONLINE" else "OFFLINE",
                                    fontSize = 20.sp, fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (state.isOnline) onlineColor else Color.Black
                                )
                            }
                            Switch(
                                checked = state.isOnline,
                                onCheckedChange = { onToggleOnline() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = onlineColor,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("ERROR: ${state.error}", modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                                color = Color(0xFFB00020), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    when {
                        state.isOnline -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("INCOMING REQUESTS", fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("${state.requests.size} AVAILABLE", fontSize = 11.sp,
                                    color = Color.Gray, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.requests.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("NO REQUESTS YET\nWAITING FOR PASSENGERS...",
                                        fontSize = 13.sp, color = Color.Gray,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold, lineHeight = 20.sp)
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(state.requests, key = { it.id }) { ride ->
                                        RequestCard(ride = ride, onAccept = onAccept, onDecline = onDecline)
                                    }
                                }
                            }
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("GO ONLINE TO RECEIVE REQUESTS", fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                                    color = Color.Gray)
                            }
                        }
                    }
                }
            }

            if (state.isLoading && state.activeRide == null) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Black)
                }
            }
        }
    }
}

// ─── Active Ride Map View ─────────────────────────────────────────────────────

@Composable
private fun ActiveRideMapView(
    ride: RideRecord,
    isLoading: Boolean,
    isSimulating: Boolean,
    paddingValues: PaddingValues,
    onStartRide: (String) -> Unit,
    onSimulateRide: (String) -> Unit,
    onComplete: (String) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { createDriverMapView(context) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    LaunchedEffect(ride.pickupLat, ride.dropoffLat) {
        val pLat = ride.pickupLat ?: return@LaunchedEffect
        val pLng = ride.pickupLng ?: return@LaunchedEffect
        val dLat = ride.dropoffLat ?: return@LaunchedEffect
        val dLng = ride.dropoffLng ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://router.project-osrm.org/route/v1/driving/$pLng,$pLat;$dLng,$dLat?geometries=polyline&overview=full"
                val resp = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
                val body = resp.body?.string() ?: return@runCatching emptyList<GeoPoint>()
                val json = Json.parseToJsonElement(body)
                val encoded = json.jsonObject["routes"]!!.jsonArray[0]
                    .jsonObject["geometry"]!!.jsonPrimitive.content
                decodePolyline(encoded)
            }.getOrElse { emptyList() }
        }.let { if (it.isNotEmpty()) routePoints = it }
    }

    DisposableEffect(Unit) { onDispose { mapView.onDetach() } }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                val pLat = ride.pickupLat ?: return@AndroidView
                val pLng = ride.pickupLng ?: return@AndroidView
                val dLat = ride.dropoffLat ?: return@AndroidView
                val dLng = ride.dropoffLng ?: return@AndroidView
                val drvLat = ride.driverLat
                val drvLng = ride.driverLng

                map.overlays.clear()
                val pickupPt = GeoPoint(pLat, pLng)
                val dropoffPt = GeoPoint(dLat, dLng)

                map.overlays.add(Polyline().apply {
                    setPoints(if (routePoints.isNotEmpty()) routePoints else listOf(pickupPt, dropoffPt))
                    outlinePaint.color = android.graphics.Color.parseColor("#10B981")
                    outlinePaint.strokeWidth = 10f
                })
                map.overlays.add(Marker(map).apply {
                    position = pickupPt; title = "Pickup"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                })
                map.overlays.add(Marker(map).apply {
                    position = dropoffPt; title = "Destination"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                })
                if (drvLat != null && drvLng != null) {
                    map.overlays.add(Marker(map).apply {
                        position = GeoPoint(drvLat, drvLng); title = "You"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    })
                }

                val pts = mutableListOf(pickupPt, dropoffPt)
                if (drvLat != null && drvLng != null) pts.add(GeoPoint(drvLat, drvLng))
                map.zoomToBoundingBox(BoundingBox.fromGeoPoints(pts).increaseByScale(1.4f), false)
                map.invalidate()
            }
        )

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("ACTIVE RIDE", fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                    RideStatusChip(ride.status)
                }

                Spacer(Modifier.height(12.dp))
                Text("FROM", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
                Text(ride.pickup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("TO", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
                Text(ride.dropoff, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₱${"%.2f".format(ride.fare)}", color = Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("  •  ${ride.passengerCount} pax  •  ${ride.rideType}",
                        color = Color.Gray, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(Modifier.height(16.dp))

                if (ride.status == "ACCEPTED") {
                    Button(
                        onClick = { onStartRide(ride.id) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("START RIDE →", color = Color.White,
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = { onSimulateRide(ride.id) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSimulating && ride.pickupLat != null,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6))
                ) {
                    if (isSimulating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color(0xFF3B82F6),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (isSimulating) "SIMULATING RIDE..." else "SIMULATE RIDE",
                        color = Color(0xFF3B82F6),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { onComplete(ride.id) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("COMPLETE RIDE", color = Color.Black,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Request Card ─────────────────────────────────────────────────────────────

@Composable
private fun RequestCard(
    ride: RideRecord,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            RideDetailRow(label = "PICKUP", value = ride.pickup)
            Spacer(modifier = Modifier.height(6.dp))
            RideDetailRow(label = "DROPOFF", value = ride.dropoff)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                RideDetailRow(label = "PAX", value = "${ride.passengerCount}")
                RideDetailRow(label = "FARE", value = "₱${"%.2f".format(ride.fare)}")
                RideDetailRow(label = "TYPE", value = ride.rideType)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onDecline(ride.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("DECLINE", fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = { onAccept(ride.id) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("ACCEPT", color = Color.White, fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── Status Chip ──────────────────────────────────────────────────────────────

@Composable
private fun RideStatusChip(status: String) {
    val (color, label) = when (status) {
        "ACCEPTED" -> Color(0xFF3B82F6) to "ACCEPTED"
        "IN_PROGRESS" -> Color(0xFF10B981) to "IN PROGRESS"
        "COMPLETED" -> Color(0xFF6B7280) to "COMPLETED"
        else -> Color(0xFFF59E0B) to status
    }
    Surface(shape = RoundedCornerShape(4.dp), color = color) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = Color.White, fontFamily = FontFamily.Monospace)
    }
}

// ─── Shared helpers ───────────────────────────────────────────────────────────

@Composable
private fun RideDetailRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace, color = Color.Black)
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

private fun createDriverMapView(context: Context): MapView {
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
