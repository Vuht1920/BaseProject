package com.mmt

import android.annotation.SuppressLint
import com.blankj.utilcode.util.LogUtils
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.wapper.AdWrapperListener
import com.mmt.ads.wapper.NativeAdViewWrapper
import com.tohsoft.ads.AdsModule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Stack

/**
* Helper class is used to load NativeAd sequentially, avoiding loading multiple NativeAds at the same time
* */
object NativeAdCenterLoader {
    private const val TAG = "[NativeAdCenterLoader] "
    private val mNativeAdQueue: LinkedHashMap<Int, NativeAdViewWrapper?> = linkedMapOf()
    private val mQueue: Stack<Int> = Stack()

    @Volatile
    private var isLoading = false
    @Volatile
    private var isDestroying = false

    @JvmStatic
    fun add(nativeAd: NativeAdViewWrapper?) {
        add(nativeAd, false)
    }

    @JvmStatic
    fun add(nativeAd: NativeAdViewWrapper?, isPriority: Boolean) {
        if (isDestroying) return

        nativeAd?.let {
            val key = nativeAd.hashCode()
            if (isPriority && mQueue.contains(key) && mNativeAdQueue.containsKey(key)) {
                // When you want to show NativeAd immediately but it is exist in the queue -> delete it from queue, so you can add it to the top of the queue.
                mQueue.remove(key)
                mNativeAdQueue.remove(key)
            }

            if (!mNativeAdQueue.containsKey(key)) {
                if (it.isAdAvailable() || it.isLoading) {
                    AdDebugLog.logw("$TAG it.isAdAvailable() || it.isLoading -> call showAds immediate")
                    nativeAd.showAds(AdsModule.getInstance().context, nativeAd.getContainer(), null)
                    return
                }
                mNativeAdQueue[key] = nativeAd

                if (isPriority) {
                    // if this NativeAd need to init immediate -> add to top of stack
                    AdDebugLog.logw("$TAG add ${nativeAd.getLayoutType()} to top queue $key")
                    mQueue.add(key)
                } else {
                    // Add to bottom of stack
                    AdDebugLog.logw("$TAG add ${nativeAd.getLayoutType()} to queue $key")
                    mQueue.add(0, key)
                }
            } else {
                AdDebugLog.logw("mNativeAdQueue.containsKey $key")
            }
            checkQueueAndPreload()
        }
    }

    private fun checkQueueAndPreload() {
        if (mQueue.isNotEmpty() && !isLoading && !isDestroying) {
            val key = mQueue.pop() // Get last item - top of this stack (the last item of the Vector object).
            val nativeAd = mNativeAdQueue[key]

            nativeAd?.let {
                isLoading = true
//                AdDebugLog.logw("$TAG checkQueueAndPreload, show nativeAd ${nativeAd.mLayoutType}, key = $key")
                val listener = getAdWrapperListener(nativeAd, key)
                nativeAd.addListener(listener)
                nativeAd.showAds(AdsModule.getInstance().context, nativeAd.getContainer(), null)
            }

            if (nativeAd == null) {
                removeFromQueueAndLoad(key)
            }
        }
    }

    private fun getAdWrapperListener(nativeAd: NativeAdViewWrapper, key: Int): AdWrapperListener {
        val listener = object : AdWrapperListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                delayRemoveListeners(nativeAd, this)
                removeFromQueueAndLoad(key)
            }

            override fun onAdFailedToLoad(error: Int) {
                super.onAdFailedToLoad(error)
                delayRemoveListeners(nativeAd, this)
                removeFromQueueAndLoad(key)
            }
        }
        return listener
    }

    @SuppressLint("LogNotTimber")
    val coroutineExceptionHandler = CoroutineExceptionHandler() { _, throwable ->
        LogUtils.e(throwable)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun delayRemoveListeners(nativeAd: NativeAdViewWrapper?, listener: AdWrapperListener?) {
        GlobalScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            delay(250)
            nativeAd?.removeListener(listener)
        }
    }

    private fun removeFromQueueAndLoad(key: Int) {
        if (isDestroying) return
        mNativeAdQueue.remove(key)
        mQueue.remove(key)
        isLoading = false
        checkQueueAndPreload()
    }

    fun destroy() {
        isDestroying = true
        mQueue.clear()
        if (mNativeAdQueue.isNotEmpty()) {
            mNativeAdQueue.keys.forEach { key ->
                mNativeAdQueue[key]?.destroy()
            }
            mNativeAdQueue.clear()
        }
        isDestroying = false
        isLoading = false
    }
}