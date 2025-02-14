package com.mmt.ads.wrapper

abstract class AdWrapperListener {
    open fun onAdLoaded() {}
    open fun onAdFailedToLoad(error: Int) {}
    open fun onAdClicked() {}
    open fun onAdPreShow() {}
    open fun onAdOpened() {}
    open fun onAdClosed() {}
}
