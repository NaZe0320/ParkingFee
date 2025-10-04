package com.naze.parkingfee.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naze.parkingfee.presentation.ui.screens.home.HomeScreen
import com.naze.parkingfee.presentation.ui.screens.settings.SettingsScreen
import com.naze.parkingfee.presentation.ui.screens.addparkinglot.AddParkingLotScreen
import com.naze.parkingfee.presentation.ui.screens.zonedetail.ZoneDetailScreen
import com.naze.parkingfee.presentation.ui.screens.history.HistoryScreen

/**
 * 앱의 네비게이션 호스트
 */
@Composable
fun NavigationHost(
    navController: NavHostController = rememberNavController(),
    onStartParkingService: () -> Unit = {},
    onStopParkingService: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToAddParkingLot = {
                    navController.navigate("add_parking_lot")
                },
                onNavigateToZoneDetail = { zoneId ->
                    navController.navigate("zone_detail/$zoneId")
                },
                onNavigateToEditZone = { zoneId ->
                    navController.navigate("add_parking_lot?zoneId=$zoneId")
                },
                onStartParkingService = onStartParkingService,
                onStopParkingService = onStopParkingService
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "add_parking_lot?zoneId={zoneId}",
            arguments = listOf(
                navArgument("zoneId") { nullable = true }
            )
        ) { backStackEntry ->
            val zoneId = backStackEntry.arguments?.getString("zoneId")
            AddParkingLotScreen(
                zoneId = zoneId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToOcr = {
                    // OCR 화면으로 이동 (추후 구현)
                }
            )
        }
        
        composable("zone_detail/{zoneId}") { backStackEntry ->
            val zoneId = backStackEntry.arguments?.getString("zoneId") ?: ""
            ZoneDetailScreen(
                zoneId = zoneId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { editZoneId ->
                    navController.navigate("add_parking_lot?zoneId=$editZoneId")
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
        
        composable("history") {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
