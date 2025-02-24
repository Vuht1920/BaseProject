package com.mmt.extractor.domain.useCase

import com.mmt.extractor.data.repository.applications.AppInfoRepository
import com.mmt.extractor.domain.mapper.toDomainModel
import com.mmt.extractor.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAppListUseCase @Inject constructor(private val appInfoRepository: AppInfoRepository) {
    suspend operator fun invoke(): Flow<List<AppInfo>> {
        return appInfoRepository.getAllAppInfo().map { it.map { it.toDomainModel() } }
    }
}