package com.mmt.app.utils.extensions

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.SystemClock
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.mmt.R
import com.mmt.app.utils.log.DebugLog


fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun View.setVisibleOrHidden(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

fun RecyclerView.snapToPosition(position: Int, toDown: Boolean) {
    if (toDown) {
        (layoutManager as? LinearLayoutManager)?.scrollToPosition(position)
    } else {
        (layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, 0)
    }
}

fun Toolbar.marqueeRepeatLimit() {
    try {
        val toolbarClass = Toolbar::class.java
        val titleTextViewField = toolbarClass.getDeclaredField("mTitleTextView")
        titleTextViewField.isAccessible = true
        (titleTextViewField.get(this) as TextView).apply {
            isSelected = true
            setHorizontallyScrolling(true)
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ViewGroup.changeHeightAnimation(isExpand: Boolean, normalHeight: Int, expandHeight: Int, animationRunning: (Boolean) -> Unit, duration: Long = 150) {
    val currentHeight = this.height
    try {
        clearAnimation()
        animationRunning.invoke(true)
        val fromHeight = if (isExpand) normalHeight else expandHeight
        val toHeight = if (isExpand) expandHeight else normalHeight
        if (fromHeight == toHeight && toHeight == currentHeight) {
            animationRunning.invoke(false)
            return
        }
        val slideAnimator: ValueAnimator = ValueAnimator
            .ofInt(fromHeight, toHeight)
            .setDuration(duration)

        slideAnimator.addUpdateListener {
            val layoutParams: ViewGroup.LayoutParams = layoutParams
            val height = slideAnimator.animatedValue as Int
            if (height == layoutParams.height) return@addUpdateListener
            layoutParams.height = height
            requestLayout()
        }

        val animationSet = AnimatorSet()
        animationSet.play(slideAnimator)
        animationSet.interpolator = LinearInterpolator()
        animationSet.start()
        animationSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                if (isExpand) {
                    for (child in children) {
                        child.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        child.requestLayout()
                    }
                }
                animationRunning.invoke(false)
            }

            override fun onAnimationCancel(p0: Animator) {
                animationRunning.invoke(false)
            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })

    } catch (e: Exception) {
        DebugLog.loge(e)
    }
}

fun TextView.tintIcon(color: Int) {
    for (drawable in getCompoundDrawables()) {
        if (drawable != null) {
            drawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN)
        }
    }
}

fun View.setEnableAndAlpha(isEnable: Boolean) {
    if (isEnable) {
        this.isEnabled = true
        this.alpha = 1f
    } else {
        this.isEnabled = false
        this.alpha = 0.5f
    }
}

fun TabLayout.applyMarqueeRepeatLimit(onlyUpdateSelected: Boolean = false) {
    for (position in 0 until this.tabCount) {
        val currentSelect = this.selectedTabPosition
        (this.getTabAt(position)?.view?.getChildAt(1) as? TextView)?.let {
            if (onlyUpdateSelected) {
                it.isSelected = true
                if (currentSelect != position) {
                    it.setTextColor(ContextCompat.getColor(this.context, R.color.color_A2A9B0))
                }
            } else {
                it.isSelected = true
                it.isSingleLine = true
                it.marqueeRepeatLimit = -1
                it.ellipsize = TextUtils.TruncateAt.MARQUEE
                it.isFocusable = true
                it.isFocusableInTouchMode = true
                it.setHorizontallyScrolling(true)
            }
        }
    }
}

fun EditText?.focusEdittext() {
    this?.postDelayed({
        val uptimeMillis = SystemClock.uptimeMillis()
        val endOf = this.width.toFloat()
        this.dispatchTouchEvent(MotionEvent.obtain(uptimeMillis, uptimeMillis, MotionEvent.ACTION_DOWN, endOf, 0f, 0));
        this.dispatchTouchEvent(MotionEvent.obtain(uptimeMillis + 50, uptimeMillis + 50, MotionEvent.ACTION_UP, endOf, 0f, 0));
        this.setSelection(this.text.length)
    }, 100)
}