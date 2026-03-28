package com.example.uni_lift.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var saveActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Header Logo & Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_myplaces), // Placeholder
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Uni-Lift",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "[ AUTH REQUIRED ]",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Login Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TERMINAL LOGIN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Login", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onRegisterClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("Register", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("UNIVERSITY ID / EMAIL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("student@university.edu", color = Color.LightGray) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text("ACCESS CREDENTIALS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("RESET KEY?", fontSize = 10.sp, color = Color(0xFF2563EB))
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = saveActive, onCheckedChange = { saveActive = it })
                    Text("SAVE ACTIVE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Sign In", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ACCESS RESTRICTED TO VERIFIED STUDENTS ONLY",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "PROTECTED BY UNIVERSITY AUTHENTICATION PROTOCOL",
            fontSize = 10.sp,
            color = Color.Gray
        )
        Row(modifier = Modifier.padding(vertical = 16.dp)) {
            Text("TERMS  ", fontSize = 10.sp, color = Color.Gray)
            Text("PRIVACY  ", fontSize = 10.sp, color = Color.Gray)
            Text("SUPPORT", fontSize = 10.sp, color = Color.Gray)
        }
    }
}
