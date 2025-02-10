package com.mmt.iap.billing.listeners

import com.mmt.iap.billing.model.ErrorType
import com.mmt.iap.billing.model.MyPurchase

interface BillingEventListener {
    fun onProductsPurchased(myPurchases: List<MyPurchase?>)
    fun onPurchaseAcknowledged(myPurchase: MyPurchase)
    fun onPurchaseConsumed(myPurchase: MyPurchase)
    fun onBillingError(error: ErrorType)
}