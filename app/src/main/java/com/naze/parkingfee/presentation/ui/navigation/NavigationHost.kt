package com.naze.parkingfee.presentation.ui.navigation

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naze.parkingfee.presentation.ui.components.BottomNavigationBar
import com.naze.parkingfee.presentation.ui.components.ExitConfirmDialog
import com.naze.parkingfee.presentation.ui.screens.home.HomeScreen
import com.naze.parkingfee.presentation.ui.screens.settings.SettingsScreen
import com.naze.parkingfee.presentation.ui.screens.settings.vehicles.list.VehicleListScreen
import com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add.AddVehicleScreen
import com.naze.parkingfee.presentation.ui.screens.parkinglots.add.AddParkingLotScreen
import com.naze.parkingfee.presentation.ui.screens.settings.parkinglots.list.ParkingLotListScreen
import com.naze.parkingfee.presentation.ui.screens.zonedetail.ZoneDetailScreen
import com.naze.parkingfee.presentation.ui.screens.vehicledetail.VehicleDetailScreen
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
    
    // 앱 종료 다이얼로그 상태
    var showExitDialog by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity
    
    // 메인 탭 목록 (백스택 관리를 위한)
    val mainTabs = listOf("home", "parkinglots/list", "vehicles/list", "history", "settings")
    
    // 뒤로 가기 처리 - 메인 탭에서만 종료 다이얼로그 표시
    BackHandler(enabled = currentRoute in mainTabs) {
        showExitDialog = true
    }
    
    // 앱 종료 확인 다이얼로그
    ExitConfirmDialog(
        visible = showExitDialog,
        onConfirm = {
            activity?.finish()
        },
        onDismiss = {
            showExitDialog = false
        }
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // 현재 탭과 같은 탭을 선택한 경우 아무것도 하지 않음
                    if (currentRoute == route || 
                        (currentRoute.startsWith("parkinglots/") && route == "parkinglots/list") ||
                        (currentRoute.startsWith("vehicles/") && route == "vehicles/list")) {
                        return@BottomNavigationBar
                    }
                    
                    // 탭 이동 시 백스택 완전히 제거
                    navController.navigate(route) {
                        // 현재 화면을 포함한 모든 백스택 제거
                        popUpTo(currentRoute) {
                            inclusive = true
                            saveState = false
                        }
                        // 같은 화면 중복 방지
                        launchSingleTop = true
                        // 상태 복원하지 않음
                        restoreState = false
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
                    },
                    onNavigateToDetailVehicle = { vehicleId ->
                        navController.navigate("vehicle_detail/$vehicleId")
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
            
            composable("vehicle_detail/{vehicleId}") { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
                VehicleDetailScreen(
                    vehicleId = vehicleId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = { editVehicleId ->
                        navController.navigate("vehicles/add?vehicleId=$editVehicleId")
                    }
                )
            }
        }
    }
}
