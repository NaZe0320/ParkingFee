package com.naze.parkingfee.data.repository

import com.naze.parkingfee.data.datasource.local.datastore.AppUpdateDataStore
import com.naze.parkingfee.data.datasource.remote.remoteconfig.AppUpdateRemoteConfigDataSource
import com.naze.parkingfee.domain.repository.AppUpdateConfig
import com.naze.parkingfee.domain.repository.AppUpdateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepositoryImpl @Inject constructor(
    private val remoteConfigDataSource: AppUpdateRemoteConfigDataSource,
    private val appUpdateDataStore: AppUpdateDataStore
) : AppUpdateRepository {
    override suspend fun fetchAndGetUpdateConfig(): AppUpdateConfig {
        return remoteConfigDataSource.fetchAndGet()
    }

    override suspend fun getLastSkippedOptionalVersionCode(): Long {
        return appUpdateDataStore.getLastSkippedOptionalVersionCode()
    }

    override suspend fun setLastSkippedOptionalVersionCode(versionCode: Long) {
        appUpdateDataStore.setLastSkippedOptionalVersionCode(versionCode)
    }
}

