package com.mmt.iap.billing.helper.billingPrefernces

import android.content.Context

class PurchasedHistoryUtils(private val context: Context) {
	
	suspend fun recordPurchase(purchase: PurchasedProduct) {
		val db = PurchaseDatabase.getDatabase(context)
		db.purchaseDao().insertPurchasedProduct(purchase)
	}
	
	suspend fun hasUserEverPurchased(): Boolean {
		val db = PurchaseDatabase.getDatabase(context)
		return db.purchaseDao().getAllPurchasedProducts().isNotEmpty()
	}
	
	suspend fun getPurchasedPlansHistory(): List<PurchasedProduct> {
		val db = PurchaseDatabase.getDatabase(context)
		return db.purchaseDao().getAllPurchasedProducts()
	}
	
}