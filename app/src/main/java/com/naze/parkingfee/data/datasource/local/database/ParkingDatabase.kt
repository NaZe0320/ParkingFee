package com.naze.parkingfee.data.datasource.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.naze.parkingfee.data.datasource.local.dao.ParkingDao
import com.naze.parkingfee.data.datasource.local.dao.ParkingHistoryDao
import com.naze.parkingfee.data.datasource.local.dao.VehicleDao
import com.naze.parkingfee.data.datasource.local.entity.ParkingZoneEntity
import com.naze.parkingfee.data.datasource.local.entity.ParkingSessionEntity
import com.naze.parkingfee.data.datasource.local.entity.ParkingHistoryEntity
import com.naze.parkingfee.data.datasource.local.entity.VehicleEntity

/**
 * 주차 관련 Room 데이터베이스
 */
@Database(
    entities = [
        ParkingZoneEntity::class,
        ParkingSessionEntity::class,
        ParkingHistoryEntity::class,
        VehicleEntity::class
    ],
    version = 1, // 초기화: 마이그레이션 없음
    exportSchema = false
)
abstract class ParkingDatabase : RoomDatabase() {
    
    abstract fun parkingDao(): ParkingDao
    abstract fun parkingHistoryDao(): ParkingHistoryDao
    abstract fun vehicleDao(): VehicleDao
    
    companion object {
        @Volatile
        private var INSTANCE: ParkingDatabase? = null
        
        fun getDatabase(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "parking_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
