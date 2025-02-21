package com.mmt.app.base

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.mmt.ads.AdsModule
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity() : AppCompatActivity() {
    val TAG = this::class.java.simpleName
    protected val mHandler = Handler(Looper.myLooper() ?: Looper.getMainLooper())

    open fun getBottomAdContainer(): ViewGroup? {
        return null
    }

    open fun showBottomBanner() {
        AdsModule.getInstance().showBannerBottom(getBottomAdContainer())
    }

    override fun onResume() {
        super.onResume()
        // Show bottom banner
        showBottomBanner()
    }

    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}