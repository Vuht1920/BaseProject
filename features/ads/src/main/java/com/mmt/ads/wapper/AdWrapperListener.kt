package com.mmt.ads.wapper

abstract class AdWrapperListener {
    open fun onAdStartLoad() {}
    open fun onAdLoaded() {}
    open fun onAdFailedToLoad(error: Int) {}
    open fun onAdClicked() {}
    open fun onAdPreShow() {}
    open fun onAdOpened() {}
    open fun onAdClosed() {}
}
