package com.mmt.extractor.domain.mapper

sealed class CategoryAppMapper {
    data object All : CategoryAppMapper()
    data object Undefined : CategoryAppMapper()
    data object Social : CategoryAppMapper()
    data object Games : CategoryAppMapper()
    data object AudioVideo : CategoryAppMapper()
    data object Productivity : CategoryAppMapper()
    data object Video : CategoryAppMapper()
    data object Image : CategoryAppMapper()
    data object News: CategoryAppMapper()
    data object Maps: CategoryAppMapper()
    data object Accessibility: CategoryAppMapper()
}