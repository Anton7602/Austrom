package com.colleagues.austrom.models

import java.time.LocalDate
import java.util.Date

class Transaction(
    val userID: String? = null,
    val sourceID: String? = null,
    val sourceName: String? = null,
    val targetID: String? = null,
    val targetName: String? = null,
    val amount: Double = 0.0,
    val categoryID: String? = null,
    val transactionDate: LocalDate? = null,
    val comment: String? = null)  {
}