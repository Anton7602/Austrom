package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import java.util.UUID

@Entity
data class User(
    @PrimaryKey(autoGenerate = false) @Exclude
    var userId: String = "",
    var username: String? = null,
    var email: String? = null,
    var password: String? = null,
    var activeBudgetId: String? = null,
    var primaryPaymentMethod: String? = null,
    var baseCurrencyCode: String = "USD") {
    //var categories: MutableList<Category> = mutableListOf()) {

    companion object {
        fun generateUniqueAssetKey() : String {
            return UUID.randomUUID().toString()
        }
    }
}
