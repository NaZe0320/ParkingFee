package com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naze.parkingfee.infrastructure.notification.ToastManager
import com.naze.parkingfee.presentation.ui.screens.settings.vehicles.add.components.*

/**
 * 차량 등록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToOcr: () -> Unit = {},
    vehicleId: String? = null // 편집 모드를 위한 차량 ID
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    
    // 편집 모드 초기화
    LaunchedEffect(vehicleId) {
        if (vehicleId != null) {
            viewModel.processIntent(AddVehicleContract.AddVehicleIntent.LoadVehicleForEdit(vehicleId))
        } else {
            viewModel.processIntent(AddVehicleContract.AddVehicleIntent.Initialize)
        }
    }
    
    // Effect 처리
    LaunchedEffect(effect) {
        effect?.let { currentEffect ->
            when (currentEffect) {
                is AddVehicleContract.AddVehicleEffect.ShowToast -> {
                    ToastManager.show(context, currentEffect.message)
                }
                is AddVehicleContract.AddVehicleEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is AddVehicleContract.AddVehicleEffect.OpenOcrScreen -> {
                    onNavigateToOcr()
                }
                is AddVehicleContract.AddVehicleEffect.NavigateTo -> {
                    // 네비게이션 처리
                }
                is AddVehicleContract.AddVehicleEffect.ShowDialog -> {
                    // Dialog 표시 로직
                }
                is AddVehicleContract.AddVehicleEffect.ShowValidationError -> {
                    // 유효성 검사 에러 표시 로직
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("차량 등록") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // // OCR 진입 버튼
        // item {
        //     OcrEntryButton(
        //         onOcrClick = {
        //             viewModel.processIntent(AddVehicleContract.AddVehicleIntent.OpenOcrScreen)
        //         }
        //     )
        // }
        
        // 차량 이름 입력
        item {
            VehicleNameInputCard(
                vehicleName = state.vehicleName,
                onNameChange = { name ->
                    viewModel.processIntent(AddVehicleContract.AddVehicleIntent.UpdateVehicleName(name))
                },
                nameError = state.validationErrors["vehicleName"]
            )
        }
        
        // 번호판 입력
        item {
            PlateInputCard(
                plateNumber = state.plateNumber,
                onPlateNumberChange = { plateNumber ->
                    viewModel.processIntent(AddVehicleContract.AddVehicleIntent.UpdatePlateNumber(plateNumber))
                },
                plateNumberError = state.validationErrors["plateNumber"]
            )
        }
        
        // 할인 자격 설정
        item {
            DiscountEligibilityCard(
                compactCarEnabled = state.compactCarDiscount,
                nationalMeritEnabled = state.nationalMeritDiscount,
                disabledEnabled = state.disabledDiscount,
                onCompactCarChange = { enabled ->
                    viewModel.processIntent(AddVehicleContract.AddVehicleIntent.ToggleCompactCarDiscount(enabled))
                },
                onNationalMeritChange = { enabled ->
                    viewModel.processIntent(AddVehicleContract.AddVehicleIntent.ToggleNationalMeritDiscount(enabled))
                },
                onDisabledChange = { enabled ->
                    viewModel.processIntent(AddVehicleContract.AddVehicleIntent.ToggleDisabledDiscount(enabled))
                }
            )
        }
        
        // 저장 버튼
        item {
            SaveVehicleButton(
                isSaving = state.isSaving,
                onSaveClick = {
                    viewModel.processIntent(AddVehicleContract.AddVehicleIntent.SaveVehicle)
                }
            )
        }
        
        // 에러 메시지 표시
        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        }
    }
}
