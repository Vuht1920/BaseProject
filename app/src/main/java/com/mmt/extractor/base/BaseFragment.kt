package com.mmt.extractor.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId){
     val TAG = this::class.java.simpleName
}