package com.example.consentircanitas.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.consentircanitas.ui.screens.AttendanceScreen
import com.example.consentircanitas.ui.screens.HomeScreen
import com.example.consentircanitas.ui.screens.ScanScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(navController)
        }

        composable("scan") {
            ScanScreen(navController)
        }

        composable("attendance") {
            AttendanceScreen(navController)
        }
    }
}