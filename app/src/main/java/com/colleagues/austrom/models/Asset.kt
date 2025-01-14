package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.google.firebase.database.Exclude
import java.util.UUID

@Entity
class Asset(var assetName: String, val assetTypeId: AssetType, val currencyCode: String, var amount: Double = 0.0) {
    @PrimaryKey(autoGenerate = false) @Exclude
    var assetId: String = generateUniqueAssetKey()
    var userId: String = AustromApplication.appUser!!.userId
    var isPrivate: Boolean = false
    var isArchived: Boolean = false

    fun delete(dbProvider: LocalDatabaseProvider) {
        dbProvider.deleteAsset(this)
        AustromApplication.activeAssets.remove(assetId)
    }

    fun add(dbProvider: LocalDatabaseProvider) {
        dbProvider.createNewAsset(this)
        AustromApplication.activeAssets[this.assetId] = this
    }

    companion object{
        fun generateUniqueAssetKey() : String {
            return UUID.randomUUID().toString()
        }

        fun groupAssetsByType(assets: MutableMap<String, Asset>) : MutableMap<AssetType, MutableList<Asset>> {
            val groupedAssets = mutableMapOf<AssetType, MutableList<Asset>>()
            for (asset in assets) {
                if (!groupedAssets.containsKey(asset.value.assetTypeId)) {
                    groupedAssets[asset.value.assetTypeId] = mutableListOf()
                }
                groupedAssets[asset.value.assetTypeId]!!.add(asset.value)
            }
            return  groupedAssets
        }
    }
}

enum class AssetType(val stringResourceId: Int = R.string.unresolved, val stringDescriptionResourceId: Int = R.string.unresolved, val iconResourceId: Int = R.drawable.ic_placeholder_icon, val isLiability: Boolean = false){
    CARD(R.string.debit_card, R.string.card_desc, R.drawable.ic_assettype_card_temp),
    CASH(R.string.cash, R.string.cash_desc, R.drawable.ic_assettype_cash_temp),
    DEPOSIT(R.string.deposit, R.string.deposit_desc, R.drawable.ic_assettype_deposit_temp),
    INVESTMENT(R.string.investment, R.string.investment_desc, R.drawable.ic_assettype_investment_temp),

    CREDIT_CARD(R.string.credit_card, R.string.credit_card_desc, R.drawable.ic_assettype_card_temp, true),
    LOAN(R.string.loan, R.string.loan_desc, R.drawable.ic_assettype_loan_temp, true),
    MORTAGE(R.string.mortage, R.string.mortage_desc, R.drawable.ic_assettype_mortage_temp, true)
}

enum class AssetValidationType{
    VALID, UNKNOWN_ASSET_TYPE_INVALID, AMOUNT_INVALID
}

class InvalidAssetException(message: String, validationType: AssetValidationType) : Exception(message) {
    constructor(validationType: AssetValidationType): this(when(validationType) {
        AssetValidationType.VALID -> "CRITICAL ERROR!!! Asset is valid but InvalidTransactionException thrown."
        AssetValidationType.UNKNOWN_ASSET_TYPE_INVALID -> "Invalid Asset Type"
        AssetValidationType.AMOUNT_INVALID -> "Provided amount is not allowed for this type of Asset"
    }, validationType)
}