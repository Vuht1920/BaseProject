package com.mmt.ads.wrapper;

public interface AdOPAListener {

    void onAdOPACompleted();

    default void onAdOPALoaded() {
    }

    default void onAdOPAOpened() {
    }
}
