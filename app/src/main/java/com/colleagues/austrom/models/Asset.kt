package com.colleagues.austrom.models

class Asset(
    var assetId: String? = null,
    val assetTypeId: Int = 0,
    val userId: String? = null,
    val assetName: String = "",
    val amount: Double = 0.0,
    val currencyId: String? = null,
    val isPrivate: Boolean = false) {

    companion object {
        val defaultAssetTypes = listOf<String>(
            "Card", "Cash", "Investment"
        )
    }

}