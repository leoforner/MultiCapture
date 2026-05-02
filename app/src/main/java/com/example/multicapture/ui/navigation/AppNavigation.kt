package com.example.multicapture.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.multicapture.ui.screens.CaptureScreen
import com.example.multicapture.ui.screens.SettingsScreen
import com.example.multicapture.ui.screens.FormatInfoScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "capture") {
        composable("capture") {
            CaptureScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToFormatInfo = { navController.navigate("format_info") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFormatInfo = { navController.navigate("format_info") }
            )
        }
        composable("format_info") {
            FormatInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
