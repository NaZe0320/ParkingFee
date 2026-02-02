package com.naze.parkingfee.domain.usecase.appupdate

import com.naze.parkingfee.domain.repository.AppUpdateRepository

/**
 * 앱 런치 시 1회 업데이트 정책을 확인합니다.
 *
 * - 강제 업데이트: currentVersionCode < minVersionCode
 * - 권장 업데이트: 강제 대상이 아니고 currentVersionCode < latestVersionCode
 *   - 단, 사용자가 동일 latestVersionCode에 대해 "나중에"를 눌렀으면 다시 띄우지 않음
 *
 * Remote Config fetch 실패 등 예외 상황에서는 fail-open(업데이트 다이얼로그 미노출)합니다.
 */
class CheckAppUpdateOnLaunchUseCase(
    private val appUpdateRepository: AppUpdateRepository
) {
    suspend fun execute(currentVersionCode: Long): AppUpdateCheckResult {
        return try {
            val config = appUpdateRepository.fetchAndGetUpdateConfig()

            val min = config.forceUpdateMinVersionCode
            if (min > 0 && currentVersionCode < min) {
                return AppUpdateCheckResult.ForceUpdate(
                    message = config.forceUpdateMessage,
                    storeUrl = config.storeUrl
                )
            }

            val latest = config.optionalUpdateLatestVersionCode
            if (latest > 0 && currentVersionCode < latest) {
                val lastSkipped = appUpdateRepository.getLastSkippedOptionalVersionCode()
                if (lastSkipped >= latest) {
                    return AppUpdateCheckResult.NoUpdate
                }

                return AppUpdateCheckResult.OptionalUpdate(
                    message = config.optionalUpdateMessage,
                    latestVersionCode = latest,
                    storeUrl = config.storeUrl
                )
            }

            AppUpdateCheckResult.NoUpdate
        } catch (_: Exception) {
            AppUpdateCheckResult.NoUpdate
        }
    }
}

sealed class AppUpdateCheckResult {
    data class ForceUpdate(
        val message: String,
        val storeUrl: String?
    ) : AppUpdateCheckResult()

    data class OptionalUpdate(
        val message: String,
        val latestVersionCode: Long,
        val storeUrl: String?
    ) : AppUpdateCheckResult()

    data object NoUpdate : AppUpdateCheckResult()
}

