package com.mmt.ads.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale


object Utils {
    fun readTextFileInAsset(context: Context, placeFileName: String?): String {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(context.assets.open(placeFileName!!), "UTF-8"))
            val returnString = StringBuilder()
            var mLine: String?
            while ((reader.readLine().also { mLine = it }) != null) {
                returnString.append(mLine)
            }
            return returnString.toString().trim { it <= ' ' }
        } catch (e: IOException) {
            AdDebugLog.loge(e)
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    AdDebugLog.loge(e)
                }
            }
        }
        return ""
    }

    fun getScreenWidth(context: Context): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = (context as AppCompatActivity).windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics().apply { display.getMetrics(this) }
            val widthPixels = outMetrics.widthPixels.toFloat()
            val density = outMetrics.density
            return (widthPixels / density).toInt()
        }
    }

    fun getDeviceId(context: Context): String {
        try {
            @SuppressLint("HardwareIds") val androidID = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val digest = MessageDigest.getInstance("MD5")
            digest.update(androidID.toByteArray())
            val messageDigest = digest.digest()
            // Create Hex String
            val hexString = java.lang.StringBuilder()
            for (i in messageDigest.indices) {
                val aMessageDigest = messageDigest[i]
                val h = java.lang.StringBuilder(Integer.toHexString(0xFF and aMessageDigest.toInt()))
                while (h.length < 2) h.insert(0, "0")
                hexString.append(h)
            }
            return hexString.toString().uppercase(Locale.getDefault())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}