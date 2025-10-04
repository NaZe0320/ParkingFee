package com.naze.parkingfee.di

import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import com.naze.parkingfee.domain.repository.VehicleRepository
import com.naze.parkingfee.domain.usecase.AddParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.DeleteParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.GetActiveParkingSessionUseCase
import com.naze.parkingfee.domain.usecase.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.GetParkingZoneByIdUseCase
import com.naze.parkingfee.domain.usecase.UpdateParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.StartParkingUseCase
import com.naze.parkingfee.domain.usecase.StopParkingUseCase
import com.naze.parkingfee.domain.usecase.SaveParkingHistoryUseCase
import com.naze.parkingfee.domain.usecase.GetParkingHistoriesUseCase
import com.naze.parkingfee.domain.usecase.DeleteParkingHistoryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * UseCase 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideAddParkingZoneUseCase(repository: ParkingRepository): AddParkingZoneUseCase {
        return AddParkingZoneUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteParkingZoneUseCase(repository: ParkingRepository): DeleteParkingZoneUseCase {
        return DeleteParkingZoneUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetActiveParkingSessionUseCase(repository: ParkingRepository): GetActiveParkingSessionUseCase {
        return GetActiveParkingSessionUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetParkingZonesUseCase(repository: ParkingRepository): GetParkingZonesUseCase {
        return GetParkingZonesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetParkingZoneByIdUseCase(repository: ParkingRepository): GetParkingZoneByIdUseCase {
        return GetParkingZoneByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateParkingZoneUseCase(repository: ParkingRepository): UpdateParkingZoneUseCase {
        return UpdateParkingZoneUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideStartParkingUseCase(repository: ParkingRepository): StartParkingUseCase {
        return StartParkingUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideStopParkingUseCase(
        repository: ParkingRepository,
        parkingHistoryRepository: ParkingHistoryRepository,
        selectedVehicleRepository: SelectedVehicleRepository,
        vehicleRepository: VehicleRepository
    ): StopParkingUseCase {
        return StopParkingUseCase(repository, parkingHistoryRepository, selectedVehicleRepository, vehicleRepository)
    }

    @Provides
    @Singleton
    fun provideSaveParkingHistoryUseCase(repository: ParkingHistoryRepository): SaveParkingHistoryUseCase {
        return SaveParkingHistoryUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetParkingHistoriesUseCase(repository: ParkingHistoryRepository): GetParkingHistoriesUseCase {
        return GetParkingHistoriesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteParkingHistoryUseCase(repository: ParkingHistoryRepository): DeleteParkingHistoryUseCase {
        return DeleteParkingHistoryUseCase(repository)
    }
}
