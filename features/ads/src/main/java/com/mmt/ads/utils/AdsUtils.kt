package com.mmt.ads.utils

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blankj.utilcode.util.ConvertUtils
import com.google.android.gms.ads.AdView
import com.mmt.ads.config.AdsConfig

object AdsUtils {
    fun addAdsToContainer(container: ViewGroup?, adView: View?) {
        try {
            if (container == null || !AdsConfig.getInstance().canShowAd()) {
                return
            }
            if (adView != null) {
                if (adView.parent != null) {
                    if (adView.parent === container) {
                        return
                    }
                    (adView.parent as ViewGroup).removeAllViews()
                }

                //                container.setVisibility(adView.getVisibility());
                container.removeAllViews()
                container.addView(adView)
                if (container.background == null) {
                    container.setBackgroundColor(Color.parseColor("#5C000000"))
                }
                setupAdContainerAttachStateListener(container)

                // Adview cách view liền kế tối thiểu 2px
                val layoutParams = adView.layoutParams
                val topMargin = 2
                if (layoutParams is LinearLayout.LayoutParams) {
                    layoutParams.topMargin = topMargin
                    layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                } else if (layoutParams is FrameLayout.LayoutParams) {
                    layoutParams.topMargin = topMargin
                    layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                } else if (layoutParams is RelativeLayout.LayoutParams) {
                    layoutParams.topMargin = topMargin
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
                }
                adView.layoutParams = layoutParams
            } else {
                setHeightForContainer(container, 0)
                container.visibility = View.GONE
            }
        } catch (e: Exception) {
            AdDebugLog.loge(e)
        }
    }

    fun marginAd(adView: View?) {
        if (adView != null && adView.parent != null) {
            val layoutParams = adView.layoutParams
            val margin = ConvertUtils.dp2px(12f)
            if (layoutParams is LinearLayout.LayoutParams) {
                layoutParams.setMargins(margin, margin, margin, margin)
                layoutParams.gravity = Gravity.CENTER
            } else if (layoutParams is FrameLayout.LayoutParams) {
                layoutParams.setMargins(margin, margin, margin, margin)
                layoutParams.gravity = Gravity.CENTER
            } else if (layoutParams is RelativeLayout.LayoutParams) {
                layoutParams.setMargins(margin, margin, margin, margin)
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            }
            adView.layoutParams = layoutParams
        }
    }

    private val listener: View.OnAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
        }

        override fun onViewDetachedFromWindow(view: View) {
            if (view is ViewGroup) {
                view.removeAllViews()
            }
            view.removeOnAttachStateChangeListener(this)
        }
    }

    fun resumeAdView(container: View?, isResume: Boolean) {
        if (container is ViewGroup && container.childCount > 0) {
            val adView = container.getChildAt(0)
            if (adView is AdView) {
                if (isResume) {
                    adView.resume()
                } else {
                    adView.pause()
                }
            }
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
            AdDebugLog.loge(e)
        }
    }
}