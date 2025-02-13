package com.mmt

import android.view.View
import android.view.ViewGroup
import com.mmt.ads.utils.AdsUtils

/**
 * Class lưu trữ lại toàn bộ container được dùng để hiển thị AdView, phục vụ cho việc duyệt và update lại height cho tất cả các container
 * Giải quyết vấn đề container ở MH trước vẫn chiếm space khi đã update VIP
 * */
object AdContainerWatcher {
    private val containers: HashSet<ViewGroup?> = hashSetOf()

    fun add(container: ViewGroup?) {
        container?.let {
            containers.add(it)
            // Lắng nghe event onViewDetachedFromWindow để tự động remove khỏi list watcher
            val attachStateChanged = object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                }

                override fun onViewDetachedFromWindow(v: View) {
                    try {
                        v.removeOnAttachStateChangeListener(this)
                        containers.remove(container)
                    } catch (_: Exception) {
                    }
                }
            }
            it.addOnAttachStateChangeListener(attachStateChanged)
        }
    }

    fun remove(container: ViewGroup?) {
        container?.let {
            try {
                containers.remove(it)
            } catch (_: Exception) {
            }
        }
    }

    fun wrapHeightForAllContainer() {
        containers.forEach { container ->
            // Set height cho container là WrapContent
            AdsUtils.setHeightForContainer(container, 0)
        }
    }

    fun clear() {
        containers.clear()
    }
}