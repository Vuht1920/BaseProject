package com.mmt.ads.utils

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.GsonUtils
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.AdsId
import com.mmt.ads.models.AdsType
import com.mmt.ads.utils.AdDebugLog.logd
import com.mmt.ads.utils.AdDebugLog.loge
import java.util.Locale

object AdsUtils {
    fun addAdsToContainer(container: ViewGroup?, adView: View?) {
        try {
            if (container == null || !AdsConfig.getInstance().canShowAd()) {
                return
            }
            if (adView != null) {
                if (adView.parent != null) {
                    if (adView.parent == container) {
                        return
                    }
                    (adView.parent as ViewGroup).removeAllViews()
                }

                container.removeAllViews()
                container.addView(adView)
                if (container.background == null) {
                    container.setBackgroundColor(Color.parseColor("#5C000000"))
                }
                setupAdContainerAttachStateListener(container)

                // Adview cách view liền kế tối thiểu 2px
                val layoutParams = adView.layoutParams
                val topMargin = 2
                when (layoutParams) {
                    is LinearLayout.LayoutParams -> {
                        layoutParams.topMargin = topMargin
                        layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }

                    is FrameLayout.LayoutParams -> {
                        layoutParams.topMargin = topMargin
                        layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }

                    is RelativeLayout.LayoutParams -> {
                        layoutParams.topMargin = topMargin
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
                    }
                }
                adView.layoutParams = layoutParams
            } else {
                setHeightForContainer(container, 0)
            }
        } catch (e: Exception) {
            loge(e)
        }
    }

    fun marginAd(adView: View?) {
        if (adView != null && adView.parent != null) {
            val layoutParams = adView.layoutParams
            val margin: Int = ConvertUtils.dp2px(12f)
            when (layoutParams) {
                is LinearLayout.LayoutParams -> {
                    layoutParams.setMargins(margin, margin, margin, margin)
                    layoutParams.gravity = Gravity.CENTER
                }

                is FrameLayout.LayoutParams -> {
                    layoutParams.setMargins(margin, margin, margin, margin)
                    layoutParams.gravity = Gravity.CENTER
                }

                is RelativeLayout.LayoutParams -> {
                    layoutParams.setMargins(margin, margin, margin, margin)
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
                }
            }
            adView.layoutParams = layoutParams
        }
    }

