package com.example.uni_lift.features.auth.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginRoute(
    onLoginSuccess: (role: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    val presenter = remember { LoginPresenter(LoginRepository(context)) }
    var uiState by remember { mutableStateOf(LoginContract.UiState()) }

    val view = remember(onLoginSuccess, onRegisterClick) {
        object : LoginContract.View {
            override fun render(state: LoginContract.UiState) { uiState = state }
            override fun navigateToDashboard(role: String) { onLoginSuccess(role) }
            override fun navigateToRegister() { onRegisterClick() }
        }
    }

    DisposableEffect(presenter, view) {
        presenter.attach(view)
        onDispose { presenter.detach() }
    }

    LoginScreen(
        state = uiState,
        onEmailChanged = presenter::onEmailChanged,
        onPasswordChanged = presenter::onPasswordChanged,
        onSaveActiveChanged = presenter::onSaveActiveChanged,
        onLoginClick = presenter::onLoginClicked,
        onRegisterClick = presenter::onRegisterClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginContract.UiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSaveActiveChanged: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

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
            Text(text = "[ AUTH REQUIRED ]", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "TERMINAL LOGIN", fontSize = 20.sp, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(4.dp)
                    ) { Text("Login", color = Color.White) }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onRegisterClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) { Text("Register", color = Color.Black) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("UNIVERSITY ID / EMAIL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("student@university.edu", color = Color.LightGray) },
                    singleLine = true,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text("ACCESS CREDENTIALS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("RESET KEY?", fontSize = 10.sp, color = Color(0xFF2563EB))
                }
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.saveActive, onCheckedChange = onSaveActiveChanged, enabled = !state.isLoading)
                    Text("SAVE ACTIVE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.error, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(4.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Sign In", color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ACCESS RESTRICTED TO VERIFIED STUDENTS ONLY",
                    fontSize = 10.sp, color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(text = "PROTECTED BY UNIVERSITY AUTHENTICATION PROTOCOL", fontSize = 10.sp, color = Color.Gray)
        Row(modifier = Modifier.padding(vertical = 16.dp)) {
            Text("TERMS  ", fontSize = 10.sp, color = Color.Gray)
            Text("PRIVACY  ", fontSize = 10.sp, color = Color.Gray)
            Text("SUPPORT", fontSize = 10.sp, color = Color.Gray)
        }
    }
}
