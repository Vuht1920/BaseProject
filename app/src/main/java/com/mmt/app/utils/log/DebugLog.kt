package com.mmt.app.utils.log

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

object DebugLog {
    private const val TAG = "DebugLog"
    var DEBUG: Boolean = true

    fun logd(obj: Any?) {
        if (obj == null || !DEBUG) return
        val message = obj.toString()
        val fullClassName = Thread.currentThread().stackTrace[3].className
        var className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
        if (className.contains("$")) {
            className = className.substring(0, className.lastIndexOf("$"))
        }
        val methodName = Thread.currentThread().stackTrace[3].methodName
        val lineNumber = Thread.currentThread().stackTrace[3].lineNumber

        Log.d(TAG, "at ($className.java:$lineNumber) [$methodName]$message")
    }

    fun logn(obj: Any?) {
        if (obj == null || !DEBUG) return
        val message = obj.toString()
        val fullClassName = Thread.currentThread().stackTrace[3].className
        var className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
        if (className.contains("$")) {
            className = className.substring(0, className.lastIndexOf("$"))
        }
        val methodName = Thread.currentThread().stackTrace[3].methodName
        val lineNumber = Thread.currentThread().stackTrace[3].lineNumber

        Log.i(TAG, "at ($className.java:$lineNumber) [$methodName]$message")
    }

    fun loge(obj: Any?) {
        if (obj == null || !DEBUG) return
        val message = obj.toString()
        val fullClassName = Thread.currentThread().stackTrace[3].className
        var className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
        if (className.contains("$")) {
            className = className.substring(0, className.lastIndexOf("$"))
        }
        val methodName = Thread.currentThread().stackTrace[3].methodName
        val lineNumber = Thread.currentThread().stackTrace[3].lineNumber

        Log.e(TAG, "at ($className.java:$lineNumber) [$methodName]$message")
    }

    fun loge(e: Exception?) {
        if (e == null || !DEBUG) return
        val errors = StringWriter()
        e.printStackTrace(PrintWriter(errors))

        val message = errors.toString()

        val fullClassName = Thread.currentThread().stackTrace[3].className
        var className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
        if (className.contains("$")) {
            className = className.substring(0, className.lastIndexOf("$"))
        }
        val methodName = Thread.currentThread().stackTrace[3].methodName
        val lineNumber = Thread.currentThread().stackTrace[3].lineNumber

        Log.e(TAG, "at ($className.java:$lineNumber) [$methodName]$message")
    }

    fun logi(obj: Any?) {
        if (obj == null || !DEBUG) return
        val message = obj.toString()
        val fullClassName = Thread.currentThread().stackTrace[3].className
        var className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1)
        if (className.contains("$")) {
            className = className.substring(0, className.lastIndexOf("$"))
        }
        val methodName = Thread.currentThread().stackTrace[3].methodName
        val lineNumber = Thread.currentThread().stackTrace[3].lineNumber

        Log.i(TAG, "at ($className.java:$lineNumber) [$methodName]$message")
    }
}