package com.colleagues.austrom.models

import java.time.LocalDate
import java.util.Date

class Transaction(
    var transactionId: String? = null,
    val userID: String? = null,
    val sourceID: String? = null,
    val sourceName: String? = null,
    val targetID: String? = null,
    val targetName: String? = null,
    val amount: Double = 0.0,
    val currency: String? = null,
    val categoryID: String? = null,
    var transactionDate: LocalDate? = null,
    val transactionDateInt: Int? = null,
    val comment: String? = null)  {
}