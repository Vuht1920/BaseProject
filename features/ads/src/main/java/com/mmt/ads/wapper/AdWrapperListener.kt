package com.mmt.ads.wapper

import android.view.ViewGroup

abstract class AdWrapperListener {
    open fun onAdStartLoad(id: String) {}
    open fun onAdLoaded() {}
    open fun onAdFailedToLoad(error: Int, message: String?) {}
    open fun onAdClicked() {}
    open fun onAdPreShow() {}
    open fun onAdOpened() {}
    open fun onAdClosed() {}
    open fun onAdAttachedToContainer(container: ViewGroup){}
    open fun ignoreRequestAd(why: String) {}
    open fun badLoadingAd(id: String) {}
}
