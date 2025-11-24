package com.naze.parkingfee.di

import com.naze.parkingfee.data.repository.VehicleRepositoryImpl
import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import com.naze.parkingfee.domain.repository.VehicleRepository
import com.naze.parkingfee.domain.usecase.vehicle.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 차량 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object VehicleModule {
    
    @Provides
    @Singleton
    fun provideVehicleRepository(
        vehicleDao: com.naze.parkingfee.data.datasource.local.dao.VehicleDao
    ): VehicleRepository {
        return VehicleRepositoryImpl(vehicleDao)
    }
    
    @Provides
    fun provideGetVehiclesUseCase(vehicleRepository: VehicleRepository): GetVehiclesUseCase {
        return GetVehiclesUseCase(vehicleRepository)
    }
    
    @Provides
    fun provideAddVehicleUseCase(vehicleRepository: VehicleRepository): AddVehicleUseCase {
        return AddVehicleUseCase(vehicleRepository)
    }
    
    @Provides
    fun provideUpdateVehicleUseCase(vehicleRepository: VehicleRepository): UpdateVehicleUseCase {
        return UpdateVehicleUseCase(vehicleRepository)
    }
    
    @Provides
    fun provideDeleteVehicleUseCase(
        vehicleRepository: VehicleRepository,
        parkingRepository: ParkingRepository,
        selectedVehicleRepository: SelectedVehicleRepository
    ): DeleteVehicleUseCase {
        return DeleteVehicleUseCase(vehicleRepository, parkingRepository, selectedVehicleRepository)
    }
}
