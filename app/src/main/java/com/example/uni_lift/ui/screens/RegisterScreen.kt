package com.example.uni_lift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
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

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "TERMINAL ACCESS", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(text = "AUTHORIZED PERSONNEL ONLY", fontSize = 10.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("Login", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("[ Register ]", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("FULL NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("John Doe") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("STUDENT ID NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2024-00000") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("UNIVERSITY EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("name@university.edu") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("ACCESS CREDENTIALS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("CONFIRM PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        Text("SECURITY NOTICE:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Verification required. Your student status will be verified before you can use the service.",
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRegisterSuccess,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Initiate Session →", color = Color.White)
                }
            }
        }
    }
}
