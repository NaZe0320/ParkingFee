package com.naze.parkingfee.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.naze.parkingfee.data.datasource.cloud.firestore.FirestoreUserDataSource
import com.naze.parkingfee.data.datasource.local.blockstore.BlockStoreDataSource
import com.naze.parkingfee.data.repository.UserRepositoryImpl
import com.naze.parkingfee.domain.repository.UserRepository
import com.naze.parkingfee.domain.usecase.DeleteUserUseCase
import com.naze.parkingfee.domain.usecase.InitializeUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 사용자 계정 관련 의존성 주입 모듈
 * Block Store와 Firestore를 통합한 사용자 계정 관리 기능을 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideBlockStoreDataSource(
        @ApplicationContext context: Context
    ): BlockStoreDataSource {
        return BlockStoreDataSource(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        blockStoreDataSource: BlockStoreDataSource,
        firestoreUserDataSource: FirestoreUserDataSource
    ): UserRepository {
        return UserRepositoryImpl(blockStoreDataSource, firestoreUserDataSource)
    }

    @Provides
    fun provideInitializeUserUseCase(
        userRepository: UserRepository
    ): InitializeUserUseCase {
        return InitializeUserUseCase(userRepository)
    }

    @Provides
    fun provideDeleteUserUseCase(
        userRepository: UserRepository
    ): DeleteUserUseCase {
        return DeleteUserUseCase(userRepository)
    }
}
