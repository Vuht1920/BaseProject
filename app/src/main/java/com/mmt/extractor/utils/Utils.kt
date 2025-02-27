package com.mmt.extractor.utils

import android.app.PendingIntent
import android.os.Build
import android.widget.ImageView
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.mmt.extractor.base.BaseActivity
import com.mmt.extractor.base.BaseFragment


object Utils {
    fun getPendingIntentFlag(defaultFlag: Int = PendingIntent.FLAG_UPDATE_CURRENT): Int {
        var flag = defaultFlag
        if (Build.VERSION.SDK_INT >= 31) {
            flag = flag or PendingIntent.FLAG_MUTABLE
        }
        return flag
    }

    fun transitionEffectFragmentTransaction(context: BaseActivity, id: Int, from: BaseFragment, to: BaseFragment, imageView: ImageView) {

        // Inflate transitions
        val changeTransform: Transition? = TransitionInflater.from(context).inflateTransition(android.R.transition.move)

        val explodeTransform: Transition? = TransitionInflater.from(context).inflateTransition(android.R.transition.explode)


        // Cài đặt hiệu ứng chuyển tiếp khi thoát khỏi fragment thứ nhất
        from.sharedElementReturnTransition = changeTransform
        from.exitTransition = explodeTransform


        // Cài đặt hiệu ứng chuyển tiếp khi bật fragment thứ hai
        to.sharedElementEnterTransition = changeTransform
        to.enterTransition = explodeTransform

        // Replace fragment thứ hai
        val beginTransaction = context.supportFragmentManager.beginTransaction()
        beginTransaction.replace(id, to)
            .addToBackStack(to.TAG)
            .addSharedElement(imageView, "app_preview")
            .commit()
    }
}