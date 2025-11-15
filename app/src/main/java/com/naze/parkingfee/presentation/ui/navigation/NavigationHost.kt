package com.naze.parkingfee.presentation.ui.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naze.parkingfee.presentation.ui.components.BottomNavigationBar
import com.naze.parkingfee.presentation.ui.screens.home.HomeScreen
import com.naze.parkingfee.presentation.ui.screens.settings.SettingsScreen
import com.naze.parkingfee.presentation.ui.screens.settings.vehicles.list.VehicleListScreen
import com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add.AddVehicleScreen
import com.naze.parkingfee.presentation.ui.screens.parkinglots.add.AddParkingLotScreen
import com.naze.parkingfee.presentation.ui.screens.settings.parkinglots.list.ParkingLotListScreen
import com.naze.parkingfee.presentation.ui.screens.zonedetail.ZoneDetailScreen
import com.naze.parkingfee.presentation.ui.screens.history.HistoryScreen

/**
 * 앱의 네비게이션 호스트
 * 하단 네비게이션 바와 함께 사용됩니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationHost(
    navController: NavHostController = rememberNavController(),
    onStartParkingService: () -> Unit = {},
    onStopParkingService: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                        "parkinglots/list" -> navController.navigate("parkinglots/list")
                        "vehicles/list" -> navController.navigate("vehicles/list")
                        "history" -> navController.navigate("history")
                        "settings" -> navController.navigate("settings")
                    }
                }
            )
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .padding(bottom = paddingValues.calculateBottomPadding())

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
                        navController.navigate("parkinglots/add")
                    },
                    onNavigateToZoneDetail = { zoneId ->
                        navController.navigate("zone_detail/$zoneId")
                    },
                    onNavigateToEditZone = { zoneId ->
                        navController.navigate("parkinglots/add?zoneId=$zoneId")
                    },
                    onNavigateToEditVehicle = { vehicleId ->
                        navController.navigate("vehicles/add?vehicleId=$vehicleId")
                    },
                    onStartParkingService = onStartParkingService,
                    onStopParkingService = onStopParkingService
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToVehicleManagement = {
                        navController.navigate("vehicles/list")
                    },
                    onNavigateToParkingLotManagement = {
                        navController.navigate("parkinglots/list")
                    }
                )
            }
            
            // 주차장 관리 라우트들
            composable("parkinglots/list") {
                ParkingLotListScreen(
                    onNavigateToAddParkingLot = {
                        navController.navigate("parkinglots/add")
                    },
                    onNavigateToEditParkingLot = { zoneId ->
                        navController.navigate("parkinglots/add?zoneId=$zoneId")
                    },
                    onNavigateToDetailParkingLot = { zoneId ->
                        navController.navigate("zone_detail/$zoneId")
                    }
                )
            }
            
            composable(route = "parkinglots/add?zoneId={zoneId}",  // 이 부분 추가
                arguments = listOf(
                    navArgument("zoneId") { nullable = true }  // 이 부분 추가
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
                        navController.navigate("parkinglots/add?zoneId=$editZoneId")
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
            
            // 차량 관리 라우트들
            composable("vehicles/list") {
                VehicleListScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToAddVehicle = {
                        navController.navigate("vehicles/add")
                    },
                    onNavigateToEditVehicle = { vehicleId ->
                        navController.navigate("vehicles/add?vehicleId=$vehicleId")
                    }
                )
            }
            
            composable(
                route = "vehicles/add?vehicleId={vehicleId}",
                arguments = listOf(
                    navArgument("vehicleId") { nullable = true }
                )
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("vehicleId")
                AddVehicleScreen(
                    vehicleId = vehicleId,
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
}
