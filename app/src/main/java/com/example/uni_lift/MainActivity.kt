package com.example.uni_lift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uni_lift.ui.screens.*
import com.example.uni_lift.ui.theme.UniLiftTheme

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

@Composable
fun UniLiftApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("dashboard") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                dashboardData = null, // Placeholder for real data
                onProfileClick = { navController.navigate("profile") },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onFindDriverClick = { /* Implement ride logic */ }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onUpdateProfileClick = { navController.navigate("update_profile") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate("update_profile") },
                onChangePasswordClick = { navController.navigate("change_password") }
            )
        }
        composable("update_profile") {
            UpdateProfileScreen(
                onBackClick = { navController.popBackStack() },
                onUpdateSuccess = { navController.popBackStack() }
            )
        }
        composable("change_password") {
            ChangePasswordScreen(
                onBackClick = { navController.popBackStack() },
                onPasswordChanged = { navController.popBackStack() }
            )
        }
    }
}
