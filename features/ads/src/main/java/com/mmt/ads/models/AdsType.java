package com.mmt.ads.models;

public enum AdsType {
    BANNER_BOTTOM("banner_bottom"),
    BANNER_EMPTY_SCREEN("banner_empty_screen"),
    BANNER_EXIT_DIALOG("banner_exit_dialog"),
    INTERSTITIAL_OPA("interstitial_opa"),
    REWARDED_AD("rewarded_ad"),
    INTERSTITIAL("interstitial");

    final String value;

    public String getValue() {
        return value;
    }

    AdsType(String value) {
        this.value = value;
    }
}
