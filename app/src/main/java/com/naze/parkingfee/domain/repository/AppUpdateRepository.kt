package com.naze.parkingfee.domain.repository

/**
 * 앱 업데이트 체크를 위한 Repository
 * - Remote Config로부터 업데이트 정책(최소/최신 버전 등)을 가져오고
 * - 권장 업데이트 스킵 버전을 로컬에 저장/조회합니다.
 */
interface AppUpdateRepository {
    /**
     * Remote Config를 fetch/activate 한 뒤, 업데이트 설정을 반환합니다.
     *
     * 구현체는 네트워크/파싱 에러를 throw 할 수 있으며,
     * UseCase에서 fail-open(업데이트 다이얼로그 미노출)으로 처리합니다.
     */
    suspend fun fetchAndGetUpdateConfig(): AppUpdateConfig

    suspend fun getLastSkippedOptionalVersionCode(): Long

    suspend fun setLastSkippedOptionalVersionCode(versionCode: Long)
}

data class AppUpdateConfig(
    val forceUpdateMinVersionCode: Long,
    val optionalUpdateLatestVersionCode: Long,
    val forceUpdateMessage: String,
    val optionalUpdateMessage: String,
    val storeUrl: String?
)

