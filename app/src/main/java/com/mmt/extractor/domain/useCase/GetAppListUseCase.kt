package com.mmt.extractor.domain.useCase

import com.mmt.extractor.data.repository.applications.AppInfoRepository
import com.mmt.extractor.utils.log.DebugLog
import javax.inject.Inject

class GetAppListUseCase @Inject constructor(private val appInfoRepository: AppInfoRepository) {
    suspend operator fun invoke() {
        DebugLog.loge("dlaldsadl: ")
        appInfoRepository.queryAllAppInDevice()
    }
}