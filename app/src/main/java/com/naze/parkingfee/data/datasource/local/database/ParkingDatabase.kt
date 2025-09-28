package com.naze.parkingfee.data.datasource.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.naze.parkingfee.data.datasource.local.dao.ParkingDao
import com.naze.parkingfee.data.datasource.local.dao.ParkingLotDao
import com.naze.parkingfee.data.datasource.local.entity.ParkingZoneEntity
import com.naze.parkingfee.data.datasource.local.entity.ParkingSessionEntity
import com.naze.parkingfee.data.datasource.local.entity.ParkingLotEntity

/**
 * 주차 관련 Room 데이터베이스
 */
@Database(
    entities = [
        ParkingZoneEntity::class,
        ParkingSessionEntity::class,
        ParkingLotEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ParkingDatabase : RoomDatabase() {
    
    abstract fun parkingDao(): ParkingDao
    abstract fun parkingLotDao(): ParkingLotDao
    
    companion object {
        @Volatile
        private var INSTANCE: ParkingDatabase? = null
        
        fun getDatabase(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "parking_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