    private val listener: View.OnAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {}
        override fun onViewDetachedFromWindow(view: View) {
            if (view is ViewGroup) {
                view.removeAllViews()
            }
            view.removeOnAttachStateChangeListener(this)
        }
    }

    fun setupAdContainerAttachStateListener(container: View?) {
        if (container == null) {
            return
        }
        container.removeOnAttachStateChangeListener(listener)
        container.addOnAttachStateChangeListener(listener)
    }

    fun setHeightForContainer(container: View?, height: Int) {
        try {
            if (container != null) {
                val layoutParams = container.layoutParams
                if (height == 0) {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    layoutParams.height = height + 2
                }
                container.layoutParams = layoutParams
            }
        } catch (e: Exception) {
            loge(e)
        }
    }


    /**
    *
    * */
    fun readIdsFromAssetsFile(context: Context?, assetsFileName: String?): AdsId? {
        if (context == null || assetsFileName == null) {
            return null
        }
        val data = Utils.readTextFileInAsset(context, assetsFileName)
        if (!TextUtils.isEmpty(data)) {
            return GsonUtils.fromJson(data, GsonUtils.getType(AdsId::class.java))
        }
        return null
    }

    // Sắp xếp Ads id theo config truyền vào
    fun mixAdsIdWithConfig(admobIds: AdsId?, fanIds: AdsId?, adsIdConfigList: List<String>): AdsId? {
        if (adsIdConfigList.isNotEmpty()) {
            val adsId = AdsId()
            // std_banner
            if (!admobIds?.banner_in_app.isNullOrEmpty() || !fanIds?.banner_in_app.isNullOrEmpty()) {
                logd("Mix banner_in_app")
                adsId.banner_in_app = mixAdsId(
                    admobIds?.banner_in_app,
                    fanIds?.banner_in_app,
                    adsIdConfigList
                )
            }
            // banner_exit_dialog
            if (!admobIds?.banner_exit_dialog.isNullOrEmpty() || !fanIds?.banner_exit_dialog.isNullOrEmpty()) {
                logd("Mix banner_exit_dialog")
                adsId.banner_exit_dialog = mixAdsId(
                    admobIds?.banner_exit_dialog,
                    fanIds?.banner_exit_dialog,
                    adsIdConfigList
                )
            }
            // banner_empty_screen
            if (!admobIds?.banner_empty_screen.isNullOrEmpty() || !fanIds?.banner_empty_screen.isNullOrEmpty()) {
                logd("Mix banner_empty_screen")
                adsId.banner_empty_screen = mixAdsId(
                    admobIds?.banner_empty_screen,
                    fanIds?.banner_empty_screen,
                    adsIdConfigList
                )
            }
            // interstitial
            if (!admobIds?.interstitial.isNullOrEmpty() || !fanIds?.interstitial.isNullOrEmpty()) {
                logd("Mix interstitial")
                adsId.interstitial = mixAdsId(
                    admobIds?.interstitial,
                    fanIds?.interstitial,
                    adsIdConfigList
                )
            }
            // interstitial_opa
            if (!admobIds?.interstitial_opa.isNullOrEmpty() || !fanIds?.interstitial_opa.isNullOrEmpty()) {
                logd("Mix interstitial_opa")
                adsId.interstitial_opa = mixAdsId(
                    admobIds?.interstitial_opa,
                    fanIds?.interstitial_opa,
                    adsIdConfigList
                )
            }
            // interstitial_gift
            if (!admobIds?.interstitial_gift.isNullOrEmpty() || !fanIds?.interstitial_gift.isNullOrEmpty()) {
                logd("Mix interstitial_gift")
                adsId.interstitial_gift = mixAdsId(
                    admobIds?.interstitial_gift,
                    fanIds?.interstitial_gift,
                    adsIdConfigList
                )
            }
            return adsId
        }
        return null
    }

    fun mixAdsId(admobIds: List<String>?, fanIds: List<String>?, adsIdConfigList: List<String>): List<String> {
        if (adsIdConfigList.isNotEmpty()) {
            val adsIdList: MutableList<String> = ArrayList()
            for (adsConfig in adsIdConfigList) { // adsConfig = ADMOB-0 | FAN-0
                var position = 0
                try {
                    // Lấy ra vị trí id cần lấy trong mảng
                    position = adsConfig.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }.toInt()
                } catch (e: java.lang.Exception) {
                    loge(e)
                }

                // Kiểm tra xem position của id có trong mảng tương ứng không, nếu có thì thêm tiền tố tương ứng rồi add vào list
                if (adsConfig.lowercase(Locale.getDefault()).contains(AdsConstants.ADMOB) && admobIds != null && position < admobIds.size) {
                    adsIdList.add(admobIds[position])
                } else if (adsConfig.lowercase(Locale.getDefault()).contains(AdsConstants.FAN) && fanIds != null && position < fanIds.size) {
                    adsIdList.add(AdsConstants.FAN_ID_PREFIX + fanIds[position])
                }
            }

            logAdsId(adsIdList.toTypedArray<String>())
            return adsIdList
        }
        return ArrayList()
    }

    fun logAdsId(adsIdList: Array<String>) {
        val builder = StringBuilder()
        for (id in adsIdList) {
            builder.append("\n").append(id)
        }
        logd("Ads id:$builder")
    }

    fun mixCustomAdsIdConfig(adsId: AdsId?, admobIds: AdsId?, fanIds: AdsId?, adsType: AdsType?, adsIdConfigList: List<String>) {
        if (adsId != null && adsType != null && adsIdConfigList.isNotEmpty()) {
            logd("mixCustomAdsIdConfig - " + adsType.value)
            when (adsType) {
                AdsType.BANNER_IN_APP -> {
                    adsId.banner_in_app = mixAdsId(
                        admobIds?.banner_in_app,
                        fanIds?.banner_in_app,
                        adsIdConfigList
                    )
                }
                AdsType.BANNER_EXIT_DIALOG -> {
                    adsId.banner_exit_dialog = mixAdsId(
                        admobIds?.banner_exit_dialog,
                        fanIds?.banner_exit_dialog,
                        adsIdConfigList
                    )
                }
                AdsType.BANNER_EMPTY_SCREEN -> {
                    adsId.banner_empty_screen = mixAdsId(
                        admobIds?.banner_empty_screen,
                        fanIds?.banner_empty_screen,
                        adsIdConfigList
                    )
                }
                AdsType.INTERSTITIAL_OPA -> {
                    adsId.interstitial_opa = mixAdsId(
                        admobIds?.interstitial_opa,
                        fanIds?.interstitial_opa,
                        adsIdConfigList
                    )
                }
                AdsType.INTERSTITIAL -> {
                    adsId.interstitial = mixAdsId(
                        admobIds?.interstitial,
                        fanIds?.interstitial,
                        adsIdConfigList
                    )
                }
                AdsType.INTERSTITIAL_GIFT -> {
                    adsId.interstitial_gift = mixAdsId(
                        admobIds?.interstitial_gift,
                        fanIds?.interstitial_gift,
                        adsIdConfigList
                    )
                }
            }
        }
    }

    fun getAdName(adId: String): String {
        var name = adId
        when {
            adId.endsWith("1358810902") || adId.endsWith("2266011543") || adId.endsWith("8833081360") -> {
                name = "banner_in_app"
            }
            adId.endsWith("4732392780") || adId.endsWith("3469968921") -> {
                name = "banner_exit_dialog"
            }
            adId.endsWith("7600785627") || adId.endsWith("5853902765") -> {
                name = "banner_empty_screen"
            }
        }
        return name
    }
}
