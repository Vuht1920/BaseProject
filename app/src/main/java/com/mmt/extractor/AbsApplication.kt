package com.mmt.extractor

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication

abstract class AbsApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks, LifecycleEventObserver {
    private val TAG = "AbsApplication"
    private var mCurrentActivity: Pair<String, Int>? = null // className, hashCode
    private var mLastStopActivity: Pair<String, Int>? = null  // className, hashCode

    private var isLaunchToForeground = false
    private var isMoveToBackground = false

    private val mHandler = Handler(Looper.getMainLooper())

    fun isAppInBackground(): Boolean {
        return isMoveToBackground
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    protected fun destroy() {
        unregisterActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        isMoveToBackground = false
        mHandler.removeCallbacks(runnableCheckEndSession)
        // Save last time use app timestamp
        Log.d(TAG, "\nonActivityCreated: " + activity::class.java.simpleName + "\nhash: " + activity.hashCode())
    }

    override fun onActivityStarted(activity: Activity) {
        mCurrentActivity = Pair(activity::class.java.simpleName, activity.hashCode())
//        Log.d(TAG,"\nonActivityStarted: " + activity::class.java.simpleName + "\nhash: " + activity.hashCode())
    }

    override fun onActivityResumed(activity: Activity) {
        isLaunchToForeground = true
        isMoveToBackground = false
        mCurrentActivity = Pair(activity::class.java.simpleName, activity.hashCode())
//        Log.d(TAG,"\nonActivityResumed: " + activity::class.java.simpleName + "\nhash: " + activity.hashCode())
    }

    override fun onActivityPaused(activity: Activity) {
        isLaunchToForeground = false
    }

    override fun onActivityStopped(activity: Activity) {
        mLastStopActivity = Pair(activity::class.java.simpleName, activity.hashCode())
        isLaunchToForeground = false
//        Log.d(TAG,"\nonActivityStopped: " + activity::class.java.simpleName + "\nhash: " + activity.hashCode())
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (mCurrentActivity?.first == activity::class.java.simpleName && activity.hashCode() == mCurrentActivity?.second) {
            mCurrentActivity = null
        }
        if (mLastStopActivity?.first == activity::class.java.simpleName && activity.hashCode() == mLastStopActivity?.second) {
            mLastStopActivity = null
        }
        Log.d(TAG, "\nonActivityDestroyed: " + activity::class.java.simpleName + "\nhash: " + activity.hashCode())
    }

    private val runnableCheckEndSession = Runnable {
        if (mCurrentActivity == null && mLastStopActivity == null) {
            Log.e(TAG, "END_SESSION!")
            isMoveToBackground = true
            isLaunchToForeground = false
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_STOP) {
            isMoveToBackground = true
            isLaunchToForeground = false
        }
    }
}