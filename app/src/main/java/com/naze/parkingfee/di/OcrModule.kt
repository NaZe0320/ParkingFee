package com.naze.parkingfee.di

import android.content.Context
import com.naze.parkingfee.data.datasource.local.ocr.OcrProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * OCR 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object OcrModule {

    /**
     * OcrProcessor 제공
     */
    @Provides
    @Singleton
    fun provideOcrProcessor(
        @ApplicationContext context: Context
    ): OcrProcessor {
        return OcrProcessor(context)
    }
}

