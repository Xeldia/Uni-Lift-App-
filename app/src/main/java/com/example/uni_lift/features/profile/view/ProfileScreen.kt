package com.example.uni_lift.features.profile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileRoute(
    onBackClick: () -> Unit,
    onUpdateProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRideHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val presenter = remember { ProfilePresenter(ProfileRepository(context)) }
    var uiState by remember { mutableStateOf(ProfileContract.UiState()) }

    val view = remember(onBackClick, onUpdateProfileClick, onSettingsClick, onRideHistoryClick) {
        object : ProfileContract.View {
            override fun render(state: ProfileContract.UiState) { uiState = state }
            override fun navigateBack() { onBackClick() }
            override fun navigateToUpdateProfile() { onUpdateProfileClick() }
            override fun navigateToSettings() { onSettingsClick() }
            override fun navigateToRideHistory() { onRideHistoryClick() }
        }
    }

    DisposableEffect(presenter, view) {
        presenter.attach(view)
        onDispose { presenter.detach() }
    }

    ProfileScreen(
        state = uiState,
        onBackClick = presenter::onBackClicked,
        onUpdateProfileClick = presenter::onUpdateProfileClicked,
        onSettingsClick = presenter::onSettingsClicked,
        onRideHistoryClick = presenter::onRideHistoryClicked,
        onLogoutClick = onLogoutClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileContract.UiState,
    onBackClick: () -> Unit,
    onUpdateProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRideHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val profile = state.profile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("USER PROFILE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    if (onBackClick != {}) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Black)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile.initials.ifEmpty { "??" },
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(profile.fullName, fontWeight = FontWeight.Black, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
            Text(profile.role, fontSize = 12.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)

            Spacer(modifier = Modifier.height(4.dp))

            // Account status badge
            val (badgeColor, badgeText) = when (profile.accountStatus) {
                "ACTIVE" -> Color(0xFF10B981) to "VERIFIED ACTIVE"
                "SUSPENDED" -> Color(0xFFEF4444) to "SUSPENDED"
                else -> Color(0xFFF59E0B) to "PENDING VERIFICATION"
            }
            Surface(shape = RoundedCornerShape(4.dp), color = badgeColor) {
                Text(
                    badgeText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row (if has data)
            if (profile.rating != null || profile.ridesCompleted != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    profile.rating?.let { StatChip("RATING", "%.1f ★".format(it)) }
                    profile.ridesCompleted?.let { StatChip("RIDES", "$it") }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACCOUNT INFO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileField("EMAIL", profile.email)
                    ProfileField("STUDENT ID", profile.studentId.ifEmpty { "—" })
                    ProfileField("CONTACT", profile.contactNumber.ifEmpty { "—" })
                    ProfileField("CAMPUS", profile.campusLocation.ifEmpty { "—" })
                }
            }

            // Vehicle card (drivers only)
            if (profile.role == "DRIVER" || profile.vehicle != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("VEHICLE INFO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileField("VEHICLE", profile.vehicle ?: "Not registered")
                        ProfileField("TYPE", profile.vehicleType ?: "—")
                        // Driver verification status
                        profile.driverVerificationStatus?.let { dvs ->
                            val (dvColor, dvLabel) = when (dvs) {
                                "APPROVED" -> Color(0xFF10B981) to "APPROVED"
                                "REJECTED" -> Color(0xFFEF4444) to "REJECTED"
                                "REVOKED" -> Color(0xFFEF4444) to "REVOKED"
                                else -> Color(0xFFF59E0B) to "PENDING"
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("DRIVER STATUS", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = dvColor) {
                                    Text(dvLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Button(
                onClick = onUpdateProfileClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("UPDATE PROFILE", fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onRideHistoryClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RIDE HISTORY", color = Color.Black, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ACCOUNT SETTINGS", color = Color.Black, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SIGN OUT", color = Color.Red, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.error, color = Color.Red, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
        Text(label, fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = FontFamily.Monospace)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp, modifier = Modifier.padding(top = 4.dp))
    }
}
