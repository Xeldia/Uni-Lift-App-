package com.example.uni_lift.features.rides.history

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.uni_lift.core.models.RideRecord
import com.example.uni_lift.core.session.SessionManager

// ─── Status colors ────────────────────────────────────────────────────────────

private val colorCompleted = Color(0xFF10B981)
private val colorCancelled = Color(0xFFEF4444)
private val colorAmber     = Color(0xFFF59E0B)

private fun statusColor(status: String): Color = when (status.uppercase()) {
    "COMPLETED"  -> colorCompleted
    "CANCELLED"  -> colorCancelled
    else         -> colorAmber   // SEARCHING, MATCHED, IN_PROGRESS, etc.
}

// ─── Route ───────────────────────────────────────────────────────────────────

@Composable
fun RideHistoryRoute(
    onBackClick: () -> Unit,
    presenter: RideHistoryContract.Presenter = run {
        val context = LocalContext.current
        remember {
            RideHistoryPresenter(
                repository     = RideHistoryRepository(context),
                sessionManager = SessionManager(context)
            )
        }
    }
) {
    var uiState by remember { mutableStateOf(RideHistoryContract.UiState()) }

    val view = remember(onBackClick) {
        object : RideHistoryContract.View {
            override fun render(state: RideHistoryContract.UiState) {
                uiState = state
            }
        }
    }

    DisposableEffect(presenter, view) {
        presenter.attach(view)
        onDispose { presenter.detach() }
    }

    RideHistoryScreen(
        state       = uiState,
        onBackClick = onBackClick
    )
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen(
    state: RideHistoryContract.UiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text       = "RIDE HISTORY",
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = Color.Black
                    )
                }

                state.error != null -> {
                    Column(
                        modifier            = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text       = state.error,
                            color      = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text       = "Please try again later.",
                            color      = Color.Gray,
                            fontSize   = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                state.rides.isEmpty() -> {
                    Text(
                        text       = "No rides yet",
                        modifier   = Modifier.align(Alignment.Center),
                        color      = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                else -> {
                    LazyColumn(
                        modifier            = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.rides) { ride ->
                            RideCard(ride = ride)
                        }
                    }
                }
            }
        }
    }
}

// ─── Ride card ───────────────────────────────────────────────────────────────

@Composable
private fun RideCard(ride: RideRecord) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Status badge + date row
            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBadge(status = ride.status)
                Text(
                    text       = formatDate(ride.createdAt),
                    fontSize   = 11.sp,
                    color      = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pickup → dropoff
            Text(
                text       = ride.pickup,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text       = "↓",
                color      = Color.Gray,
                fontSize   = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text       = ride.dropoff,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Fare, pax count, ride type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = "₱%.2f".format(ride.fare),
                    fontWeight = FontWeight.Black,
                    fontSize   = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text       = "${ride.passengerCount} pax",
                    color      = Color.Gray,
                    fontSize   = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(12.dp))
                RideTypeChip(rideType = ride.rideType)
            }
        }
    }
}

// ─── Status badge ─────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(status: String) {
    Surface(
        color        = statusColor(status),
        shape        = RoundedCornerShape(4.dp)
    ) {
        Text(
            text       = status.uppercase(),
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color      = Color.White,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─── Ride type chip ───────────────────────────────────────────────────────────

@Composable
private fun RideTypeChip(rideType: String) {
    Surface(
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text       = rideType.uppercase(),
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color      = Color.Black,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─── Date formatter ───────────────────────────────────────────────────────────

private fun formatDate(createdAt: String): String {
    // createdAt is an ISO-8601 string, e.g. "2024-05-01T14:30:00.000Z"
    // Trim to just the date + short time portion for display
    return try {
        val withoutMillis = createdAt.substringBefore('.')
        val date = withoutMillis.substringBefore('T')
        val time = withoutMillis.substringAfter('T', "")
        if (time.isNotEmpty()) "$date $time" else date
    } catch (e: Exception) {
        createdAt
    }
}
