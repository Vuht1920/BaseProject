package com.mmt.extractor.data.model

sealed class CategoryApp {
    data object All : CategoryApp()
    data object Undefined : CategoryApp()
    data object Social : CategoryApp()
    data object Games : CategoryApp()
    data object AudioVideo : CategoryApp()
    data object Productivity : CategoryApp()
    data object Video : CategoryApp()
    data object Image : CategoryApp()
    data object News: CategoryApp()
    data object Maps: CategoryApp()
    data object Accessibility: CategoryApp()
}