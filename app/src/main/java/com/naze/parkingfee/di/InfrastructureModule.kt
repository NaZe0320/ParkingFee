package com.naze.parkingfee.di

import android.content.Context
import com.naze.parkingfee.infrastructure.alarm.AlarmScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 인프라스트럭처 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object InfrastructureModule {
    
    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context): AlarmScheduler {
        return AlarmScheduler(context)
    }
}

