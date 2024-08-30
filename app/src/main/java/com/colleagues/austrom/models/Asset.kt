package com.colleagues.austrom.models

import android.icu.util.Currency

class Asset(
    val assetType_id: Int = 0,
    val user_id: String = "",
    val assetName: String = "",
    val amount: Double = 0.0,
    val currency_id: Int = 0,
    val isPrivate: Boolean = false) {

}