package com.mmt.extractor.domain.mapper

import com.mmt.extractor.data.model.AppInfoEntity
import com.mmt.extractor.domain.model.AppInfo

fun AppInfoEntity.toDomainModel(): AppInfo {
    return AppInfo(
        this.id,
        this.appName,
        this.pkgName,
        this.apkSize,
        this.versionName,
        this.versionCode,
        this.minSDK,
        this.targetSDK,
        this.category,
        this.flag,
        this.installTime,
        this.updateTime,
        this.sourceDir,
        this.installSource,
        this.appIcon
    )
}

fun AppInfo.toDomainModel(): AppInfoEntity {
    return AppInfoEntity(
        this.id,
        this.appName,
        this.pkgName,
        this.apkSize,
        this.versionName,
        this.versionCode,
        this.minSDK,
        this.targetSDK,
        this.category,
        this.flag,
        this.installTime,
        this.updateTime,
        this.sourceDir,
        this.installSource
    )
}