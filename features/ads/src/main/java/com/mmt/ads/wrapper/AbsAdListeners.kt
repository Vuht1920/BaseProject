package com.mmt.ads.wrapper

abstract class AbsAdListeners {

    open var mAdListeners: HashMap<String, AdWrapperListener?> = hashMapOf()

    fun addListener(listener: AdWrapperListener?) {
        try {
            listener?.let {
                val key = it.hashCode().toString()
                mAdListeners[key] = it
            }
        } catch (_: Exception) {
        }

        removeNullListeners()
    }

    fun removeListener(listener: AdWrapperListener?) {
        try {
            listener?.let {
                val key = it.hashCode().toString()
                if (mAdListeners.containsKey(key)) {
                    mAdListeners.remove(key)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun removeNullListeners() {
        try {
            val removeKeys: MutableList<String> = ArrayList()
            for ((key, value) in mAdListeners) {
                if (value == null) {
                    removeKeys.add(key)
                }
            }
            for (key in removeKeys) {
                mAdListeners.remove(key)
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * AdListeners
     * */
    open fun notifyAdLoaded() {
        try {
            mAdListeners.values.forEach { listener ->
                listener?.onAdLoaded()
            }
        } catch (_: Exception) {
        }
    }

    open fun notifyAdLoadFailed(errorCode: Int = -101) {
        try {
            mAdListeners.values.forEach { listener ->
                listener?.onAdFailedToLoad(errorCode)
            }
        } catch (_: Exception) {
        }
    }

    open fun notifyAdOpened() {
        try {
            mAdListeners.values.forEach { listener ->
                listener?.onAdOpened()
            }
        } catch (_: Exception) {
        }
    }

    open fun notifyAdClicked() {
        try {
            mAdListeners.values.forEach { listener ->
                listener?.onAdClicked()
            }
        } catch (_: Exception) {
        }
    }

    open fun notifyAdClosed() {
        try {
            mAdListeners.values.forEach { listener ->
                listener?.onAdClosed()
            }
        } catch (_: Exception) {
        }
    }
}