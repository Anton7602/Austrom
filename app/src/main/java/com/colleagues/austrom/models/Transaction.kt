package com.colleagues.austrom.models

import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.database.IDatabaseProvider
import java.time.LocalDate

class Transaction(
    var transactionId: String? = null,
    val userId: String? = null,
    val sourceId: String? = null,
    val sourceName: String? = null,
    val targetId: String? = null,
    val targetName: String? = null,
    val amount: Double = 0.0,
    val secondaryAmount: Double? = null,
    val categoryId: String? = null,
    var transactionDate: LocalDate? = null,
    var transactionDateInt: Int? = null,
    var comment: String? = null,
    val details: MutableList<TransactionDetail> = mutableListOf())  {

    fun transactionType(): TransactionType {
        return if (this.sourceId!=null && this.targetId!=null) TransactionType.TRANSFER
        else if (this.sourceId!=null) TransactionType.EXPENSE else TransactionType.INCOME
    }

    fun cancel(dbProvider: IDatabaseProvider) {
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
    }
}

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}