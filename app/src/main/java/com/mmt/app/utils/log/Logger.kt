package com.mmt.app.utils.log

import android.util.Log
import com.mmt.BuildConfig

object Logger {
    private const val TAG = "ThemeLogger"
    @JvmStatic
    val debug = BuildConfig.DEBUG

    @JvmStatic
    var showLog = false

    @JvmStatic
    fun d(any: Any?) {
        if (debug || showLog) {
            Log.d(TAG, "$any --- " + Thread.currentThread().stackTrace[3].let { el -> el.methodName + "(" + el.fileName + ":" + el.lineNumber + ")" })
        }
    }

    @JvmStatic
    fun d() {
        if (debug || showLog) {
            Log.d(TAG, Thread.currentThread().stackTrace[3].let { el -> el.methodName + "(" + el.fileName + ":" + el.lineNumber + ")" })
        }
    }

    @JvmStatic
    fun i(any: Any?) {
        if (debug || showLog) {
            Log.i(TAG, "$any --- " + Thread.currentThread().stackTrace[3].let { el -> el.methodName + "(" + el.fileName + ":" + el.lineNumber + ")" })
        }
    }

    @JvmStatic
    fun dRange(any: Any?) {
        val traceLength = 7
        if (debug || showLog) {
            Log.d(TAG, "$any --- ${Thread.currentThread().stackTrace.let { it.copyOfRange(3, minOf(traceLength, it.size - 1)) }.map { el -> el.methodName + "(" + el.fileName + ":" + el.lineNumber + ")" }}")
        }
    }
    @JvmStatic
    fun dRange(any: Any?, traceLength: Int) {
        if (debug || showLog) {
            Log.d(TAG, "$any --- ${Thread.currentThread().stackTrace.let { it.copyOfRange(3, minOf(traceLength, it.size - 1)) }.map { el -> el.methodName + "(" + el.fileName + ":" + el.lineNumber + ")" }}")
        }
    }

    @JvmStatic
    fun e(message: Any) {
        if (debug || showLog) {
            if (message is Throwable) {
                Log.e(TAG, "${message.message} --- ${Thread.currentThread().stackTrace[3].let { el -> el.methodName + "(" + el.fileName + ":" + el.lineNumber + ")" }}", message)
            } else
                Log.e(TAG, "$message --- ${Thread.currentThread().stackTrace[3].let { el -> el.methodName + "(" + el.fileName + ":" + el.lineNumber + ")" }}")
        }
    }

    @JvmStatic
    fun e(message: String = "", error: Throwable) {
        if (debug || showLog) {
            Log.e(TAG, "$message --- ${Thread.currentThread().stackTrace[3].let { el -> el.methodName + "(" + el.fileName + ":" + el.lineNumber + ")" }}", error)
        }
    }
}