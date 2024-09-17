package com.colleagues.austrom.models

import java.time.LocalDate
import java.util.Date

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
    val comment: String? = null)  {
}

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}