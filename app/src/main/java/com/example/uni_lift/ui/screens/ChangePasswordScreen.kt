package com.example.uni_lift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(id = android.R.drawable.ic_lock_idle_lock), contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "CHANGE PASSWORD", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    Text(text = "UPDATE YOUR ACCOUNT CREDENTIALS", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("CURRENT PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("NEW PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("CONFIRM NEW PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("PASSWORD REQUIREMENTS:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("• Minimum 8 characters\n• Upper and lowercase letters\n• At least one number\n• Special character (!@#$%^&*)", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(id = android.R.drawable.ic_dialog_alert), contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("SECURITY WARNING:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                Text("Changing your password will log you out of all devices. You'll need to log in again.", fontSize = 10.sp, color = Color(0xFFB91C1C))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(painterResource(id = android.R.drawable.ic_lock_idle_lock), contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UPDATE PASSWORD")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("CANCEL", color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "● ENCRYPTION: AES-256 SSL: SECURE",
                fontSize = 10.sp,
                color = Color(0xFF10B981),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "All password changes are encrypted and logged for security",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
