package com.mmt.extractor.ui.home

import androidx.lifecycle.viewModelScope
import com.mmt.extractor.base.BaseViewModel
import com.mmt.extractor.domain.useCase.GetAppListUseCase
import com.mmt.extractor.utils.log.DebugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val getAppListUseCase: GetAppListUseCase) : BaseViewModel() {
    fun loadData() {
        DebugLog.loge("dlaldsadl: ")
        viewModelScope.launch {
            getAppListUseCase()
        }
    }

    init {
        DebugLog.loge("dlaldsadl: ")
        viewModelScope.launch {
            getAppListUseCase()
        }
    }
}