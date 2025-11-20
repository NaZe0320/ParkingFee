package com.naze.parkingfee.di

import com.naze.parkingfee.domain.repository.ParkingRepository
import com.naze.parkingfee.domain.repository.ParkingHistoryRepository
import com.naze.parkingfee.domain.repository.SelectedVehicleRepository
import com.naze.parkingfee.domain.repository.VehicleRepository
import com.naze.parkingfee.domain.repository.AlarmRepository
import com.naze.parkingfee.domain.usecase.parkingzone.AddParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.DeleteParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.GetParkingZonesUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.GetParkingZoneByIdUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.ToggleParkingZoneFavoriteUseCase
import com.naze.parkingfee.domain.usecase.parkingzone.UpdateParkingZoneUseCase
import com.naze.parkingfee.domain.usecase.parkingsession.GetActiveParkingSessionUseCase
import com.naze.parkingfee.domain.usecase.parkingsession.StartParkingUseCase
import com.naze.parkingfee.domain.usecase.parkingsession.StopParkingUseCase
import com.naze.parkingfee.domain.usecase.parkinghistory.SaveParkingHistoryUseCase
import com.naze.parkingfee.domain.usecase.parkinghistory.GetParkingHistoryUseCase
import com.naze.parkingfee.domain.usecase.parkinghistory.DeleteParkingHistoryUseCase
import com.naze.parkingfee.domain.usecase.parkingsession.UpdateFreeTimeUseCase
import com.naze.parkingfee.domain.usecase.alarm.AddParkingAlarmUseCase
import com.naze.parkingfee.domain.usecase.alarm.DeleteParkingAlarmUseCase
import com.naze.parkingfee.domain.usecase.alarm.GetParkingAlarmsUseCase
import com.naze.parkingfee.domain.usecase.alarm.DeleteAlarmsForSessionUseCase
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
    fun provideAddParkingZoneUseCase(parkingRepository: ParkingRepository): AddParkingZoneUseCase {
        return AddParkingZoneUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideToggleParkingZoneFavoriteUseCase(parkingRepository: ParkingRepository): ToggleParkingZoneFavoriteUseCase {
        return ToggleParkingZoneFavoriteUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteParkingZoneUseCase(parkingRepository: ParkingRepository): DeleteParkingZoneUseCase {
        return DeleteParkingZoneUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideGetActiveParkingSessionUseCase(parkingRepository: ParkingRepository): GetActiveParkingSessionUseCase {
        return GetActiveParkingSessionUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideGetParkingZonesUseCase(parkingRepository: ParkingRepository): GetParkingZonesUseCase {
        return GetParkingZonesUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideGetParkingZoneByIdUseCase(parkingRepository: ParkingRepository): GetParkingZoneByIdUseCase {
        return GetParkingZoneByIdUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateParkingZoneUseCase(parkingRepository: ParkingRepository): UpdateParkingZoneUseCase {
        return UpdateParkingZoneUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideStartParkingUseCase(parkingRepository: ParkingRepository): StartParkingUseCase {
        return StartParkingUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideStopParkingUseCase(
        parkingRepository: ParkingRepository,
        parkingHistoryRepository: ParkingHistoryRepository,
        selectedVehicleRepository: SelectedVehicleRepository,
        vehicleRepository: VehicleRepository
    ): StopParkingUseCase {
        return StopParkingUseCase(parkingRepository, parkingHistoryRepository, selectedVehicleRepository, vehicleRepository)
    }

    @Provides
    @Singleton
    fun provideSaveParkingHistoryUseCase(historyRepository: ParkingHistoryRepository): SaveParkingHistoryUseCase {
        return SaveParkingHistoryUseCase(historyRepository)
    }

    @Provides
    @Singleton
    fun provideGetParkingHistoryUseCase(historyRepository: ParkingHistoryRepository): GetParkingHistoryUseCase {
        return GetParkingHistoryUseCase(historyRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteParkingHistoryUseCase(historyRepository: ParkingHistoryRepository): DeleteParkingHistoryUseCase {
        return DeleteParkingHistoryUseCase(historyRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateFreeTimeUseCase(parkingRepository: ParkingRepository): UpdateFreeTimeUseCase {
        return UpdateFreeTimeUseCase(parkingRepository)
    }

    @Provides
    @Singleton
    fun provideAddParkingAlarmUseCase(alarmRepository: AlarmRepository): AddParkingAlarmUseCase {
        return AddParkingAlarmUseCase(alarmRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteParkingAlarmUseCase(alarmRepository: AlarmRepository): DeleteParkingAlarmUseCase {
        return DeleteParkingAlarmUseCase(alarmRepository)
    }

    @Provides
    @Singleton
    fun provideGetParkingAlarmsUseCase(alarmRepository: AlarmRepository): GetParkingAlarmsUseCase {
        return GetParkingAlarmsUseCase(alarmRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteAlarmsForSessionUseCase(alarmRepository: AlarmRepository): DeleteAlarmsForSessionUseCase {
        return DeleteAlarmsForSessionUseCase(alarmRepository)
    }
}
