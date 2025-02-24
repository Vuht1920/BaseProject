package com.mmt.extractor.ui.home

import androidx.lifecycle.viewModelScope
import com.mmt.extractor.base.BaseViewModel
import com.mmt.extractor.domain.model.AppInfo
import com.mmt.extractor.domain.useCase.GetAppListInDeviceUseCase
import com.mmt.extractor.domain.useCase.GetAppListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAppListInDeviceUseCase: GetAppListInDeviceUseCase,
    private val getAppsUseCase: GetAppListUseCase
) : BaseViewModel() {

    val appInfoFlow = MutableStateFlow<List<AppInfo>>(emptyList())


    init {
        viewModelScope.launch {
            getAppListInDeviceUseCase()
        }
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            getAppsUseCase().collect {
                appInfoFlow.value = it
            }
        }
    }
}