package com.naze.parkingfee.di

import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import com.naze.parkingfee.data.repository.ParkingRepositoryImpl
import com.naze.parkingfee.data.repository.ParkingHistoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindParkingRepository(
        parkingRepositoryImpl: ParkingRepositoryImpl
    ): ParkingRepository
    
    @Binds
    @Singleton
    abstract fun bindParkingHistoryRepository(
        parkingHistoryRepositoryImpl: ParkingHistoryRepositoryImpl
    ): ParkingHistoryRepository
}
