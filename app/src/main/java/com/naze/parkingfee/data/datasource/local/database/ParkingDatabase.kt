package com.naze.parkingfee.data.datasource.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.naze.parkingfee.data.datasource.local.dao.ParkingDao
import com.naze.parkingfee.data.datasource.local.entity.ParkingZoneEntity
import com.naze.parkingfee.data.datasource.local.entity.ParkingSessionEntity

/**
 * 주차 관련 Room 데이터베이스
 */
@Database(
    entities = [
        ParkingZoneEntity::class,
        ParkingSessionEntity::class
    ],
    version = 2, // 버전 업데이트 (ParkingLotEntity 제거)
    exportSchema = false
)
abstract class ParkingDatabase : RoomDatabase() {
    
    abstract fun parkingDao(): ParkingDao
    
    companion object {
        @Volatile
        private var INSTANCE: ParkingDatabase? = null
        
        fun getDatabase(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "parking_database"
                ).fallbackToDestructiveMigration() // 스키마 변경으로 인한 마이그레이션
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
