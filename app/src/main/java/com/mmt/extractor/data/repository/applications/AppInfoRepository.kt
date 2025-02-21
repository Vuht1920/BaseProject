package com.mmt.extractor.data.repository.applications

import android.Manifest.permission.HIDE_OVERLAY_WINDOWS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.mmt.extractor.data.model.AppInfoEntity
import com.mmt.extractor.data.room.dao.AppInfoDao
import com.mmt.extractor.utils.FileUtil
import com.mmt.extractor.utils.log.DebugLog
import java.io.File
import javax.inject.Inject

class AppInfoRepository @Inject constructor(val context: Context, private val appInfoDao: AppInfoDao) {

    suspend fun queryAllAppInDevice() {
        val packageManager = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val set = HashSet<String>()
            val names = HashSet<String>()
            packageManager.getPackagesHoldingPermissions(arrayOf(HIDE_OVERLAY_WINDOWS), PackageManager.MATCH_DEFAULT_ONLY).forEach {
                packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).apply {
                    `package` = it.packageName
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }, 0)?.let { _ ->
                    set.add(it.packageName)
                }
            }
        }
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val appsInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        val entities = HashSet<AppInfoEntity>()
        val pkgList = hashSetOf<String>()

        appsInfo.forEach { info ->
            try {
                val pkgName = info.activityInfo.packageName ?: ""
                if (pkgName.isNotEmpty()) {
                    if (!pkgList.contains(pkgName)) {
                        packageManager.packageManagerToEntity(pkgName)?.let {
                            pkgList.add(it.pkgName)
                            entities.add(it)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        DebugLog.loge("dlaldsadl: ${entities.size}")
        appInfoDao.insertAppInfos(entities.toList())
    }

    private fun PackageManager.packageManagerToEntity(packageName: String): AppInfoEntity? {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) this.getPackageInfo(
            packageName, PackageManager.PackageInfoFlags.of(0L)
        )
        else this.getPackageInfo(packageName, 0)
        val applicationInfo = packageInfo.applicationInfo
        if (packageInfo == null || applicationInfo == null) return null
        return AppInfoEntity(
            appName = this.getApplicationLabel(applicationInfo).toString(),
            pkgName = packageName,
            sourceDir = applicationInfo.sourceDir,
            versionName = packageInfo.versionName,
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong(),
            minSDK = applicationInfo.minSdkVersion,
            targetSDK = applicationInfo.targetSdkVersion,
            flag = applicationInfo.flags,
            category = applicationInfo.category,
            installTime = packageInfo.firstInstallTime,
            updateTime = packageInfo.lastUpdateTime,
            installSource = applicationInfo.sourceDir,
            apkSize = FileUtil.getBytesSizeInMB(listOf(applicationInfo.sourceDir, *(applicationInfo.splitSourceDirs ?: emptyArray())).sumOf { File(it).length() }),
        )
    }
}