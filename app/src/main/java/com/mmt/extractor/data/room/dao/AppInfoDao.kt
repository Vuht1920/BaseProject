package com.mmt.extractor.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mmt.extractor.data.model.AppInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Upsert
    suspend fun insertAppInfo(appInfoEntity: AppInfoEntity)

    @Upsert
    suspend fun insertAppInfos(appInfoEntities: List<AppInfoEntity>)

    @Query("select * from AppInfoEntity")
    fun getAllAppInfo(): Flow<List<AppInfoEntity>>
}