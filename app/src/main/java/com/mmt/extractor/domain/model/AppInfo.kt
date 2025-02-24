package com.mmt.extractor.domain.model

import android.graphics.drawable.Drawable

data class AppInfo(
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
    val appIcon: Drawable? = null
)