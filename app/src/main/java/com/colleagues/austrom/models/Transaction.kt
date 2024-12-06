package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import java.time.LocalDate
import java.util.UUID

@Entity(foreignKeys = [ForeignKey(entity = Asset::class,
    parentColumns = ["assetId"],
    childColumns = ["sourceId"],
    onDelete = ForeignKey.NO_ACTION),
    ForeignKey(entity = Asset::class,
        parentColumns = ["assetId"],
        childColumns = ["targetId"],
        onDelete = ForeignKey.NO_ACTION)])
class Transaction(
    @PrimaryKey(autoGenerate = false)
    var transactionId: String = "",
    val userId: String? = null,
    val sourceId: String? = null,
    val sourceName: String? = null,
    val targetId: String? = null,
    val targetName: String? = null,
    val amount: Double = 0.0,
    val secondaryAmount: Double? = null,
    var categoryId: String? = null,
    var transactionDate: LocalDate? = null,
    var transactionDateInt: Int? = null,
    var comment: String? = null)    {

    fun transactionType(): TransactionType {
        return if (this.sourceId!=null && this.targetId!=null) TransactionType.TRANSFER
        else if (this.sourceId!=null) TransactionType.EXPENSE else TransactionType.INCOME
    }

    fun cancel(dbProvider: LocalDatabaseProvider) {
        when (this.transactionType()) {
            TransactionType.INCOME -> {
                val target = AustromApplication.activeAssets[targetId]
                if (target != null) {
                    target.amount-=this.amount
                    dbProvider.updateAsset(target)
                    dbProvider.deleteTransaction(this)
                }
            }
            TransactionType.TRANSFER -> {
                val target = AustromApplication.activeAssets[targetId]
                val source = AustromApplication.activeAssets[sourceId]
                if (target != null && source!=null) {
                    source.amount+=this.amount
                    target.amount-= if (target.currencyCode == source.currencyCode) this.amount else this.secondaryAmount ?: 0.0
                    dbProvider.updateAsset(target)
                    dbProvider.updateAsset(source)
                    dbProvider.deleteTransaction(this)
                }
            }
            TransactionType.EXPENSE -> {
                val source = AustromApplication.activeAssets[sourceId]
                if (source!=null) {
                    source.amount+=this.amount
                    dbProvider.updateAsset(source)
                    dbProvider.deleteTransaction(this)
                }
            }
        }
    }

    fun submit(dbProvider: LocalDatabaseProvider) {
        when (this.validate()) {
            TransactionValidationType.VALID -> {
                when (this.transactionType()) {
                    TransactionType.INCOME -> {
                        val activeAsset = AustromApplication.activeAssets[targetId]
                        if (activeAsset!=null) {
                            activeAsset.amount+=amount
                            dbProvider.updateAsset(activeAsset)
                            dbProvider.writeNewTransaction(this)
                        }
                    }
                    TransactionType.EXPENSE -> {
                        val activeAsset = AustromApplication.activeAssets[sourceId]
                        if (activeAsset!=null) {
                            activeAsset.amount-=amount
                            dbProvider.updateAsset(activeAsset)
                            dbProvider.writeNewTransaction(this)
                        }
                    }
                    TransactionType.TRANSFER -> {
                        val source = AustromApplication.activeAssets[sourceId]
                        val target = AustromApplication.activeAssets[targetId]
                        if (source!=null && target!=null) {
                            source.amount-=this.amount
                            target.amount+=this.amount
                            dbProvider.updateAsset(source)
                            dbProvider.updateAsset(target)
                            dbProvider.writeNewTransaction(this)
                        }
                    }
                }
            }
            TransactionValidationType.UNKNOWN_ASSET_INVALID -> throw InvalidTransactionException("Assets used in this transaction not recognized by the App", TransactionValidationType.UNKNOWN_ASSET_INVALID)
            TransactionValidationType.UNKNOWN_CATEGORY_INVALID -> throw InvalidTransactionException("Category used in this transaction not recognized by the App", TransactionValidationType.UNKNOWN_ASSET_INVALID)
            TransactionValidationType.NEGATIVE_ASSET_AMOUNT_INVALID -> throw InvalidTransactionException("Assets used in this transaction not recognized by the App", TransactionValidationType.UNKNOWN_ASSET_INVALID)
        }
    }

    fun validate(): TransactionValidationType {
        if (sourceId == null && targetId==null) return TransactionValidationType.UNKNOWN_ASSET_INVALID
        when (transactionType()) {
            TransactionType.INCOME -> if (AustromApplication.activeAssets[targetId]==null) return TransactionValidationType.UNKNOWN_ASSET_INVALID
            TransactionType.EXPENSE -> if (AustromApplication.activeAssets[sourceId]==null) return TransactionValidationType.UNKNOWN_ASSET_INVALID
            TransactionType.TRANSFER -> if (AustromApplication.activeAssets[sourceId]==null || AustromApplication.activeAssets[targetId]==null) return TransactionValidationType.UNKNOWN_ASSET_INVALID
        }
        when (transactionType()) {
            TransactionType.INCOME -> if (AustromApplication.activeIncomeCategories[categoryId]==null) return TransactionValidationType.UNKNOWN_CATEGORY_INVALID
            TransactionType.EXPENSE -> if (AustromApplication.activeExpenseCategories[categoryId]==null) return TransactionValidationType.UNKNOWN_CATEGORY_INVALID
            TransactionType.TRANSFER -> {}
        }
        if (transactionType() == TransactionType.EXPENSE && AustromApplication.activeAssets[sourceId]!!.amount-amount<0) return TransactionValidationType.NEGATIVE_ASSET_AMOUNT_INVALID
        return TransactionValidationType.VALID
    }

    companion object{
        fun groupTransactionsByDate(transactions: MutableList<Transaction>) : MutableMap<LocalDate, MutableList<Transaction>> {
            transactions.sortByDescending { it.transactionDate }
            val groupedTransactions = mutableMapOf<LocalDate, MutableList<Transaction>>()
            for (transaction in transactions){
                if (!groupedTransactions.containsKey(transaction.transactionDate)) {
                    groupedTransactions[transaction.transactionDate!!] = mutableListOf()
                }
                groupedTransactions[transaction.transactionDate]!!.add(transaction)
            }
            return  groupedTransactions
        }

        fun generateUniqueTransactionKey() : String {
            return UUID.randomUUID().toString()
        }
    }
}

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class TransactionValidationType{
    VALID, UNKNOWN_ASSET_INVALID, UNKNOWN_CATEGORY_INVALID, NEGATIVE_ASSET_AMOUNT_INVALID
}

class InvalidTransactionException(message: String, validationType: TransactionValidationType) : Exception(message)