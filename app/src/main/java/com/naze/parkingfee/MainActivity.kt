package com.naze.parkingfee

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.naze.parkingfee.infrastructure.service.ParkingService
import com.naze.parkingfee.presentation.ui.navigation.NavigationHost
import com.naze.parkingfee.ui.theme.ParkingFeeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 권한 결과 처리
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 13+ 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Android 12+ (API 31+) 정확한 알람 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // 정확한 알람을 스케줄할 수 없으면 설정 화면으로 안내
                // 실제 앱에서는 사용자에게 다이얼로그를 표시하고 설정으로 이동할지 물어보는 것이 좋습니다
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    // 인텐트 처리 실패 시 무시
                }
            }
        }
        
        setContent {
            ParkingFeeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationHost(
                        onStartParkingService = { startParkingService() },
                        onStopParkingService = { stopParkingService() }
                    )
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        // 앱 복귀 시 활성 세션과 알림 동기화
        syncNotificationWithActiveSession()
    }
    
    private fun startParkingService() {
        val intent = Intent(this, ParkingService::class.java).apply {
            action = ParkingService.ACTION_START_PARKING
        }
        ContextCompat.startForegroundService(this, intent)
    }
    
    private fun stopParkingService() {
        val intent = Intent(this, ParkingService::class.java).apply {
            action = ParkingService.ACTION_STOP_PARKING
        }
        startService(intent)
    }
    
    private fun syncNotificationWithActiveSession() {
        val intent = Intent(this, ParkingService::class.java).apply {
            action = ParkingService.ACTION_SYNC_NOTIFICATION
        }
        startService(intent)
    }
}