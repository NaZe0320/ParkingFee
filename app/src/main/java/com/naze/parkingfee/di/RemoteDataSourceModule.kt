package com.naze.parkingfee.di

import com.naze.parkingfee.data.datasource.remote.firestore.ParkingHistoryFirestoreDataSource
import com.naze.parkingfee.data.datasource.remote.firestore.ParkingHistoryRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Remote DataSource 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindParkingHistoryRemoteDataSource(
        impl: ParkingHistoryFirestoreDataSource
    ): ParkingHistoryRemoteDataSource
}

