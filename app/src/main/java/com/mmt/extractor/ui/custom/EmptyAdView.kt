package com.mmt.extractor.ui.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ConvertUtils
import com.mmt.ads.AdsModule
import com.mmt.ads.config.AdsConfig
import com.mmt.extractor.R

class EmptyAdView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : NestedScrollView(context, attrs, defStyleAttr) {
    private lateinit var mEmptyTextView: TextView
    private lateinit var mAdContainer: FrameLayout

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        isFillViewport = true
        val padding = ConvertUtils.dp2px(8f)
        val linear = LinearLayout(getContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, padding, 0, padding)
            val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.EmptyAdView)

            mEmptyTextView = TextView(getContext())
            mEmptyTextView.apply {
                setPadding(padding, padding, padding, padding)
//                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                gravity = Gravity.CENTER
                var emptyText = typedArray.getText(R.styleable.EmptyAdView_emptyText)
                if (TextUtils.isEmpty(emptyText)) {
                    emptyText = context.getString(R.string.msg_empty_data)
                }
                text = emptyText
                if (Build.VERSION.SDK_INT >= 23) {
                    setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
                }
            }
            mAdContainer = FrameLayout(getContext())
            addView(mEmptyTextView, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = 3 * padding
                marginEnd = 3 * padding
            })
            addView(mAdContainer, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

            mAdContainer.visibility = GONE
            mEmptyTextView.setTextColor(typedArray.getColor(R.styleable.EmptyAdView_emptyTextColor, Color.WHITE))

            typedArray.recycle()
        }
        addView(linear, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    fun showEmptyAd() {
        if (AdsConfig.getInstance().canShowAd()) {
            mAdContainer.visibility = VISIBLE
            AdsModule.getInstance().showBannerEmptyScreen(mAdContainer)
        } else {
            hideEmptyAd()
        }
    }

    fun hideEmptyAd() {
        mAdContainer.removeAllViews()
        mAdContainer.visibility = GONE
    }

    fun setMessage(msg: String?) {
        if (msg != null) {
            mEmptyTextView.text = msg
        }
    }

    fun setMessage(resString: Int) {
        if (resString > 0) {
            mEmptyTextView.text = context.getString(resString)
        }
    }

    fun setTextColor(color: Int) {
        mEmptyTextView.setTextColor(color)
    }

}