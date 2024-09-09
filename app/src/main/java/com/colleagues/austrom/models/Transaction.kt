package com.colleagues.austrom.models

import java.util.Date

class Transaction(
    val userID: String? = null,
    val sourceID: String? = null,
    val sourceName: String? = null,
    val targetID: String? = null,
    val targetName: String? = null,
    val categoryID: String? = null,
    val transactionDate: Date? = null,
    val comment: String? = null)  {


}