package com.mmt.extractor.utils

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mmt.extractor.R

class GlideUtils {
    fun loadImageWithGlide(
        context: Context?,
        obj: Any?,
        holder: Int,
        target: ImageView,
        scaleType: ImageView.ScaleType? = null,
        priority: Priority? = null
    ) {
        var placeHolder = holder
        if (obj == null || context == null) {
            return
        }
        if (context is Activity && context.isDestroyed) return
        if (placeHolder == 0) {
            placeHolder = R.drawable.thumb_default
        }
        var requestPriority = Priority.NORMAL
        priority?.let { requestPriority = it }
        var requestOptions = RequestOptions()
            .placeholder(placeHolder)
            .error(placeHolder)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .priority(requestPriority)
        requestOptions = when (scaleType) {
            ImageView.ScaleType.CENTER_CROP -> requestOptions.centerCrop()
            ImageView.ScaleType.CENTER_INSIDE -> requestOptions.centerInside()
            ImageView.ScaleType.FIT_CENTER -> requestOptions.fitCenter()
            else -> requestOptions.centerCrop()
        }
        Glide.with(context)
            .load(obj)
            .apply(requestOptions)
            .transition(GenericTransitionOptions.with(R.anim.glide_anim))
            .into(target)
    }
}