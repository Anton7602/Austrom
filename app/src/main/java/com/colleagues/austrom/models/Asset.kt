package com.colleagues.austrom.models

class Asset(
    var assetId: String? = null,
    val assetTypeId: AssetType? = null,
    val userId: String? = null,
    val assetName: String = "",
    var amount: Double = 0.0,
    val currencyCode: String? = null,
    val isPrivate: Boolean = false) {

    companion object{
        fun groupAssetsByType(assets: MutableMap<String, Asset>) : MutableMap<String, MutableList<Asset>> {
            val groupedAssets = mutableMapOf<String, MutableList<Asset>>()
            for (asset in assets) {
                if (!groupedAssets.containsKey(asset.value.assetTypeId.toString())) {
                    groupedAssets[asset.value.assetTypeId.toString()] = mutableListOf()
                }
                groupedAssets[asset.value.assetTypeId.toString()]!!.add(asset.value)
            }
            return  groupedAssets
        }

        fun toList(assets: MutableMap<String, Asset>): MutableList<Asset> {
            val assetList = mutableListOf<Asset>()
            for (asset in assets) {
                assetList.add(asset.value)
            }
            return assetList
        }
    }
}

enum class AssetType{
    CARD, CASH, INVESTMENT
}