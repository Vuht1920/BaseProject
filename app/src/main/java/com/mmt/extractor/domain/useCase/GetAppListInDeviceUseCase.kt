package com.mmt.extractor.domain.useCase

import com.mmt.extractor.data.repository.applications.AppInfoRepository
import javax.inject.Inject

class GetAppListInDeviceUseCase @Inject constructor(private val appInfoRepository: AppInfoRepository) {
    suspend operator fun invoke() {
        appInfoRepository.queryAllAppInDevice()
    }
}