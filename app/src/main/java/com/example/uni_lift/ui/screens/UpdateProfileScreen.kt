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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    onBackClick: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("student@university.edu") }
    var studentId by remember { mutableStateOf("2024-00000") }
    var contactNumber by remember { mutableStateOf("+63 912 345 6789") }
    var campusLocation by remember { mutableStateOf("CIT-U Main Campus") }

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
                    Text(text = "UPDATE PROFILE", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(text = "EDIT YOUR PERSONAL INFORMATION", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("FULL NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("STUDENT ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("CONTACT NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("CAMPUS LOCATION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = campusLocation,
                        onValueChange = { campusLocation = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onUpdateSuccess,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("SAVE CHANGES", color = Color.White)
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
        }
    }
}
