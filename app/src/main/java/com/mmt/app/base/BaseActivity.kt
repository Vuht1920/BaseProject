package com.mmt.app.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.blankj.utilcode.util.LogUtils.A
import com.mmt.R
import com.mmt.ads.AdsModule
import com.mmt.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding

@AndroidEntryPoint
open class BaseActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.WHITE))
        super.onCreate(savedInstanceState)
    }

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
}