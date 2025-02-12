package com.mmt.ads.models

enum class AdsType(var value: String) {
    BANNER_IN_APP("banner_in_app"),
    BANNER_EXIT_DIALOG("banner_exit_dialog"),
    BANNER_EMPTY_SCREEN("banner_empty_screen"),
    INTERSTITIAL("interstitial"),
    INTERSTITIAL_OPA("interstitial_opa"),
    INTERSTITIAL_GIFT("interstitial_gift")
}
