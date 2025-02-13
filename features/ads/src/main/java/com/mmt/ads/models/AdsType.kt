package com.mmt.ads.models

enum class AdsType(var value: String) {
    BANNER_ALL("banner_all"),
    NATIVE_ALL("native_all"),
    NATIVE_LIST("native_list"),
    NATIVE_TAB_HOME("native_tab_home"),
    NATIVE_EXIT_DIALOG("native_exit_dialog"),
    NATIVE_EMPTY_SCREEN("native_empty_screen"),
    NATIVE_LYRICS_EMPTY("native_lyrics_empty"),
    NATIVE_LYRICS_DIALOG("native_lyrics_dialog"),
    INTERSTITIAL_OPA("interstitial_opa"),
    APP_OPEN_ADS("app_open_ads"),

    INTERSTITIAL("interstitial")
}
