package com.naze.parkingfee.domain.usecase

import com.naze.parkingfee.domain.model.ParkingLot
import com.naze.parkingfee.domain.repository.ParkingLotRepository
import javax.inject.Inject

/**
 * 주차장 추가 UseCase
 */
class AddParkingLotUseCase @Inject constructor(
    private val parkingLotRepository: ParkingLotRepository
) {
    suspend operator fun invoke(parkingLot: ParkingLot): ParkingLot {
        return parkingLotRepository.addParkingLot(parkingLot)
    }
    
    suspend fun execute(parkingLot: ParkingLot): ParkingLot = invoke(parkingLot)
}

/**
 * 주차장 목록 조회 UseCase
 */
class GetParkingLotsUseCase @Inject constructor(
    private val parkingLotRepository: ParkingLotRepository
) {
    suspend operator fun invoke(): List<ParkingLot> {
        return parkingLotRepository.getParkingLots()
    }
    
    suspend fun execute(): List<ParkingLot> = invoke()
}
