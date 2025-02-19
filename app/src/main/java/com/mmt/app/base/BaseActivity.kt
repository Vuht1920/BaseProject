package com.mmt.app.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.mmt.R
import com.mmt.ads.AdsModule
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {
    val TAG = this::class.java.simpleName
    protected val mHandler = Handler(Looper.myLooper() ?: Looper.getMainLooper())

    open fun getBottomAdContainer(): ViewGroup? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoBackground_Dark_Accent1)
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