package com.naze.parkingfee.di

import android.content.Context
import com.naze.parkingfee.data.datasource.local.dao.ParkingDao
import com.naze.parkingfee.data.datasource.local.database.ParkingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 데이터베이스 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideParkingDatabase(@ApplicationContext context: Context): ParkingDatabase {
        return ParkingDatabase.getDatabase(context)
    }

    @Provides
    fun provideParkingDao(database: ParkingDatabase): ParkingDao {
        return database.parkingDao()
    }
}
