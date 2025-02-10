package com.mmt.iap.billing.listeners

interface BillingClientListener {
    fun onPurchasesUpdated()
    fun onClientReady()
    fun onClientAllReadyConnected(){}
    fun onClientInitError()
}