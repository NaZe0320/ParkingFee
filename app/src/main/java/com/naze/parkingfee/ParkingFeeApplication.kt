package com.naze.parkingfee

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.naze.parkingfee.domain.usecase.InitializeUserUseCase
import com.naze.parkingfee.infrastructure.notification.ParkingNotificationManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ParkingFeeApplication : Application() {
    
    companion object {
        private const val TAG = "ParkingFeeApplication"
    }
    
    @Inject
    lateinit var initializeUserUseCase: InitializeUserUseCase
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // Firebase 초기화
        FirebaseApp.initializeApp(this)
        
        // 알림 채널 초기화
        ParkingNotificationManager.createNotificationChannel(this)
        
        // 사용자 계정 초기화 (비동기)
        initializeUserAccount()
    }
    
    /**
     * 사용자 계정을 초기화합니다.
     * Block Store와 Firestore를 동기화하여 사용자 ID를 확인/생성합니다.
     */
    private fun initializeUserAccount() {
        applicationScope.launch {
            try {
                val result = initializeUserUseCase.execute()
                if (result.isSuccess) {
                    Log.d(TAG, "사용자 계정 초기화 완료")
                } else {
                    Log.e(TAG, "사용자 계정 초기화 실패: ${result.exceptionOrNull()?.message}")
                }
            } catch (exception: Exception) {
                Log.e(TAG, "사용자 계정 초기화 중 예외 발생: ${exception.message}", exception)
            }
        }
    }
}
