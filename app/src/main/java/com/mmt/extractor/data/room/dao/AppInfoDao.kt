package com.mmt.extractor.data.room.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.mmt.extractor.data.model.AppInfoEntity

@Dao
interface AppInfoDao {
    @Upsert
    suspend fun insertAppInfo(appInfoEntity: AppInfoEntity)

    @Upsert
    suspend fun insertAppInfos(appInfoEntities: List<AppInfoEntity>)
}