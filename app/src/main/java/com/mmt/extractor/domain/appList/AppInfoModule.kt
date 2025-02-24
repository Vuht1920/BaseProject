package com.mmt.extractor.domain.appList

import android.content.Context
import com.mmt.extractor.data.repository.applications.AppInfoRepository
import com.mmt.extractor.data.room.dao.AppInfoDao
import com.mmt.extractor.domain.useCase.GetAppListInDeviceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppInfoModule {

    @Provides
    @Singleton
    fun provideAppInfoRepository(@ApplicationContext context: Context, appInfoDao: AppInfoDao): AppInfoRepository {
        return AppInfoRepository(context, appInfoDao)
    }

    @Provides
    @Singleton
    fun provideGetAppListUseCase(appInfoRepository: AppInfoRepository): GetAppListInDeviceUseCase {
        return GetAppListInDeviceUseCase(appInfoRepository)
    }
}