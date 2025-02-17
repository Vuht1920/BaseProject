package com.mmt.app.utils.exception

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import com.mmt.app.data.repository.dataStore.PrefDataStore
import com.mmt.app.ui.MainActivity
import com.mmt.app.utils.Utils
import com.mmt.app.utils.log.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.util.Date
import javax.inject.Inject
import kotlin.system.exitProcess

class UnCaughtException @Inject constructor(@ApplicationContext private val context: Context) : Thread.UncaughtExceptionHandler {

    @Inject
    lateinit var prefDataStore: PrefDataStore

    private var path: String? = null
    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            val report = StringBuilder()
            val curDate = Date()
            report.append("Error Report collected on : ").append(curDate).append('\n').append('\n')
            report.append("Information :").append('\n')
            report.append(com.mmt.ads.utils.Utils.getDeviceId(context))
            report.append('\n').append('\n')
            report.append("Stack:\n")
            val result: Writer = StringWriter()
            val printWriter = PrintWriter(result)
            e.printStackTrace(printWriter)
            report.append(result)
            printWriter.close()
            report.append('\n')
            report.append("**** End of current Report ***")
            Log.e(javaClass.simpleName, ":\n$report")
            Process.killProcess(Process.myPid())
        } catch (throwable: Throwable) {
            Log.e("throwable", "Error while recordErrorLog: ", throwable)
        }
        try {
            restartApp()
        } catch (_: Throwable) {
        }
    }

    /*
     * */
    private fun restartApp() {
        if (!shouldAutoRestartApp()) {
            prefDataStore.setFlagAutoRestartApp(0)
            killApp()
            return
        }
        setFlagAutoRestartApp()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        val restartIntent = PendingIntent.getActivity(context, 113, intent, Utils.getPendingIntentFlag())
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager[AlarmManager.RTC, System.currentTimeMillis() + 1] = restartIntent
        killApp()
    }

    private fun killApp() {
        ActivityUtils.finishAllActivities()
        exitProcess(0)
    }

    init {
        try {
            val folder = File(context.cacheDir, "crash_log")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val logFile = File(folder, "log.txt")
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            path = logFile.path
        } catch (e: Exception) {
            DebugLog.loge(e)
        }
    }

    /*
     * Tự restart app tối đa 3 lần liên tục
     * */
    private fun shouldAutoRestartApp(): Boolean {
        return prefDataStore.autoRestartApp < 3
    }

    private fun setFlagAutoRestartApp() {
        val currentCount = prefDataStore.autoRestartApp
        prefDataStore.setFlagAutoRestartApp(currentCount + 1)
    }
}