package com.naze.parkingfee.data.repository

import com.naze.parkingfee.domain.model.ParkingLot
import com.naze.parkingfee.domain.repository.ParkingLotRepository
import com.naze.parkingfee.data.datasource.local.dao.ParkingLotDao
import com.naze.parkingfee.data.mapper.ParkingLotMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 주차장 Repository 구현체
 */
class ParkingLotRepositoryImpl @Inject constructor(
    private val parkingLotDao: ParkingLotDao,
    private val parkingLotMapper: ParkingLotMapper
) : ParkingLotRepository {

    override suspend fun addParkingLot(parkingLot: ParkingLot): ParkingLot {
        val entity = parkingLotMapper.mapToEntity(parkingLot)
        parkingLotDao.insertParkingLot(entity)
        return parkingLot
    }

    override suspend fun getParkingLots(): List<ParkingLot> {
        return parkingLotDao.getAllParkingLots().map { entity ->
            parkingLotMapper.mapToDomain(entity)
        }
    }

    override fun observeParkingLots(): Flow<List<ParkingLot>> {
        return parkingLotDao.observeParkingLots().map { entities ->
            entities.map { entity ->
                parkingLotMapper.mapToDomain(entity)
            }
        }
    }

    override suspend fun getParkingLot(lotId: String): ParkingLot? {
        return parkingLotDao.getParkingLot(lotId)?.let { entity ->
            parkingLotMapper.mapToDomain(entity)
        }
    }

    override suspend fun updateParkingLot(parkingLot: ParkingLot): ParkingLot {
        val entity = parkingLotMapper.mapToEntity(parkingLot)
        parkingLotDao.updateParkingLot(entity)
        return parkingLot
    }

    override suspend fun deleteParkingLot(lotId: String): Boolean {
        return try {
            parkingLotDao.deleteParkingLotById(lotId)
            true
        } catch (e: Exception) {
            false
        }
    }
}
