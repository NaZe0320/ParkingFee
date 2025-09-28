package com.naze.parkingfee.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.naze.parkingfee.presentation.ui.screens.home.HomeScreen
import com.naze.parkingfee.presentation.ui.screens.settings.SettingsScreen
import com.naze.parkingfee.presentation.ui.screens.addparkinglot.AddParkingLotScreen

/**
 * 앱의 네비게이션 호스트
 */
@Composable
fun NavigationHost(
    navController: NavHostController = rememberNavController()
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
                    // 히스토리 화면으로 이동 (추후 구현)
                },
                onNavigateToAddParkingLot = {
                    navController.navigate("add_parking_lot")
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("add_parking_lot") {
            AddParkingLotScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToOcr = {
                    // OCR 화면으로 이동 (추후 구현)
                }
            )
        }
    }
}
