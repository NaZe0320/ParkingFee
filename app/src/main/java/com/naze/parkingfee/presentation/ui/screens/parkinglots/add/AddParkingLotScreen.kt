package com.naze.parkingfee.presentation.ui.screens.parkinglots.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naze.parkingfee.infrastructure.notification.ToastManager
import com.naze.parkingfee.presentation.ui.screens.parkinglots.add.components.*

/**
 * 주차장 추가 화면
 * MVI 패턴에 따라 State를 구독하고 UI를 렌더링합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParkingLotScreen(
    viewModel: AddParkingLotViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToOcr: () -> Unit = {},
    zoneId: String? = null // 편집 모드를 위한 구역 ID
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 편집 모드 초기화
    LaunchedEffect(zoneId) {
        if (zoneId != null) {
            viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.LoadZoneForEdit(zoneId))
        } else {
            viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.Initialize)
        }
    }

    // Effect 처리 - SharedFlow를 직접 collect하여 모든 Effect를 순차적으로 처리
    LaunchedEffect(Unit) {
        viewModel.effect.collect { currentEffect ->
            when (currentEffect) {
                is AddParkingLotContract.AddParkingLotEffect.ShowToast -> {
                    ToastManager.show(context, currentEffect.message)
                }
                is AddParkingLotContract.AddParkingLotEffect.NavigateTo -> {
                    when (currentEffect.route) {
                        "ocr" -> onNavigateToOcr()
                    }
                }
                is AddParkingLotContract.AddParkingLotEffect.ShowDialog -> {
                    // Dialog 표시 로직
                }
                is AddParkingLotContract.AddParkingLotEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is AddParkingLotContract.AddParkingLotEffect.OpenOcrScreen -> {
                    onNavigateToOcr()
                }
                is AddParkingLotContract.AddParkingLotEffect.ShowValidationError -> {
                    // 유효성 검사 에러 표시 로직
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (zoneId != null) "주차장 수정" else "주차장 추가") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.SaveParkingLot)
                        },
                        enabled = !state.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "저장", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("저장", style = MaterialTheme.typography.bodyMedium)
                        }
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
        // OCR 진입 버튼
        // item {
        //     OcrEntryButton(
        //         onOcrClick = {
        //             viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.OpenOcrScreen)
        //         }
        //     )
        // }

        // 주차장 기본 정보
        item {
            ParkingLotBasicInfoCard(
                parkingLotName = state.parkingLotName,
                onNameChange = { name ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.UpdateParkingLotName(name))
                },
                nameError = state.validationErrors["parkingLotName"]
            )
        }

        // 주차장 타입 선택
        item {
            ParkingLotTypeCard(
                isPublic = state.isPublic,
                onIsPublicChange = { isPublic ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.ToggleIsPublic(isPublic))
                }
            )
        }

        // 기본 요금 체계
        item {
            BasicFeeRuleCard(
                durationMinutes = state.basicFeeDuration,
                feeAmount = state.basicFeeAmount,
                onDurationChange = { minutes ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.UpdateBasicFeeDuration(minutes))
                },
                onFeeChange = { amount ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.UpdateBasicFeeAmount(amount))
                },
                durationError = state.validationErrors["basicFeeDuration"],
                feeError = state.validationErrors["basicFeeAmount"]
            )
        }

        // 추가 요금 체계
        item {
            AdditionalFeeRuleCard(
                intervalMinutes = state.additionalFeeInterval,
                feeAmount = state.additionalFeeAmount,
                onIntervalChange = { minutes ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.UpdateAdditionalFeeInterval(minutes))
                },
                onFeeChange = { amount ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.UpdateAdditionalFeeAmount(amount))
                },
                intervalError = state.validationErrors["additionalFeeInterval"],
                feeError = state.validationErrors["additionalFeeAmount"]
            )
        }

        // 일 최대 요금 체계
        item {
            DailyMaxFeeRuleCard(
                enabled = state.dailyMaxFeeEnabled,
                maxFeeAmount = state.dailyMaxFeeAmount,
                onEnabledChange = { enabled ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.ToggleDailyMaxFee(enabled))
                },
                onFeeChange = { amount ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.UpdateDailyMaxFeeAmount(amount))
                },
                feeError = state.validationErrors["dailyMaxFeeAmount"]
            )
        }

        // 커스텀 요금 구간
        item {
            CustomFeeRulesCard(
                customFeeRules = state.customFeeRules,
                onAddRule = {
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.AddCustomFeeRule)
                },
                onRemoveRule = { index ->
                    viewModel.processIntent(AddParkingLotContract.AddParkingLotIntent.RemoveCustomFeeRule(index))
                },
                onUpdateRule = { index, minMinutes, maxMinutes, fee ->
                    viewModel.processIntent(
                        AddParkingLotContract.AddParkingLotIntent.UpdateCustomFeeRule(
                            index = index,
                            minMinutes = minMinutes,
                            maxMinutes = maxMinutes,
                            fee = fee
                        )
                    )
                },
                validationErrors = state.validationErrors
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
