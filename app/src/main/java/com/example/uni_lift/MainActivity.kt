package com.example.uni_lift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uni_lift.core.supabase.SupabaseProvider
import com.example.uni_lift.core.supabase.UserRow
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uni_lift.core.session.SessionManager
import com.example.uni_lift.core.theme.UniLiftTheme
import com.example.uni_lift.features.admin.AdminRoute
import com.example.uni_lift.features.admin.users.AdminUsersRoute
import com.example.uni_lift.features.admin.verifications.AdminVerificationsRoute
import com.example.uni_lift.features.auth.login.LoginRoute
import com.example.uni_lift.features.auth.register.RegisterRoute
import com.example.uni_lift.features.messages.ConversationRoute
import com.example.uni_lift.features.messages.MessagesRoute
import com.example.uni_lift.features.profile.update.UpdateProfileRoute
import com.example.uni_lift.features.profile.view.ProfileRoute
import com.example.uni_lift.features.rides.dashboard.DashboardRoute
import com.example.uni_lift.features.rides.dashboard.DestinationSelection
import com.example.uni_lift.features.rides.dashboard.PickupSelection
import com.example.uni_lift.features.rides.active.ActiveRideRoute
import com.example.uni_lift.features.rides.location.LocationPickerRoute
import com.example.uni_lift.features.rides.driver.DriverRoute
import com.example.uni_lift.features.rides.rating.RatingRoute
import com.example.uni_lift.features.rides.history.RideHistoryRoute
import com.example.uni_lift.features.settings.main.SettingsRoute
import com.example.uni_lift.features.settings.password.ChangePasswordRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniLiftTheme {
                UniLiftApp()
            }
        }
    }
}

private data class NavTab(val label: String, val icon: ImageVector, val key: String)

private fun tabsForRole(role: String): List<NavTab> = when (role.uppercase()) {
    "DRIVER" -> listOf(
        NavTab("REQUESTS", Icons.AutoMirrored.Filled.List, "driver_requests"),
        NavTab("MESSAGES", Icons.Default.Email, "messages"),
        NavTab("PROFILE", Icons.Default.Person, "profile")
    )
    "ADMIN" -> listOf(
        NavTab("DASHBOARD", Icons.Default.Home, "admin"),
        NavTab("USERS", Icons.Default.Menu, "admin_users"),
        NavTab("VERIF.", Icons.Default.Check, "admin_verif")
    )
    else -> listOf(
        NavTab("HOME", Icons.Default.Home, "home"),
        NavTab("MESSAGES", Icons.Default.Email, "messages"),
        NavTab("PROFILE", Icons.Default.Person, "profile")
    )
}

