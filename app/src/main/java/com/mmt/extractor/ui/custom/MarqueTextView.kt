package com.mmt.extractor.ui.custom

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MarqueTextView : AppCompatTextView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        isSelected = true
    }
}