package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.IDatabaseProvider
import com.google.firebase.database.Exclude
import java.security.MessageDigest
import java.util.UUID

@Entity
class Asset(
    @PrimaryKey(autoGenerate = false) @Exclude
    var assetId: String = "",
    val assetTypeId: AssetType? = null,
    val userId: String? = null,
    val assetName: String = "",
    var amount: Double = 0.0,
    val currencyCode: String? = null,
    var isPrivate: Boolean = false) {

    fun delete(dbProvider: IDatabaseProvider) {
        val transactionsOfThisAsset = dbProvider.getTransactionsOfAsset(this)
        for (transaction in transactionsOfThisAsset) {
            dbProvider.deleteTransaction(transaction)
        }
        dbProvider.deleteAsset(this)
        AustromApplication.activeAssets.remove(assetId)
    }

    companion object{
        fun generateUniqueAssetKey(asset: Asset) : String {
//            val currentDateTime = System.currentTimeMillis()
//            val uniqueString = "${asset.userId}-$currentDateTime"
//            val digest = MessageDigest.getInstance("SHA-256")
//            val hashBytes = digest.digest(uniqueString.toByteArray())
//            val hexString = hashBytes.joinToString("") { "%02x".format(it) }
//            return hexString.take(24)
            return UUID.randomUUID().toString()
        }

        fun groupAssetsByType(assets: MutableMap<String, Asset>) : MutableMap<AssetType, MutableList<Asset>> {
            val groupedAssets = mutableMapOf<AssetType, MutableList<Asset>>()
            for (asset in assets) {
                if (!groupedAssets.containsKey(asset.value.assetTypeId)) {
                    groupedAssets[asset.value.assetTypeId!!] = mutableListOf()
                }
                groupedAssets[asset.value.assetTypeId]!!.add(asset.value)
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

enum class AssetType(val stringResourceId: Int = R.string.unresolved){
    CARD(R.string.card), CASH(R.string.cash), INVESTMENT(R.string.investment)
}