@Composable
fun UniLiftApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val session = remember { SessionManager(context) }

    // If already logged in, skip login screen
    val start = if (session.isLoggedIn()) "main/${session.fetchUserRole()}" else "login"

    NavHost(navController = navController, startDestination = start) {

        // ── Auth ──────────────────────────────────────────────────────────────
        composable("login") {
            LoginRoute(
                onLoginSuccess = { role ->
                    navController.navigate("main/$role") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterRoute(
                onRegisterSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // ── Main (bottom nav shell) ──────────────────────────────────────────
        composable("main/{role}") { back ->
            val role = back.arguments?.getString("role") ?: "RIDER"
            MainScreen(
                role = role,
                onLogout = {
                    session.clearSession()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onNavigate = { dest -> navController.navigate(dest) },
                navController = navController
            )
        }

        // ── Deep screens (no bottom nav) ─────────────────────────────────────
        composable("profile_detail") {
            ProfileRoute(
                onBackClick = { navController.popBackStack() },
                onUpdateProfileClick = { navController.navigate("update_profile") },
                onSettingsClick = { navController.navigate("settings") },
                onRideHistoryClick = { navController.navigate("ride_history") }
            )
        }
        composable("update_profile") {
            UpdateProfileRoute(
                onBackClick = { navController.popBackStack() },
                onUpdateSuccess = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsRoute(
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate("update_profile") },
                onChangePasswordClick = { navController.navigate("change_password") }
            )
        }
        composable("change_password") {
            ChangePasswordRoute(
                onBackClick = { navController.popBackStack() },
                onPasswordChanged = { navController.popBackStack() }
            )
        }
        composable("ride_history") {
            RideHistoryRoute(onBackClick = { navController.popBackStack() })
        }
        composable("admin_users_detail") {
            AdminUsersRoute(onBackClick = { navController.popBackStack() })
        }
        composable("admin_verif_detail") {
            AdminVerificationsRoute(onBackClick = { navController.popBackStack() })
        }
        composable("conversation/{conversationId}") { back ->
            val convId = back.arguments?.getString("conversationId") ?: ""
            ConversationRoute(
                conversationId = convId,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("active_ride/{rideId}") { back ->
            val rideId = back.arguments?.getString("rideId") ?: return@composable
            ActiveRideRoute(
                rideId = rideId,
                onRideCompleted = { completedRideId, driverId ->
                    navController.navigate("rate_ride/$completedRideId/${driverId ?: "unknown"}") {
                        popUpTo("active_ride/$rideId") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("rate_ride/{rideId}/{driverId}") { back ->
            val rideId = back.arguments?.getString("rideId") ?: return@composable
            val driverId = back.arguments?.getString("driverId") ?: ""
            RatingRoute(
                rideId = rideId,
                driverId = driverId,
                onDone = {
                    navController.navigate("main/${session.fetchUserRole()}") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("location_picker?lat={lat}&lng={lng}&mode={mode}") { back ->
            val lat = back.arguments?.getString("lat")?.toDoubleOrNull() ?: 10.2897
            val lng = back.arguments?.getString("lng")?.toDoubleOrNull() ?: 123.8628
            val mode = back.arguments?.getString("mode") ?: "pickup"
            LocationPickerRoute(
                initialLat = lat,
                initialLng = lng,
                title = if (mode == "destination") "CHOOSE DESTINATION" else "CHOOSE PICKUP",
                onBackClick = { navController.popBackStack() },
                onConfirm = { label, confLat, confLng ->
                    val handle = navController.previousBackStackEntry?.savedStateHandle
                    if (mode == "destination") {
                        handle?.set("dropoff_label", label)
                        handle?.set("dropoff_lat", confLat)
                        handle?.set("dropoff_lng", confLng)
                    } else {
                        handle?.set("pickup_label", label)
                        handle?.set("pickup_lat", confLat)
                        handle?.set("pickup_lng", confLng)
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun MainScreen(
    role: String,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var activeMode by rememberSaveable { mutableStateOf(role.uppercase()) }
    var isVerifiedDriver by rememberSaveable { mutableStateOf(false) }

    // Fetch verification status once — determines if mode toggle is shown
    LaunchedEffect(Unit) {
        val userId = session.fetchUserId() ?: return@LaunchedEffect
        val verified = withContext(Dispatchers.IO) {
            runCatching {
                val row = SupabaseProvider.client.from("users").select {
                    filter { eq("id", userId) }
                }.decodeSingle<UserRow>()
                row.driverVerificationStatus == "APPROVED"
            }.getOrElse { false }
        }
        isVerifiedDriver = verified
    }

    val tabs = remember(activeMode) { tabsForRole(activeMode) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    // Reset tab index when mode switches (tab counts differ)
    LaunchedEffect(activeMode) { selectedIndex = 0 }
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val pickupLabel = savedStateHandle?.get<String>("pickup_label")
    val pickupLat = savedStateHandle?.get<Double>("pickup_lat")
    val pickupLng = savedStateHandle?.get<Double>("pickup_lng")
    val pickupSelection = if (pickupLabel != null && pickupLat != null && pickupLng != null) {
        PickupSelection(pickupLabel, pickupLat, pickupLng)
    } else {
        null
    }
    val dropoffLabel = savedStateHandle?.get<String>("dropoff_label")
    val dropoffLat = savedStateHandle?.get<Double>("dropoff_lat")
    val dropoffLng = savedStateHandle?.get<Double>("dropoff_lng")
    val destinationSelection = if (dropoffLabel != null && dropoffLat != null && dropoffLng != null) {
        DestinationSelection(dropoffLabel, dropoffLat, dropoffLng)
    } else {
        null
    }
    var lastPickupLat by rememberSaveable { mutableStateOf(10.2897) }
    var lastPickupLng by rememberSaveable { mutableStateOf(123.8628) }
    var lastDropoffLat by rememberSaveable { mutableStateOf(10.2897) }
    var lastDropoffLng by rememberSaveable { mutableStateOf(123.8628) }

    LaunchedEffect(pickupSelection) {
        pickupSelection?.let {
            lastPickupLat = it.lat
            lastPickupLng = it.lng
        }
    }

    LaunchedEffect(destinationSelection) {
        destinationSelection?.let {
            lastDropoffLat = it.lat
            lastDropoffLng = it.lng
        }
    }

    Scaffold(
        topBar = {
            if (isVerifiedDriver && role.uppercase() != "ADMIN") {
                Surface(color = Color.Black, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MODE", fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                            color = Color(0xFF666666), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF1A1A1A)) {
                            Row {
                                listOf("RIDER", "DRIVER").forEach { mode ->
                                    val selected = activeMode == mode
                                    TextButton(
                                        onClick = { activeMode = mode },
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (selected) Color.White else Color.Transparent,
                                            contentColor = if (selected) Color.Black else Color(0xFF888888)
                                        )
                                    ) {
                                        Text(mode, fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.Black) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(tab.icon, contentDescription = tab.label,
                                tint = if (selectedIndex == index) Color.White else Color(0xFF666666))
                        },
                        label = {
                            Text(tab.label, fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                                color = if (selectedIndex == index) Color.White else Color(0xFF666666))
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF222222)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tabs.getOrNull(selectedIndex)?.key) {
                // Rider
                "home" -> DashboardRoute(
                    onProfileClick = { onNavigate("profile_detail") },
                    onLogoutClick = onLogout,
                    onPickupLocationClick = {
                        navController.navigate("location_picker?lat=$lastPickupLat&lng=$lastPickupLng&mode=pickup")
                    },
                    pickupSelection = pickupSelection,
                    onPickupSelectionConsumed = {
                        savedStateHandle?.remove<String>("pickup_label")
                        savedStateHandle?.remove<Double>("pickup_lat")
                        savedStateHandle?.remove<Double>("pickup_lng")
                    },
                    onDestinationLocationClick = {
                        navController.navigate("location_picker?lat=$lastDropoffLat&lng=$lastDropoffLng&mode=destination")
                    },
                    destinationSelection = destinationSelection,
                    onDestinationSelectionConsumed = {
                        savedStateHandle?.remove<String>("dropoff_label")
                        savedStateHandle?.remove<Double>("dropoff_lat")
                        savedStateHandle?.remove<Double>("dropoff_lng")
                    },
                    onNavigateToActiveRide = { rideId ->
                        navController.navigate("active_ride/$rideId")
                    }
                )
                // Driver
                "driver_requests" -> DriverRoute()
                // Admin
                "admin" -> AdminRoute(
                    onUsersClick = { onNavigate("admin_users_detail") },
                    onVerificationsClick = { onNavigate("admin_verif_detail") }
                )
                "admin_users" -> AdminUsersRoute(onBackClick = {})
                "admin_verif" -> AdminVerificationsRoute(onBackClick = {})
                // Shared
                "messages" -> MessagesRoute(
                    onConversationClick = { id -> onNavigate("conversation/$id") }
                )
                "profile" -> ProfileRoute(
                    onBackClick = {},
                    onUpdateProfileClick = { onNavigate("update_profile") },
                    onSettingsClick = { onNavigate("settings") },
                    onRideHistoryClick = { onNavigate("ride_history") },
                    onLogoutClick = onLogout
                )
            }
        }
    }
}
