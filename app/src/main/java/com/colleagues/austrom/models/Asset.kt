package com.colleagues.austrom.models

class Asset(
    var assetId: String? = null,
    val assetTypeId: AssetType? = null,
    val userId: String? = null,
    val assetName: String = "",
    var amount: Double = 0.0,
    val currencyId: String? = null,
    val isPrivate: Boolean = false) {
}

enum class AssetType{
    CARD, CASH, INVESTMENT
}