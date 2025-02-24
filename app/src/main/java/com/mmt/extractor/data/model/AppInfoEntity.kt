package com.mmt.extractor.data.model

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["pkgName"], unique = true)])
data class AppInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val pkgName: String,
    val apkSize: Float,
    val versionName: String?,
    val versionCode: Long,
    val minSDK: Int,
    val targetSDK: Int,
    val category: Int,
    val flag: Int,
    val installTime: Long,
    val updateTime: Long,
    val sourceDir: String,
    val installSource: String?,
){
    @Ignore
    var appIcon: Drawable? = null
}