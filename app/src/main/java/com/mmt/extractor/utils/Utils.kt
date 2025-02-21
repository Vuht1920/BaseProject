package com.mmt.extractor.utils

import android.app.PendingIntent
import android.os.Build

object Utils {
    fun getPendingIntentFlag(defaultFlag: Int = PendingIntent.FLAG_UPDATE_CURRENT): Int {
        var flag = defaultFlag
        if (Build.VERSION.SDK_INT >= 31) {
            flag = flag or PendingIntent.FLAG_MUTABLE
        }
        return flag
    }
}