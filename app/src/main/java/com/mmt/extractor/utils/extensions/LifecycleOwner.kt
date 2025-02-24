package com.mmt.extractor.utils.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mmt.extractor.BaseApplication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

fun <T> LifecycleOwner.safeCollect(flow: Flow<T>, collectCallBack: (T) -> Unit, flowOn: CoroutineDispatcher = Dispatchers.IO) {
    this.lifecycleScope.launch(BaseApplication.coroutineExceptionHandler()) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.flowOn(flowOn).collect {
                    collectCallBack(it)
                }
        }
    }
}