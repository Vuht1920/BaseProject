package com.mmt.iap.billing.helper

import com.android.billingclient.api.Purchase
import com.mmt.iap.billing.model.MyPurchase

fun List<Purchase>.toMyPurchases(): List<MyPurchase> {
    return this.map { purchase ->
        MyPurchase(
            products = purchase.products.toMutableList(),
            purchaseState = purchase.purchaseState,
            purchaseToken = purchase.purchaseToken,
            isAcknowledged = purchase.isAcknowledged,
            packageName = purchase.packageName,
            developerPayload = purchase.developerPayload,
            isAutoRenewing = purchase.isAutoRenewing,
            orderId = purchase.orderId,
            originalJson = purchase.originalJson,
            purchaseTime = purchase.purchaseTime,
            quantity = purchase.quantity,
            signature = purchase.signature,
            skus = purchase.skus
        )
    }
}

fun Purchase.toMyPurchase(): MyPurchase {
    return MyPurchase(
        products = this.products.toMutableList(),
        purchaseState = this.purchaseState,
        purchaseToken = this.purchaseToken,
        isAcknowledged = this.isAcknowledged,
        packageName = this.packageName,
        developerPayload = this.developerPayload,
        isAutoRenewing = this.isAutoRenewing,
        orderId = this.orderId,
        originalJson = this.originalJson,
        purchaseTime = this.purchaseTime,
        quantity = this.quantity,
        signature = this.signature,
        skus = this.products
    )
}