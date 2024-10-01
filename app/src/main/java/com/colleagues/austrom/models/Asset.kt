package com.colleagues.austrom.models

class Asset(
    var assetId: String? = null,
    val assetTypeId: AssetType? = null,
    val userId: String? = null,
    val assetName: String = "",
    var amount: Double = 0.0,
    val currencyCode: String? = null,
    var isPrimary: Boolean = false,
    var isPrivate: Boolean = false) {

    override fun toString(): String {
        return "$assetId~$assetTypeId~$userId~$assetName~$amount~$currencyCode~$isPrimary~$isPrivate"
    }

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

        fun parseFromString(string: String): Asset {
            val array = string.split("~")
            return Asset(
                assetId = array[0],
                assetTypeId = when (array[1]) {"CARD"->AssetType.CARD
                                              "CASH"->AssetType.CASH
                                              "INVESTMENT"->AssetType.INVESTMENT
                                              else -> AssetType.CARD},
                userId = array[2],
                assetName = array[3],
                amount = array[4].toDouble(),
                currencyCode = array[5],
                isPrimary = (array[6]=="true"),
                isPrivate = (array[7]=="true"),
            )
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