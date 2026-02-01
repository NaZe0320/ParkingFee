package com.naze.parkingfee.data.datasource.remote.storage

/**
 * (스켈레톤) ParkingZone 이미지 Storage 데이터 소스
 *
 * 지금은 실제 Storage 연동 전 단계로, 인터페이스만 제공한다.
 */
interface ParkingZoneImageStorageDataSource {

    /**
     * 주차 구역 이미지 업로드
     * @return 업로드된 이미지의 다운로드 URL(예정)
     */
    suspend fun uploadParkingZoneImage(
        zoneId: String,
        imageBytes: ByteArray,
        contentType: String = "image/jpeg"
    ): Result<String>

    /**
     * 주차 구역 이미지 삭제
     */
    suspend fun deleteParkingZoneImage(zoneId: String): Result<Unit>
}

