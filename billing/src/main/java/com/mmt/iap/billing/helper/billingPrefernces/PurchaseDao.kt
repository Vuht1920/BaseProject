package com.mmt.iap.billing.helper.billingPrefernces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mmt.iap.billing.helper.billingPrefernces.PurchasedProduct

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchasedProduct(purchase: PurchasedProduct)

    @Query("SELECT * FROM purchased_products")
    suspend fun getAllPurchasedProducts(): List<PurchasedProduct>

}