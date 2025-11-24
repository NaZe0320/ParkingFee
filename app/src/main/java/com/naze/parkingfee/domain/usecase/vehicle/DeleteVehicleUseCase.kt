package com.naze.parkingfee.domain.usecase.vehicle

import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import com.naze.parkingfee.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 차량 삭제 결과
 */
sealed class DeleteVehicleResult {
    object Success : DeleteVehicleResult()
    object VehicleInUse : DeleteVehicleResult()
    data class Error(val message: String) : DeleteVehicleResult()
}

/**
 * 차량 삭제 UseCase
 * 활성 주차 세션에서 사용 중인 차량은 삭제할 수 없습니다.
 */
class DeleteVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val parkingRepository: ParkingRepository,
    private val selectedVehicleRepository: SelectedVehicleRepository
) {
    suspend fun execute(vehicleId: String): DeleteVehicleResult {
        return try {
            // 활성 주차 세션 확인
            val activeSession = parkingRepository.getActiveParkingSession()
            
            // 활성 주차 세션이 있고, 선택된 차량이 삭제하려는 차량인지 확인
            if (activeSession != null) {
                val selectedVehicleId = selectedVehicleRepository.selectedVehicleId.first()
                if (selectedVehicleId == vehicleId) {
                    return DeleteVehicleResult.VehicleInUse
                }
            }
            
            // 삭제 수행
            val result = vehicleRepository.deleteVehicle(vehicleId)
            if (result.isSuccess) {
                DeleteVehicleResult.Success
            } else {
                DeleteVehicleResult.Error("삭제에 실패했습니다.")
            }
        } catch (e: Exception) {
            DeleteVehicleResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }
}
