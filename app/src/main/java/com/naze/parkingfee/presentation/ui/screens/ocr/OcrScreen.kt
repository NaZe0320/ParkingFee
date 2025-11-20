package com.naze.parkingfee.presentation.ui.screens.ocr

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.naze.parkingfee.infrastructure.notification.ToastManager
import java.io.File

/**
 * OCR 화면
 * 이미지를 선택/촬영하고 텍스트를 인식합니다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OcrScreen(
    viewModel: OcrViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 권한 상태
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    // 카메라 촬영을 위한 임시 파일 URI
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리에서 이미지 선택
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processIntent(OcrContract.OcrIntent.ImageSelected(it))
        }
    }

    // 카메라로 이미지 촬영
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let {
                viewModel.processIntent(OcrContract.OcrIntent.ImageSelected(it))
            }
        }
    }

    // Effect 처리
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OcrContract.OcrEffect.ShowToast -> {
                    ToastManager.show(context, effect.message)
                }
                is OcrContract.OcrEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is OcrContract.OcrEffect.OpenGallery -> {
                    if (permissionsState.allPermissionsGranted) {
                        galleryLauncher.launch("image/*")
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }
                is OcrContract.OcrEffect.OpenCamera -> {
                    if (permissionsState.allPermissionsGranted) {
                        // 임시 파일 생성
                        val imageFile = File(context.cacheDir, "ocr_temp_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            imageFile
                        )
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }
                is OcrContract.OcrEffect.NavigateToAddParkingLotWithResult -> {
                    // 나중에 구현: AddParkingLotScreen으로 데이터 전달
                    ToastManager.show(context, "이 기능은 추후 구현 예정입니다.")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OCR 텍스트 인식") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    if (state.hasResult) {
                        IconButton(
                            onClick = {
                                viewModel.processIntent(OcrContract.OcrIntent.Reset)
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "초기화")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 안내 문구
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "주차장 표지판 사진을 선택하거나 촬영하면\n텍스트를 자동으로 인식합니다.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // 이미지 선택/촬영 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.processIntent(OcrContract.OcrIntent.SelectImageFromGallery)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("갤러리")
                }

                Button(
                    onClick = {
                        viewModel.processIntent(OcrContract.OcrIntent.CaptureImageFromCamera)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("카메라")
                }
            }

            // 선택된 이미지 미리보기
            if (state.hasImage && state.selectedImageUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Text(
                            text = "선택된 이미지",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Image(
                            painter = rememberAsyncImagePainter(state.selectedImageUri),
                            contentDescription = "선택된 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // OCR 실행 버튼
                Button(
                    onClick = {
                        viewModel.processIntent(OcrContract.OcrIntent.ProcessOcr)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isProcessing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("텍스트 인식 중...")
                    } else {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("텍스트 인식 실행")
                    }
                }
            }

            // OCR 결과 표시
            if (state.hasResult && state.recognizedText != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "인식 결과",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        // 전체 텍스트
                        Text(
                            text = "전체 텍스트:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = state.recognizedText ?: "",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 파싱된 정보 (있을 경우)
                        if (state.parsedParkingLotName != null || state.parsedFeeInfo != null) {
                            HorizontalDivider()
                            
                            Text(
                                text = "자동 파싱 결과:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            state.parsedParkingLotName?.let { name ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "주차장 이름:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            state.parsedFeeInfo?.let { info ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "요금 정보:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = info,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 결과 사용 버튼 (추후 구현)
                // Button(
                //     onClick = {
                //         viewModel.processIntent(OcrContract.OcrIntent.UseOcrResult)
                //     },
                //     modifier = Modifier.fillMaxWidth(),
                //     shape = RoundedCornerShape(12.dp),
                //     colors = ButtonDefaults.buttonColors(
                //         containerColor = MaterialTheme.colorScheme.primary
                //     )
                // ) {
                //     Text("이 결과로 주차장 등록하기")
                // }
            }

            // 에러 메시지
            state.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

