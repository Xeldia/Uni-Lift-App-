package com.example.uni_lift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onUpdateProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(" Uni-Lift", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "USER PROFILE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "VERIFIED STUDENT ACCESS",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Image Placeholder
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("JD", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = onUpdateProfileClick,
                            modifier = Modifier
                                .size(28.dp)
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color(0xFF2563EB), CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    ProfileField("FULL NAME", "John Doe")
                    ProfileField("STUDENT ID", "2024-00000")
                    ProfileField("EMAIL ADDRESS", "student@university.edu")
                    ProfileField("CONTACT NUMBER", "+63 912 345 6789")
                    ProfileField("CAMPUS LOCATION", "CIT-U Main Campus")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFECFDF5), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ACCOUNT STATUS: VERIFIED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF064E3B))
                            Text("Your student credentials have been verified by the university", fontSize = 10.sp, color = Color(0xFF065F46))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onUpdateProfileClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UPDATE PROFILE")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ACCOUNT SETTINGS", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6))
                            .padding(12.dp)
                    ) {
                        Text("SECURITY NOTICE:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Never share your credentials with anyone. Report suspicious activity immediately.", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
