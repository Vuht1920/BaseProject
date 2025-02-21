package com.mmt.extractor.data.room

import android.content.Context
import androidx.room.Room
import com.mmt.extractor.data.room.dao.AppInfoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideApkDao(apkDatabase: ExtractorDatabase): AppInfoDao {
        return apkDatabase.appInfoDao()
    }

    @Provides
    @Singleton
    fun provideRoomDataBase(@ApplicationContext context: Context): ExtractorDatabase {
        return Room.databaseBuilder(context, ExtractorDatabase::class.java, "extractor_db").build()
    }
}