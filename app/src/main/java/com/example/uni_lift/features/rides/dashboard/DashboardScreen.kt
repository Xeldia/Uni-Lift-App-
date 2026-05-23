package com.example.uni_lift.features.rides.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardRoute(
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onPickupLocationClick: () -> Unit,
    pickupSelection: PickupSelection?,
    onPickupSelectionConsumed: () -> Unit,
    onDestinationLocationClick: () -> Unit,
    destinationSelection: DestinationSelection?,
    onDestinationSelectionConsumed: () -> Unit,
    onNavigateToActiveRide: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val presenter = remember { DashboardPresenter(DashboardRepository(context), context) }
    var uiState by remember { mutableStateOf(DashboardContract.UiState()) }

    // rememberSaveable survives NavHost disposal when navigating to location_picker and back
    var savedPickup by rememberSaveable { mutableStateOf("") }
    var savedPickupLat by rememberSaveable { mutableStateOf(0.0) }
    var savedPickupLng by rememberSaveable { mutableStateOf(0.0) }
    var savedDropoff by rememberSaveable { mutableStateOf("") }
    var savedDropoffLat by rememberSaveable { mutableStateOf(0.0) }
    var savedDropoffLng by rememberSaveable { mutableStateOf(0.0) }

    val view = remember(onProfileClick, onLogoutClick) {
        object : DashboardContract.View {
            override fun render(state: DashboardContract.UiState) { uiState = state }
            override fun navigateToProfile() { onProfileClick() }
            override fun navigateToLogin() { onLogoutClick() }
        }
    }

    DisposableEffect(presenter, view) {
        presenter.attach(view)
        onDispose { presenter.detach() }
    }

    // Restore saved form data into the fresh presenter after navigation disposal/recreation
    LaunchedEffect(Unit) {
        if (savedPickup.isNotBlank()) presenter.onPickupLocationSelected(savedPickup, savedPickupLat, savedPickupLng)
        if (savedDropoff.isNotBlank()) presenter.onDestinationLocationSelected(savedDropoff, savedDropoffLat, savedDropoffLng)
    }

    LaunchedEffect(pickupSelection) {
        pickupSelection?.let {
            savedPickup = it.label; savedPickupLat = it.lat; savedPickupLng = it.lng
            presenter.onPickupLocationSelected(it.label, it.lat, it.lng)
            onPickupSelectionConsumed()
        }
    }

    LaunchedEffect(destinationSelection) {
        destinationSelection?.let {
            savedDropoff = it.label; savedDropoffLat = it.lat; savedDropoffLng = it.lng
            presenter.onDestinationLocationSelected(it.label, it.lat, it.lng)
            onDestinationSelectionConsumed()
        }
    }
    // NOTE: Auto-navigation removed — caused infinite loop on every recomposition.
    // Navigation to ActiveRideScreen is now user-initiated via the VIEW RIDE button on the card.

    DashboardScreen(
        state = uiState,
        onPickupLocationClick = onPickupLocationClick,
        onDestinationLocationClick = onDestinationLocationClick,
        onPassengerCountChanged = presenter::onPassengerCountChanged,
        onFareChanged = presenter::onFareChanged,
        onVehicleTypeChanged = presenter::onVehicleTypeChanged,
        onFindDriverClick = presenter::onFindDriverClicked,
        onCancelRideClick = presenter::onCancelRideClicked,
        onToggleMap = presenter::onToggleMap,
        onViewRideClick = { rideId -> onNavigateToActiveRide(rideId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardContract.UiState,
    onPickupLocationClick: () -> Unit,
    onDestinationLocationClick: () -> Unit,
    onPassengerCountChanged: (Int) -> Unit,
    onFareChanged: (String) -> Unit,
    onVehicleTypeChanged: (String) -> Unit,
    onFindDriverClick: () -> Unit,
    onCancelRideClick: () -> Unit,
    onToggleMap: () -> Unit,
    onViewRideClick: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Greeting
        if (state.welcomeName.isNotBlank()) {
            Text(
                text = "WELCOME, ${state.welcomeName.uppercase()}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Active ride card
        if (state.activeRide != null) {
            ActiveRideCard(
                ride = state.activeRide,
                isLoading = state.isLoading,
                onCancelClick = onCancelRideClick,
                onViewClick = { onViewRideClick(state.activeRide.id) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Booking card (only if no active ride)
        if (state.activeRide == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "WHERE TO TODAY?",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Real-Time Vehicle Share Network for Campus Transport",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pickup
                    Text("PICKUP LOCATION", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.form.pickup,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Choose location") },
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        readOnly = true,
                        enabled = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onPickupLocationClick) {
                        Text("CHOOSE LOCATION", fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Destination
                    Text("DESTINATION", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.form.dropoff,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Where are you going?") },
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onDestinationLocationClick) {
                        Text("CHOOSE DESTINATION", fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Passenger count
                    Text("PASSENGERS", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..4).forEach { count ->
                            val selected = state.form.passengerCount == count
                            if (selected) {
                                Button(
                                    onClick = { onPassengerCountChanged(count) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) { Text("$count", color = Color.White, fontSize = 14.sp) }
                            } else {
                                OutlinedButton(
                                    onClick = { onPassengerCountChanged(count) },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) { Text("$count", color = Color.Black, fontSize = 14.sp) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fare
                    Text("FARE (₱)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.form.fare,
                        onValueChange = onFareChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter fare amount") },
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = { Text("₱ ") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vehicle type
                    Text("VEHICLE TYPE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MOTO", "CAR", "SIDECAR").forEach { type ->
                            val selected = state.form.vehicleType == type
                            if (selected) {
                                Button(
                                    onClick = { onVehicleTypeChanged(type) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) { Text(type, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
                            } else {
                                OutlinedButton(
                                    onClick = { onVehicleTypeChanged(type) },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) { Text(type, color = Color.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
                            }
                        }
                    }

                    // Error
                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.error, color = Color.Red, fontSize = 12.sp)
                    }

                    // Success message
                    if (state.successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.successMessage, color = Color(0xFF10B981), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onToggleMap,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            if (state.showMap) "HIDE MAP" else "VIEW CAMPUS MAP →",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onFindDriverClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(4.dp),
                        enabled = !state.isLoading && !state.isSearching
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                if (state.isSearching) "SEARCHING FOR DRIVER..." else "FIND A DRIVER →",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Activity status
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF10B981), RoundedCornerShape(4.dp))
            )
            Text(
                text = "  NETWORK ACTIVE",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ActiveRideCard(
    ride: com.example.uni_lift.core.models.RideRecord,
    isLoading: Boolean,
    onCancelClick: () -> Unit,
    onViewClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ACTIVE RIDE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                StatusBadge(ride.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("FROM", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
            Text(ride.pickup, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text("TO", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
            Text(ride.dropoff, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text("₱${ride.fare}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("  •  ${ride.passengerCount} pax", color = Color.Gray, fontSize = 13.sp)
            }

            // Searching indicator
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFFF59E0B),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "SEARCHING FOR DRIVER...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // View ride button
                Button(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("VIEW RIDE", color = Color.Black, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                // Cancel button
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CANCEL", color = Color.Red, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "SEARCHING" -> Color(0xFFF59E0B) to "SEARCHING"
        "MATCHED" -> Color(0xFF3B82F6) to "MATCHED"
        "IN_PROGRESS", "ACCEPTED", "TRIP_IN_PROGRESS" -> Color(0xFF10B981) to "IN PROGRESS"
        "COMPLETED" -> Color(0xFF6B7280) to "COMPLETED"
        else -> Color(0xFFF59E0B) to status
    }
    Surface(shape = RoundedCornerShape(4.dp), color = color) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
    }
}
