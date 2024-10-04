package com.colleagues.austrom.models

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
    val transactionDateInt: Int? = null,
    val comment: String? = null,
    val details: MutableList<TransactionDetail> = mutableListOf())  {

    fun getTransactionType(): TransactionType {
        return if (this.sourceId!=null && this.targetId!=null) TransactionType.TRANSFER
        else if (this.sourceId!=null) TransactionType.EXPENSE else TransactionType.INCOME
    }

    override fun toString(): String {
        return "$transactionId~$userId~$sourceId~$sourceName~$targetId~$targetName~$amount~$secondaryAmount~$categoryId~${transactionDate.toString()}~$comment"
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

        fun parseFromString(string: String): Transaction {
            val array = string.split("~")
            return Transaction(
                transactionId = array[0],
                userId = array[1],
                sourceId = if (array[2]=="null") null else array[2],
                sourceName = array[3],
                targetId = if (array[4]=="null") null else array[4],
                targetName = array[5],
                amount = array[6].toDouble(),
                secondaryAmount = if (array[7]=="null") null else array[7].toDouble(),
                categoryId = array[8],
                transactionDate = LocalDate.parse(array[9]),
                comment = array[10]
            )
        }
    }
}

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}