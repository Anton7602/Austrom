package com.colleagues.austrom.models

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.parseToDouble
import com.google.firebase.database.Exclude
import java.util.UUID

@Entity
class Asset(var assetName: String, val assetTypeId: AssetType, val currencyCode: String, var amount: Double = 0.0,
    @PrimaryKey(autoGenerate = false) @Exclude
    var assetId: String = generateUniqueAssetKey(),
            var userId: String = AustromApplication.appUser!!.userId,
            var bank: String? = null,
            var percent: Double = 0.0,
            var isPrivate: Boolean = false,
            var isArchived: Boolean = false,
            var isLiquid: Boolean = false,
            var isRefillable: Boolean = false) {

    fun serialize(): String { return "$assetId,$assetName,$assetTypeId,$bank,$percent,$currencyCode,$amount,$userId,$isPrivate,$isArchived,$isLiquid,$isRefillable"}

    fun delete(localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider? = null) {
        localDBProvider.getTransactionsOfAsset(this).forEach { transaction -> transaction.cancel(localDBProvider, remoteDBProvider as FirebaseDatabaseProvider) }
        localDBProvider.deleteAsset(this)
        AustromApplication.activeAssets.remove(assetId)
        if (remoteDBProvider!=null && AustromApplication.appUser!!.activeBudgetId!=null) {
            remoteDBProvider.deleteAsset(this, remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)!!)
        }
    }

    fun add(localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider? = null) {
        localDBProvider.createNewAsset(this)
        AustromApplication.activeAssets[this.assetId] = this
        if (remoteDBProvider!=null && AustromApplication.appUser!!.activeBudgetId!=null) {
            remoteDBProvider.createNewAsset(this, remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)!!)
        }
    }

    fun update(localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider? = null) {
        localDBProvider.updateAsset(this)
        AustromApplication.activeAssets[this.assetId] = this
        if (remoteDBProvider!=null && AustromApplication.appUser!!.activeBudgetId!=null) {
            remoteDBProvider.updateAsset(this, remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)!!)
        }
    }

    companion object{
        fun getSumOfAssets(assets: List<Asset>): Double {
            var sum = 0.0
            for (asset in AustromApplication.activeAssets) {
                sum += if (asset.value.currencyCode==AustromApplication.appUser?.baseCurrencyCode)
                    asset.value.amount else asset.value.amount/(AustromApplication.activeCurrencies[asset.value.currencyCode]?.exchangeRate ?: 1.0)
            }
            return sum
        }

        fun generateUniqueAssetKey() : String {
            return UUID.randomUUID().toString()
        }

        fun groupAssetsByType(assets: MutableMap<String, Asset>, context: Context? = null) : MutableMap<String, MutableList<Asset>> {
            val groupedAssets = mutableMapOf<String, MutableList<Asset>>()
            for (asset in assets) {
                val assetTypeName = context?.getString(asset.value.assetTypeId.stringResourceId) ?: asset.value.assetTypeId.toString()
                if (!groupedAssets.containsKey(assetTypeName)) {
                    groupedAssets[assetTypeName] = mutableListOf()
                }
                groupedAssets[assetTypeName]!!.add(asset.value)
            }

            return  sortAssetGroupsByAmount(groupedAssets)
        }

        fun groupAssetsByCurrency(assets: MutableMap<String, Asset>, context: Context? = null) : MutableMap<String, MutableList<Asset>> {
            val groupedAssets = mutableMapOf<String, MutableList<Asset>>()
            for (asset in assets) {
                val currencyName = if (context!=null) Currency.getCurrencyName(asset.value.currencyCode, context) else asset.value.currencyCode
                if (!groupedAssets.containsKey(currencyName)) {
                    groupedAssets[currencyName] = mutableListOf()
                }
                groupedAssets[currencyName]!!.add(asset.value)
            }
            return  sortAssetGroupsByAmount(groupedAssets)
        }

        private fun sortAssetGroupsByAmount(assets: MutableMap<String, MutableList<Asset>>) : MutableMap<String, MutableList<Asset>> {
            val sortedAssetGroups = assets.entries
                .sortedByDescending { (_, assets) ->
                    assets.sumOf { asset ->
                        if (asset.currencyCode == AustromApplication.appUser!!.baseCurrencyCode) {
                            asset.amount
                        } else {
                            val exchangeRate = AustromApplication.activeCurrencies[asset.currencyCode]?.exchangeRate
                                ?: throw IllegalArgumentException("Exchange rate not found for currency: ${asset.currencyCode}")
                            asset.amount / exchangeRate
                        }
                    }
                }
                .associate { it.key to it.value }.toMutableMap()
            sortedAssetGroups.forEach { (group, assetList) ->
                sortedAssetGroups[group] = assetList.sortedByDescending {asset -> if (asset.currencyCode == AustromApplication.appUser!!.baseCurrencyCode) {
                    asset.amount
                } else {
                    val exchangeRate = AustromApplication.activeCurrencies[asset.currencyCode]?.exchangeRate
                        ?: throw IllegalArgumentException("Exchange rate not found for currency: ${asset.currencyCode}")
                    asset.amount / exchangeRate
                }}.toMutableList()
            }
            return  sortedAssetGroups
        }

        fun deserialize(serializedAsset: String): Asset {
            val dataParts = serializedAsset.split(",")
            return Asset(
                assetId = dataParts[0],
                assetName = dataParts[1],
                assetTypeId = when(dataParts[2]) {
                    "CARD" -> AssetType.CARD
                    "CASH" -> AssetType.CASH
                    "DEPOSIT" -> AssetType.DEPOSIT
                    "INVESTMENT" -> AssetType.INVESTMENT
                    "REAL_ESTATE" -> AssetType.REAL_ESTATE
                    "CREDIT_CARD" -> AssetType.CREDIT_CARD
                    "LOAN" -> AssetType.LOAN
                    "MORTAGE" -> AssetType.MORTAGE
                    else -> AssetType.CARD
                },
                bank = dataParts[3],
                percent = dataParts[4].parseToDouble() ?: 0.0,
                currencyCode = dataParts[5],
                amount = dataParts[6].parseToDouble() ?: 0.0,
                userId = dataParts[7],
                isPrivate = dataParts[8].toBoolean(),
                isArchived = dataParts[9].toBoolean(),
                isLiquid = dataParts[10].toBoolean(),
                isRefillable = dataParts[11].toBoolean()
            )
        }
    }
}

enum class AssetType(val stringResourceId: Int = R.string.unresolved, val stringDescriptionResourceId: Int = R.string.unresolved, val iconResourceId: Int = R.drawable.ic_placeholder_icon, val isLiability: Boolean = false){
    CARD(R.string.debit_card, R.string.card_desc, R.drawable.ic_assettype_card_temp),
    CASH(R.string.cash, R.string.cash_desc, R.drawable.ic_assettype_cash_temp),
    DEPOSIT(R.string.deposit, R.string.deposit_desc, R.drawable.ic_assettype_deposit_temp),
    INVESTMENT(R.string.investment, R.string.investment_desc, R.drawable.ic_assettype_investment_temp),
    REAL_ESTATE(R.string.real_estate, R.string.real_estate_desc, R.drawable.ic_assettype_realestate_temp),

    CREDIT_CARD(R.string.credit_card, R.string.credit_card_desc, R.drawable.ic_assettype_card_temp, true),
    LOAN(R.string.loan, R.string.loan_desc, R.drawable.ic_assettype_loan_temp, true),
    MORTAGE(R.string.mortgage, R.string.mortgage_desc, R.drawable.ic_assettype_mortage_temp, true)
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