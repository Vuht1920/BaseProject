package com.mmt.extractor.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mmt.extractor.data.model.AppInfoEntity
import com.mmt.extractor.data.room.converter.ImageConverter
import com.mmt.extractor.data.room.converter.UriConverter
import com.mmt.extractor.data.room.dao.AppInfoDao

@Database(entities = [AppInfoEntity::class], version = 1)
@TypeConverters(UriConverter::class, ImageConverter::class)
abstract class ExtractorDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
}