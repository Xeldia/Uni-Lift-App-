package com.example.uni_lift.features.auth.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterRoute(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    presenter: RegisterContract.Presenter = remember { RegisterPresenter(RegisterRepository()) }
) {
    var uiState by remember { mutableStateOf(RegisterContract.UiState()) }

    val view = remember(onRegisterSuccess, onBackToLogin) {
        object : RegisterContract.View {
            override fun render(state: RegisterContract.UiState) {
                uiState = state
            }

            override fun navigateAfterRegister() {
                onRegisterSuccess()
            }

            override fun navigateBackToLogin() {
                onBackToLogin()
            }
        }
    }

    DisposableEffect(presenter, view) {
        presenter.attach(view)
        onDispose { presenter.detach() }
    }

    RegisterScreen(
        state = uiState,
        onFullNameChanged = presenter::onFullNameChanged,
        onStudentIdChanged = presenter::onStudentIdChanged,
        onEmailChanged = presenter::onEmailChanged,
        onPasswordChanged = presenter::onPasswordChanged,
        onConfirmPasswordChanged = presenter::onConfirmPasswordChanged,
        onRegisterClick = presenter::onRegisterClicked,
        onBackToLogin = presenter::onBackToLoginClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: RegisterContract.UiState,
    onFullNameChanged: (String) -> Unit,
    onStudentIdChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

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
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("Login", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {},
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
                    value = state.fullName,
                    onValueChange = onFullNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("John Doe") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("STUDENT ID NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.studentId,
                    onValueChange = onStudentIdChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2024-00000") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("UNIVERSITY EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("name@university.edu") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("ACCESS CREDENTIALS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("CONFIRM PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChanged,
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

                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = onRegisterClick,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Initiate Session →", color = Color.White)
                    }
                }
            }
        }
    }
}