package com.naze.parkingfee.domain.repository

import com.naze.parkingfee.domain.model.User

/**
 * 사용자 계정 관련 리포지토리 인터페이스
 * Block Store와 Firestore를 통합하여 사용자 계정을 관리합니다.
 */
interface UserRepository {
    
    /**
     * 사용자 계정을 초기화합니다.
     * Block Store에서 ID를 확인하고, Firestore와 동기화합니다.
     * 
     * @return 초기화 결과 (성공/실패)
     */
    suspend fun initializeUser(): Result<Unit>
    
    /**
     * 현재 사용자 정보를 조회합니다.
     * 
     * @return 사용자 정보 (User?) - 없으면 null
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * 사용자 계정을 완전히 삭제합니다.
     * Block Store와 Firestore에서 모든 사용자 데이터를 제거합니다.
     * 
     * @return 삭제 결과 (성공/실패)
     */
    suspend fun deleteUser(): Result<Unit>
}
